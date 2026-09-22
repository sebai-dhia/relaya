import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth';

/**
 * Guard for guest-only routes (landing page, login).
 * If the user already has an active session, redirects directly to the dashboard (/intakes)
 * with zero DOM rendering or module download delay.
 */
export const guestGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    return router.parseUrl('/intakes');
  }
  return true;
};
