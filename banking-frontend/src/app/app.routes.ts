import { Routes } from '@angular/router';
import { Login } from './components/login/login';
import { Dashboard } from './components/dashboard/dashboard';
import { Transfer } from './components/transfer/transfer';
import { History } from './components/history/history';
import { ScheduledList } from './components/scheduled-list/scheduled-list';
import { ScheduleNew } from './components/schedule-new/schedule-new';
import { OpenAccountComponent } from './components/open-account/open-account';
import { Register } from './components/register/register';
import { authGuard } from './services/auth-guard';

export const routes: Routes = [
  { path: '', component: Login },
  { path: 'dashboard', component: Dashboard, canActivate: [authGuard] },
  { path: 'transfer', component: Transfer, canActivate: [authGuard] },
  { path: 'history', component: History, canActivate: [authGuard] },
  { path: 'scheduled-list', component: ScheduledList, canActivate: [authGuard] },
  { path: 'schedule-new', component: ScheduleNew, canActivate: [authGuard] },
  { path: 'open-account', component: OpenAccountComponent },
  { path: 'register', component: Register }
];
