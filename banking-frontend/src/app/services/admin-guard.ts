import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';

export const adminGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const token = localStorage.getItem('token');
  const role = localStorage.getItem('role');

  if (token && role === 'ADMIN') {
    return true;
  } else {
    // normal user to dashboard
    router.navigate(token ? ['/dashboard'] : ['/']);
    return false;
  }
};
