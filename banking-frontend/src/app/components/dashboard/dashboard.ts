import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { AccountService, Account } from '../../services/account';
import { Router, RouterModule } from '@angular/router';
import { FixedActionDialogComponent } from '../fixed-action-dialog/fixed-action-dialog';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    RouterModule,
    MatDialogModule
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class Dashboard implements OnInit {
  activeAccounts: Account[] = [];
  otherAccounts: Account[] = [];

  isLoading = true;
  errorMessage = '';

  private router = inject(Router);
  private accountService = inject(AccountService);
  private dialog = inject(MatDialog);

  ngOnInit() {
    this.fetchAccounts();
  }

  fetchAccounts() {
    this.accountService.getMyAccounts().subscribe({
      next: (data) => {
        this.activeAccounts = data.filter(a => a.status === 'ACTIVE');
        this.otherAccounts = data.filter(a => a.status !== 'ACTIVE');
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error fetching accounts', err);
        this.errorMessage = 'Failed to load accounts. Please try again later.';
        this.isLoading = false;
      }
    });
  }

  logout() {
    localStorage.removeItem('token');
    this.router.navigate(['/']);
  }

  openFixedActionDialog(fixedAccount: Account, action: 'withdraw' | 'close') {
    const validTargets = this.activeAccounts.filter(a => a.accountType !== 'FIXED');

    if (validTargets.length === 0) {
      alert("You need an active Savings or Checking account to receive these funds.");
      return;
    }

    const dialogRef = this.dialog.open(FixedActionDialogComponent, {
      width: '450px',
      panelClass: 'dark-dialog-panel',
      data: {
        action: action,
        fixedAccount: fixedAccount,
        targetAccounts: validTargets
      }
    });

    dialogRef.afterClosed().subscribe(selectedTargetId => {
      if (selectedTargetId) {
        if (action === 'withdraw') {
          this.accountService.withdrawMatured(fixedAccount.id, selectedTargetId).subscribe({
            next: (res) => {
              alert(res.message || "Successfully withdrawn!");
              this.fetchAccounts(); // <-- Refreshes the dashboard automatically!
            },
            error: (err) => alert("Error: " + (err.error?.error || "Transaction failed"))
          });
        } else if (action === 'close') {
          this.accountService.closeFixedDeposit(fixedAccount.id, selectedTargetId).subscribe({
            next: (res) => {
              alert(res.message || "Fixed Deposit successfully closed!");
              this.fetchAccounts(); // <-- Refreshes the dashboard automatically!
            },
            error: (err) => alert("Error: " + (err.error?.error || "Transaction failed"))
          });
        }
      }
    });
  }
}
