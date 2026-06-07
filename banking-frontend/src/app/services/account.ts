import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Account {
  id: number;
  accountNumber: string;
  accountType: string;
  balance: number;
  status: string;
  maturityStatus?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AccountService {
  private http = inject(HttpClient);
  private apiUrl = environment.apiUrl;

  getMyAccounts(): Observable<Account[]> {
    return this.http.get<Account[]>(`${this.apiUrl}/accounts/my-accounts`);
  }

  withdrawMatured(fixedId: number, targetId: number): Observable<any> {
    const payload = { fixedId, targetId };
    return this.http.post(`${this.apiUrl}/fixed-deposits/withdraw-matured`, payload);
  }

  closeFixedDeposit(fixedId: number, targetId: number): Observable<any> {
    const payload = { fixedId, targetId };
    return this.http.post(`${this.apiUrl}/fixed-deposits/close`, payload);
  }

  createAccount(payload: { accountType: string, initialDeposit: number, maturityMonths?: number }): Observable<any> {
    return this.http.post(`${this.apiUrl}/account-create`, payload);
  }

  getRetryPayload(accNo: string) {
    return this.http.get<any>(`${environment.apiUrl}/account-create/retry/${accNo}`);
  }
}
