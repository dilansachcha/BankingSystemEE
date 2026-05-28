import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { Account } from '../../services/account';

@Component({
  selector: 'app-fixed-action-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatSelectModule,
    MatButtonModule
  ],
  templateUrl: './fixed-action-dialog.html',
  styleUrls: ['./fixed-action-dialog.scss']
})
export class FixedActionDialogComponent {
  selectedTargetId: number | null = null;

  constructor(
    public dialogRef: MatDialogRef<FixedActionDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: {
      action: 'withdraw' | 'close',
      fixedAccount: Account,
      targetAccounts: Account[]
    }
  ) {}

  onCancel(): void {
    this.dialogRef.close();
  }

  onConfirm(): void {
    if (this.selectedTargetId) {
      this.dialogRef.close(this.selectedTargetId);
    }
  }
}
