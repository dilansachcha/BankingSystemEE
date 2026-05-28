import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/admin`;

  getDashboardData(): Observable<any> {
    return this.http.get(`${this.apiUrl}/dashboard-data`);
  }

  approveUser(userId: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/users/${userId}/approve`, {});
  }

  toggleAccountStatus(accountId: number, action: 'block' | 'unblock', reason?: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/accounts/${accountId}/action`, { action, reason });
  }

  downloadAuditReport(): Observable<Blob> {
    const token = localStorage.getItem('token');
    return this.http.get(`${this.apiUrl}/audit-report`, {
      responseType: 'blob',
      headers: { 'Authorization': `Bearer ${token}` }
    });
  }

  analyzeUserRisk(userId: number): Observable<any> {
    const token = localStorage.getItem('token');
    return this.http.get(`${this.apiUrl}/ai/analyze-risk/${userId}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });
  }
}
