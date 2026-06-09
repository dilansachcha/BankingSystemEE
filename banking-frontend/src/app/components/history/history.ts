import { Component, OnInit, inject } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { RouterModule } from '@angular/router';
import { HistoryService, TransactionRecord } from '../../services/history';

import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

@Component({
  selector: 'app-history',
  standalone: true,
  imports: [CommonModule, MatTableModule, MatCardModule, MatButtonModule, MatProgressSpinnerModule, MatIconModule, RouterModule],
  providers: [DatePipe],
  templateUrl: './history.html',
  styleUrl: './history.scss'
})
export class History implements OnInit {
  transactions: TransactionRecord[] = [];
  displayedColumns: string[] = ['date', 'accountNumber', 'type', 'description', 'amount'];
  isLoading = true;
  errorMessage = '';

  private historyService = inject(HistoryService);
  private datePipe = inject(DatePipe);

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

  downloadPDF() {
    const doc = new jsPDF();

    doc.setFontSize(22);
    doc.setTextColor(20, 20, 24);
    doc.text('FORTRESS BANK', 14, 20);

    doc.setFontSize(11);
    doc.setTextColor(100, 100, 100);
    doc.text('Official Transaction Statement', 14, 28);
    doc.text(`Generated on: ${this.datePipe.transform(new Date(), 'medium')}`, 14, 34);

    const head = [['Date & Time', 'Account', 'Type', 'Description', 'Amount (LKR)']];
    const body = this.transactions.map(t => [
      this.datePipe.transform(t.date, 'short') || '',
      t.accountNumber,
      t.type,
      t.description,
      (t.type === 'DEBIT' ? '-' : '+') + ' ' + t.amount.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })
    ]);

    autoTable(doc, {
      startY: 45,
      head: head,
      body: body,
      theme: 'striped',
      headStyles: { fillColor: [18, 18, 20], textColor: 255 },
      alternateRowStyles: { fillColor: [245, 245, 245] },
      styles: { fontSize: 10, cellPadding: 4 },
      columnStyles: {
        4: { halign: 'right', fontStyle: 'bold' }
      }
    });

    doc.save(`Fortress_Statement_${new Date().getTime()}.pdf`);
  }
}
