import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { RouterModule } from '@angular/router';
import { HistoryService, TransactionRecord } from '../../services/history';

@Component({
  selector: 'app-history',
  standalone: true,
  imports: [CommonModule, MatTableModule, MatCardModule, MatButtonModule, MatProgressSpinnerModule, RouterModule],
  templateUrl: './history.html',
  styleUrl: './history.scss'
})
export class History implements OnInit {
  transactions: TransactionRecord[] = [];
  displayedColumns: string[] = ['date', 'accountNumber', 'type', 'description', 'amount'];
  isLoading = true;
  errorMessage = '';

  private historyService = inject(HistoryService);

  ngOnInit() {
    this.historyService.getMyHistory().subscribe({
      next: (data) => {
        this.transactions = data;
        this.isLoading = false;
      },
      error: (err) => {
        console.error(err);
        this.errorMessage = 'Could not load transaction history.';
        this.isLoading = false;
      }
    });
  }
}
