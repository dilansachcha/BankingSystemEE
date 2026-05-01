import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';

export const authGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const token = localStorage.getItem('token'); // Check

  if (token) {
    return true; //in
  } else {
    router.navigate(['/']); //back to the login
    return false;
  }
};
