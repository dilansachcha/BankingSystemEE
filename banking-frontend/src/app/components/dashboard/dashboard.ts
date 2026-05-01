import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AccountService, Account } from '../../services/account';
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule, RouterModule],
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
}
