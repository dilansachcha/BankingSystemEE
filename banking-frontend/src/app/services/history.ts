import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface TransactionRecord {
  id: number;
  accountNumber: string;
  type: string;
  amount: number;
  description: string;
  date: string;
}

@Injectable({
  providedIn: 'root'
})
export class HistoryService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/history`;

  getMyHistory(): Observable<TransactionRecord[]> {
    return this.http.get<TransactionRecord[]>(this.apiUrl);
  }
}
