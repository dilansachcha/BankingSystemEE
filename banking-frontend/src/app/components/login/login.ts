import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../services/auth';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class Login {
  email = '';
  password = '';
  errorMessage = '';
  hidePassword = true;

  private authService = inject(AuthService);
  private router = inject(Router); // INJECT ROUTER

  onSubmit() {
    this.errorMessage = '';
    this.authService.login(this.email, this.password).subscribe({
      next: (response) => {
        console.log('Login Success!', response);
        localStorage.setItem('token', response.token);

        //NAVIGATE TO DASHBOARD
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        console.error('Login Failed', err);
        this.errorMessage = 'Invalid email or password.';
      }
    });
  }
}
