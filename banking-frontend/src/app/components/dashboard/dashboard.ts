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
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

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
    MatDialogModule,
    MatSnackBarModule
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
  private snackBar = inject(MatSnackBar);

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
      this.showNotification("You need an active Savings or Checking account to receive these funds.", true);
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
              this.showNotification(res.message || "Successfully withdrawn!");
              this.fetchAccounts();
            },
            error: (err) => this.showNotification("Error: " + (err.error?.error || "Transaction failed"), true)
          });
        } else if (action === 'close') {
          this.accountService.closeFixedDeposit(fixedAccount.id, selectedTargetId).subscribe({
            next: (res) => {
              this.showNotification(res.message || "Fixed Deposit successfully closed!");
              this.fetchAccounts();
            },
            error: (err) => this.showNotification("Error: " + (err.error?.error || "Transaction failed"), true)
          });
        }
      }
    });
  }

  fundPendingAccount(acc: Account) {
    this.accountService.getRetryPayload(acc.accountNumber).subscribe({
      next: (data) => this.triggerPayHere(data),
      error: (err) => this.showNotification("Failed to contact payment gateway.", true)
    });
  }

  triggerPayHere(data: any) {
    (window as any).payhere.onCompleted = (orderId: string) => {
      this.showNotification("Payment successful! Your account is now ACTIVE.");
      this.fetchAccounts(); // Refresh the dashboard!
    };

    (window as any).payhere.onDismissed = () => {
      this.showNotification("Payment dismissed.", true);
    };

    (window as any).payhere.onError = (error: string) => {
      this.showNotification("Payment Error: " + error, true);
    };

    const payment = {
      "sandbox": true,
      "merchant_id": data.merchantId,
      "return_url": window.location.origin + "/dashboard",
      "cancel_url": window.location.origin + "/dashboard",
      "notify_url": "https://fortressbank.dedyn.io/BankingSystemEE-1.0-SNAPSHOT/api/payhere/notify",
      "order_id": data.orderId,
      "items": "Account Funding - " + data.accountType,
      "amount": data.amount,
      "currency": "LKR",
      "hash": data.hash,
      "first_name": "Valued",
      "last_name": "Customer",
      "email": "dilansachintha44@gmail.com",
      "phone": "0771855521",
      "address": "No.1, 1st Cross Street, Pettah",
      "city": "Colombo",
      "country": "Sri Lanka"
    };

    (window as any).payhere.startPayment(payment);
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
