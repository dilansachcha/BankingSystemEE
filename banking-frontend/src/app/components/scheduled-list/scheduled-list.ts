import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { RouterModule } from '@angular/router';
import { ScheduledService, ScheduledTransaction } from '../../services/scheduled';

@Component({
  selector: 'app-scheduled-list',
  standalone: true,
  imports: [CommonModule, MatTableModule, MatCardModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule, RouterModule],
  templateUrl: './scheduled-list.html',
  styleUrl: './scheduled-list.scss'
})
export class ScheduledList implements OnInit {
  schedules: ScheduledTransaction[] = [];
  displayedColumns: string[] = ['scheduledTime', 'fromAccount', 'toAccount', 'amount', 'recurring', 'status', 'lastExecuted', 'actions'];
  isLoading = true;
  errorMessage = '';

  private scheduledService = inject(ScheduledService);

  ngOnInit() {
    this.fetchSchedules();
  }

  fetchSchedules() {
    this.scheduledService.getMySchedules().subscribe({
      next: (data) => {
        this.schedules = data;
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Could not load scheduled transfers.';
        this.isLoading = false;
      }
    });
  }

  deleteSchedule(id: number) {
    if (confirm('Are you sure you want to cancel this scheduled transfer?')) {
      this.scheduledService.cancelSchedule(id).subscribe({
        next: () => {
          // Remove from UI instantly without refreshing
          this.schedules = this.schedules.filter(s => s.id !== id);
        },
        error: () => alert('Failed to cancel schedule.')
      });
    }
  }
}
