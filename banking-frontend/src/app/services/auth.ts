import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);

  // This must match your GlassFish URL exactly!
  private apiUrl = 'http://localhost:8080/BankingSystemEE-1.0-SNAPSHOT/api/auth';

  login(email: string, password: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, { email, password });
  }

  verifyAdmin(email: string, otp: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/verify-admin`, { email, otp });
  }

  register(payload: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, payload);
  }

  forgotPassword(email: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/forgot-password`, { email });
  }

  resetPassword(payload: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/reset-password`, payload);
  }
}
