import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../services/auth';
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class Login {
  email = '';
  password = '';
  otp = '';
  resetCode = '';
  newPassword = '';
  confirmPassword = '';

  errorMessage = '';
  successMessage = '';
  hidePassword = true;
  hideNewPassword = true;
  hideConfirm = true;
  isLoading = false;

  isOtpMode = false;
  isForgotMode = false;
  isResetMode = false;

  timeLeft: number = 180; // 3 mins
  timerInterval: any;
  canResend: boolean = false;

  private authService = inject(AuthService);
  private router = inject(Router);

  onSubmit() {
    this.errorMessage = '';
    this.isLoading = true;

    this.authService.login(this.email, this.password).subscribe({
      next: (res) => {
        this.isLoading = false;
        if (res.status === 'pending_verification') {
          this.isOtpMode = true;
          this.successMessage = "Admin detected. An OTP has been sent to your email.";
          this.startTimer();
          return;
        }
        localStorage.setItem('token', res.token);
        localStorage.setItem('role', res.role);
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error?.error || 'Invalid email or password.';
      }
    });
  }

  startTimer() {
    this.timeLeft = 180;
    this.canResend = false;
    clearInterval(this.timerInterval);

    this.timerInterval = setInterval(() => {
      if (this.timeLeft > 0) {
        this.timeLeft--;
      } else {
        this.canResend = true;
        clearInterval(this.timerInterval);
      }
    }, 1000);
  }

  get formattedTime() {
    const minutes: number = Math.floor(this.timeLeft / 60);
    const seconds: number = this.timeLeft % 60;
    return `${minutes}:${seconds < 10 ? '0' : ''}${seconds}`;
  }

  onResendOtp() {
    this.errorMessage = '';
    this.successMessage = '';
    this.isLoading = true;

    this.authService.login(this.email, this.password).subscribe({
      next: () => {
        this.isLoading = false;
        this.successMessage = "A new OTP has been sent to your email.";
        this.startTimer(); //Restart
      },
      error: () => {
        this.isLoading = false;
        this.errorMessage = "Failed to resend OTP. Please try again.";
      }
    });
  }

  onVerifyOtp() {
    this.errorMessage = '';
    this.isLoading = true;
    this.authService.verifyAdmin(this.email, this.otp).subscribe({
      next: (res) => {
        this.isLoading = false;
        localStorage.setItem('token', res.token);
        localStorage.setItem('role', res.role);
        this.router.navigate(['/admin-dashboard']);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error?.error || 'Invalid or expired OTP.';
      }
    });
  }

  onRequestReset() {
    this.errorMessage = '';
    this.isLoading = true;
    this.authService.forgotPassword(this.email).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.successMessage = res.message;
        this.isForgotMode = false;
        this.isResetMode = true;
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error?.error || 'Could not process request.';
      }
    });
  }

  onResetPassword() {
    this.errorMessage = '';

    if (this.newPassword !== this.confirmPassword) {
      this.errorMessage = "Passwords do not match!";
      return;
    }
    const passwordPattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[\W_]).{8,}$/;
    if (!passwordPattern.test(this.newPassword)) {
      this.errorMessage = "Password must be 8+ characters with uppercase, lowercase, number, and special character.";
      return;
    }

    this.isLoading = true;
    const payload = {
      email: this.email,
      code: this.resetCode,
      newPassword: this.newPassword
    };

    this.authService.resetPassword(payload).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.successMessage = "Password reset successful! You can now log in.";
        this.cancelFlows();
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error?.error || 'Invalid or expired reset code.';
      }
    });
  }

  cancelFlows() {
    clearInterval(this.timerInterval);
    this.isOtpMode = false;
    this.isForgotMode = false;
    this.isResetMode = false;
    this.errorMessage = '';
    this.password = '';
    this.newPassword = '';
    this.confirmPassword = '';
    this.otp = '';
    this.resetCode = '';
  }
}
