import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Router, RouterModule } from '@angular/router';
import { AccountService, Account } from '../../services/account';
import { TransactionService } from '../../services/transaction';

@Component({
  selector: 'app-transfer',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatCardModule, MatButtonModule,
    MatInputModule, MatSelectModule, MatProgressSpinnerModule, RouterModule
  ],
  templateUrl: './transfer.html',
  styleUrl: './transfer.scss'
})
export class Transfer implements OnInit {
  transferForm!: FormGroup;
  myAccounts: Account[] = [];
  isLoading = true;
  isSubmitting = false;
  errorMessage = '';
  successMessage = '';

  private fb = inject(FormBuilder);
  private accountService = inject(AccountService);
  private transactionService = inject(TransactionService);
  private router = inject(Router);

  ngOnInit() {
    this.transferForm = this.fb.group({
      fromAccNo: ['', Validators.required],
      toAccNo: ['', [Validators.required, Validators.minLength(5)]],
      amount: ['', [Validators.required, Validators.min(1)]]
    });

    this.accountService.getMyAccounts().subscribe({
      next: (data) => {
        this.myAccounts = data.filter(a => a.status === 'ACTIVE' && a.accountType !== 'FIXED');
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Failed to load your accounts.';
        this.isLoading = false;
      }
    });
  }

  onSubmit() {
    if (this.transferForm.invalid) return;

    this.isSubmitting = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.transactionService.transfer(this.transferForm.value).subscribe({
      next: (response) => {
        this.isSubmitting = false;
        this.successMessage = 'Transfer Successful!';
        this.transferForm.reset();

        setTimeout(() => this.router.navigate(['/dashboard']), 2000);
      },
      error: (err) => {
        this.isSubmitting = false;
        this.errorMessage = err.error?.error || 'Transfer failed. Please try again.';
      }
    });
  }
}
