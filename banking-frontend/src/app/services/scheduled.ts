import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface ScheduledTransaction {
  id: number;
  fromAccount: string;
  toAccount: string;
  amount: number;
  scheduledTime: string;
  recurring: boolean;
  recurrenceType: string;
  status: string;
  lastExecuted: string;
}

export interface ScheduleRequest {
  fromAcc: string;
  toAcc: string;
  amount: number;
  scheduledTime: string;
  recurring: boolean;
  recurrenceType: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class ScheduledService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/scheduled`;

  getMySchedules(): Observable<ScheduledTransaction[]> {
    return this.http.get<ScheduledTransaction[]>(this.apiUrl);
  }

  createSchedule(req: ScheduleRequest): Observable<any> {
    return this.http.post(this.apiUrl, req);
  }

  cancelSchedule(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }
}
