import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { AccountService } from '../../services/account';

declare var payhere: any;

@Component({
  selector: 'app-open-account',
  standalone: true,
  imports: [
    CommonModule, FormsModule, RouterModule,
    MatCardModule, MatButtonModule, MatFormFieldModule,
    MatInputModule, MatSelectModule, MatIconModule
  ],
  templateUrl: './open-account.html',
  styleUrls: ['./open-account.scss']
})
export class OpenAccountComponent {
  private accountService = inject(AccountService);
  private router = inject(Router);

  accountType: string = 'SAVINGS';
  initialDeposit: number | null = null;
  maturityMonths: number = 6;

  errorMessage: string = '';
  isLoading: boolean = false;

  get minDeposit(): number {
    if (this.accountType === 'SAVINGS') return 2000;
    if (this.accountType === 'CHECKING') return 1000;
    if (this.accountType === 'FIXED') return 5000;
    return 0;
  }

  onSubmit() {
    if (!this.initialDeposit || this.initialDeposit < this.minDeposit) {
      this.errorMessage = `Minimum deposit for ${this.accountType} is LKR ${this.minDeposit.toLocaleString()}`;
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    const payload = {
      accountType: this.accountType,
      initialDeposit: this.initialDeposit,
      maturityMonths: this.accountType === 'FIXED' ? this.maturityMonths : undefined
    };

    this.accountService.createAccount(payload).subscribe({
      next: (response: any) => {
        this.triggerPayHere(response);
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = err.error?.error || "Failed to create account.";
        this.isLoading = false;
      }
    });
  }

  triggerPayHere(data: any) {
    payhere.onCompleted = (orderId: string) => {
      alert("Payment successful! Your account is now ACTIVE.");
      this.router.navigate(['/dashboard']);
    };

    payhere.onDismissed = () => {
      alert("Payment dismissed. Your account remains PENDING until funded.");
      this.router.navigate(['/dashboard']);
    };

    payhere.onError = (error: string) => {
      this.errorMessage = "Payment Error: " + error;
    };

    const depositAmount = this.initialDeposit ? this.initialDeposit : 0;

    const payment = {
      "sandbox": true,
      "merchant_id": data.merchantId,
      "return_url": window.location.origin + "/dashboard",
      "cancel_url": window.location.origin + "/dashboard",
      "notify_url": "http://DROPLET_IP/BankingSystemEE-1.0-SNAPSHOT/api/payhere/notify",
      "order_id": data.orderId,
      "items": "Initial Deposit - " + this.accountType,
      "amount": depositAmount,
      "currency": "LKR",
      "hash": data.hash,
      "first_name": "Valued",
      "last_name": "Customer",
      "email": "customer@bankingsystemee.com",
      "phone": "0771234567",
      "address": "No.1, 1st Cross Street, Pettah",
      "city": "Colombo",
      "country": "Sri Lanka"
    };

    payhere.startCheckout(payment);
  }
}
