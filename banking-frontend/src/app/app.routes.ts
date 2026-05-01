import { Routes } from '@angular/router';
import { Login } from './components/login/login';
import { Dashboard } from './components/dashboard/dashboard';
import { Transfer } from './components/transfer/transfer';
import { authGuard } from './services/auth-guard';

export const routes: Routes = [
  { path: '', component: Login },
  { path: 'dashboard', component: Dashboard, canActivate: [authGuard] },
  { path: 'transfer', component: Transfer, canActivate: [authGuard] }
];
