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
      next: (res) => {
        alert(res.message);
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.errorMessage = err.error?.error || "Failed to create account.";
        this.isLoading = false;
      }
    });
  }
}
