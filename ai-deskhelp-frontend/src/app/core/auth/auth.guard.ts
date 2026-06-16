import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import {
  clearStoredAuthTokens,
  getStoredAuthToken,
  isUnexpiredJwt,
} from './auth-storage';

export const authGuard: CanActivateFn = () => {
  const router = inject(Router);
  const token = getStoredAuthToken();

  if (token && isUnexpiredJwt(token)) {
    return true;
  }

  clearStoredAuthTokens();
  return router.createUrlTree(['/login']);
};
