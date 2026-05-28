import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Router, RouterModule } from '@angular/router';
import { AccountService, Account } from '../../services/account';
import { ScheduledService } from '../../services/scheduled';

@Component({
  selector: 'app-schedule-new',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatCardModule, MatButtonModule,
    MatInputModule, MatSelectModule, MatCheckboxModule, MatProgressSpinnerModule, RouterModule
  ],
  templateUrl: './schedule-new.html',
  styleUrl: './schedule-new.scss'
})
export class ScheduleNew implements OnInit {
  scheduleForm!: FormGroup;
  myAccounts: Account[] = [];
  isLoading = true;
  isSubmitting = false;
  errorMessage = '';
  successMessage = '';

  private fb = inject(FormBuilder);
  private accountService = inject(AccountService);
  private scheduledService = inject(ScheduledService);
  private router = inject(Router);

  ngOnInit() {
    // 1. Setup Form Rules
    this.scheduleForm = this.fb.group({
      fromAcc: ['', Validators.required],
      toAcc: ['', [Validators.required, Validators.minLength(5)]],
      amount: ['', [Validators.required, Validators.min(1)]],
      scheduledTime: ['', Validators.required],
      recurring: [false],
      recurrenceType: [{ value: null, disabled: true }]
    });

    this.scheduleForm.get('recurring')?.valueChanges.subscribe(isRecurring => {
      const typeControl = this.scheduleForm.get('recurrenceType');
      if (isRecurring) {
        typeControl?.enable();
        typeControl?.setValidators([Validators.required]);
      } else {
        typeControl?.disable();
        typeControl?.clearValidators();
        typeControl?.setValue(null);
      }
      typeControl?.updateValueAndValidity();
    });

    this.accountService.getMyAccounts().subscribe({
      next: (data) => {
        this.myAccounts = data.filter(a => a.status === 'ACTIVE' && a.accountType !== 'FIXED');
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Failed to load accounts.';
        this.isLoading = false;
      }
    });
  }

  onSubmit() {
    if (this.scheduleForm.invalid) return;

    this.isSubmitting = true;
    this.errorMessage = '';

    const formValue = this.scheduleForm.getRawValue();

    this.scheduledService.createSchedule(formValue).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.successMessage = 'Transfer Scheduled Successfully!';
        setTimeout(() => this.router.navigate(['/scheduled-list']), 2000);
      },
      error: (err) => {
        this.isSubmitting = false;
        this.errorMessage = err.error?.error || 'Failed to schedule transfer.';
      }
    });
  }
}
