import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule, FormsModule, RouterModule, MatCardModule,
    MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule
  ],
  templateUrl: './register.html',
  styleUrl: './register.scss'
})
export class Register {
  fullName = '';
  email = '';
  nic = '';
  mobile = '';
  password = '';
  confirmPassword = '';

  hidePassword = true;
  hideConfirm = true;
  isLoading = false;
  errorMessage = '';
  successMessage = '';

  private authService = inject(AuthService);
  private router = inject(Router);

  nicPattern = /^(\d{9}[vVxX]|\d{12})$/;
  mobilePattern = /^(07[0-9]{8})$/;
  passwordPattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[\W_]).{8,}$/;

  onSubmit() {
    this.errorMessage = '';
    this.successMessage = '';

    if (!this.nicPattern.test(this.nic)) {
      this.errorMessage = "Invalid NIC format (e.g., 123456789V or 12 digits).";
      return;
    }
    if (!this.mobilePattern.test(this.mobile)) {
      this.errorMessage = "Invalid Sri Lankan Mobile (e.g., 0771234567).";
      return;
    }
    if (!this.passwordPattern.test(this.password)) {
      this.errorMessage = "Password must have 8+ characters, uppercase, lowercase, number, and special character.";
      return;
    }
    if (this.password !== this.confirmPassword) {
      this.errorMessage = "Passwords do not match!";
      return;
    }

    this.isLoading = true;

    const payload = {
      fullName: this.fullName,
      email: this.email,
      nic: this.nic,
      mobile: this.mobile,
      password: this.password
    };

    this.authService.register(payload).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.successMessage = res.message;
        setTimeout(() => this.router.navigate(['/']), 2500);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error?.error || 'Registration failed. Please try again.';
      }
    });
  }
}
