import { Component, OnInit, OnDestroy, inject, ViewChild, TemplateRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { Router } from '@angular/router';
import { AdminService } from '../../services/admin';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [
    CommonModule, MatCardModule, MatButtonModule, MatIconModule, MatTableModule,
    MatProgressSpinnerModule, MatSnackBarModule, MatDialogModule, FormsModule,
    MatFormFieldModule, MatInputModule
  ],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.scss'
})
export class AdminDashboard implements OnInit, OnDestroy {
  pendingUsers: any[] = [];
  allAccounts: any[] = [];

  isLoading = true;
  isDownloading = false;

  analyzingUserId: number | null = null;
  aiAnalysisResult: string | null = null;

  processStatus: string = '';

  currentAiStatus: string = 'Initializing AI Analysis...';

  private socket!: WebSocket;
  liveAlerts: string[] = [];

  private adminService = inject(AdminService);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);
  private dialog = inject(MatDialog);

  @ViewChild('actionDialog') actionDialog!: TemplateRef<any>;
  dialogRef!: MatDialogRef<any>;
  actionPayload: any = {};

  ngOnInit() {
    this.fetchData();
    this.connectWebSocket();
  }

  ngOnDestroy() {
    if (this.socket) {
      this.socket.close();
    }
  }

  fetchData() {
    this.adminService.getDashboardData().subscribe({
      next: (data) => {
        this.pendingUsers = data.pendingUsers;
        this.allAccounts = data.allAccounts;
        this.isLoading = false;
      },
      error: (err) => {
        console.error(err);
        this.isLoading = false;
        this.showNotification("Failed to load Admin data.", true);
      }
    });
  }

  downloadAudit() {
    this.isDownloading = true;
    this.adminService.downloadAuditReport().subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'audit-report.pdf';
        a.click();
        window.URL.revokeObjectURL(url);
        this.isDownloading = false;
        this.showNotification("Audit Report downloaded successfully!");
      },
      error: () => {
        this.showNotification("Failed to download report.", true);
        this.isDownloading = false;
      }
    });
  }

  formatAiResponse(text: string): string {
    if (!text) return '';
    let formatted = text;
    formatted = formatted.replace(/### (.*?)\n/g, '<h3 style="color: white; margin-top: 15px; margin-bottom: 5px;">$1</h3>');
    formatted = formatted.replace(/\*\*(.*?)\*\*/g, '<strong style="color: #ef4444;">$1</strong>');
    formatted = formatted.replace(/\* (.*?)\n/g, '<li style="margin-left: 20px;">$1</li>');
    formatted = formatted.replace(/\n/g, '<br>');
    return formatted;
  }

  analyzeRisk(userId: number, email: string) {
    this.analyzingUserId = userId;
    this.currentAiStatus = 'Contacting AI...';

    this.actionPayload = { type: 'ai-processing', email: email };
    this.dialogRef = this.dialog.open(this.actionDialog, { disableClose: true });

    this.adminService.analyzeUserRisk(userId).subscribe({
      next: (res) => {
        this.dialogRef.close();

        const beautifulText = this.formatAiResponse(res.analysis);

        this.actionPayload = { type: 'ai-result', analysis: beautifulText, email: email };
        this.dialogRef = this.dialog.open(this.actionDialog);
        this.analyzingUserId = null;
      },
      error: (err) => {
        this.dialogRef.close();
        this.actionPayload = { type: 'error', message: "All AI models are busy or unavailable." };
        this.dialogRef = this.dialog.open(this.actionDialog);
        this.analyzingUserId = null;
      }
    });
  }

  logout() {
    localStorage.clear();
    this.router.navigate(['/']);
  }

  openApproveDialog(userId: number) {
    this.actionPayload = { type: 'approve', id: userId };
    this.dialogRef = this.dialog.open(this.actionDialog, { width: '400px', panelClass: 'dark-dialog-panel' });
  }

  openToggleDialog(accountId: number, currentStatus: string) {
    const actionType = currentStatus === 'ACTIVE' ? 'block' : 'unblock';
    this.actionPayload = { type: actionType, id: accountId, reason: '' };
    this.dialogRef = this.dialog.open(this.actionDialog, { width: '400px', panelClass: 'dark-dialog-panel' });
  }

  executeAction() {
    if (this.actionPayload.type === 'approve') {
      this.adminService.approveUser(this.actionPayload.id).subscribe({
        next: () => {
          this.fetchData();
          this.showNotification("User approved successfully!");
        },
        error: () => this.showNotification("Failed to approve user.", true)
      });
    } else {
      this.adminService.toggleAccountStatus(this.actionPayload.id, this.actionPayload.type, this.actionPayload.reason).subscribe({
        next: () => {
          this.fetchData();
          this.showNotification(`Account successfully ${this.actionPayload.type}ed!`);
        },
        error: () => this.showNotification("Failed to update account status.", true)
      });
    }
    this.dialogRef.close();
  }

  connectWebSocket() {
    this.socket = new WebSocket("wss://fortressbank.dedyn.io/BankingSystemEE-1.0-SNAPSHOT/admin-notify");

    this.socket.onmessage = (event) => {
      const data = JSON.parse(event.data);

      if (data.type === "AI_STATUS") {
        this.currentAiStatus = data.message;
      } else {
        let alertMsg = "";

        if (data.type === "TRANSFER") {
          alertMsg = `TRANSFER: ${data.from} ➔ ${data.to} | LKR ${data.amount}`;
        } else if (data.type === "STATUS_CHANGE") {
          alertMsg = `STATUS: Account ${data.account} is now ${data.status}`;
        } else if (data.type === "ALERT") {
          alertMsg = `ALERT: ${data.message}`;
        } else {
          alertMsg = `MSG: ${event.data}`;
        }

        this.liveAlerts.unshift(alertMsg);
        if (this.liveAlerts.length > 5) this.liveAlerts.pop();
      }
    };

    this.socket.onerror = (error) => console.error("WebSocket Error: ", error);
  }

  showNotification(message: string, isError: boolean = false) {
    this.snackBar.open(message, 'Close', {
      duration: 3000,
      panelClass: isError ? ['error-snackbar'] : ['success-snackbar'],
      horizontalPosition: 'right',
      verticalPosition: 'bottom'
    });
  }
}
