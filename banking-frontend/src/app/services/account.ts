import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

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

  // Java REST endpoint
  private apiUrl = 'http://localhost:8080/BankingSystemEE-1.0-SNAPSHOT/api/accounts';

  getMyAccounts(): Observable<Account[]> {
    return this.http.get<Account[]>(`${this.apiUrl}/my-accounts`);
  }
}
