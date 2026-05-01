import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface TransferRequest {
  fromAccNo: string;
  toAccNo: string;
  amount: number;
}

@Injectable({
  providedIn: 'root'
})
export class TransactionService {
  private http = inject(HttpClient);

  private apiUrl = 'http://localhost:8080/BankingSystemEE-1.0-SNAPSHOT/api/transactions';

  transfer(request: TransferRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/transfer`, request);
  }
}
