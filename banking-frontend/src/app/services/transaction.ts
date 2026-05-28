import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

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
  private apiUrl = `${environment.apiUrl}/transactions`;

  transfer(request: TransferRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/transfer`, request);
  }
}
