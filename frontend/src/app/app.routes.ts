import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth-guard';
import { adminGuard } from './core/auth/admin-guard';
import { guestGuard } from './core/auth/guest-guard';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    canActivate: [guestGuard],
    loadComponent: () => import('./landing/landing').then((m) => m.LandingComponent)
  },
  {
    path: 'welcome',
    canActivate: [guestGuard],
    loadComponent: () => import('./landing/landing').then((m) => m.LandingComponent)
  },
  {
    path: 'demo',
    canActivate: [guestGuard],
    loadComponent: () => import('./landing/landing').then((m) => m.LandingComponent)
  },
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () => import('./core/auth/login/login').then((m) => m.LoginComponent)
  },
  {
    path: 'intakes',
    canActivate: [authGuard],
    children: [
      {
        path: '',
        loadComponent: () => import('./intake/intake-list/intake-list').then((m) => m.IntakeListComponent)
      },
      {
        path: 'new',
        loadComponent: () => import('./intake/intake-form/intake-form').then((m) => m.IntakeFormComponent)
      },
      {
        path: ':id/review',
        loadComponent: () => import('./review/review-page/review-page').then((m) => m.ReviewPageComponent)
      }
    ]
  },
  {
    path: 'admin',
    canActivate: [adminGuard],
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'usage'
      },
      {
        path: 'usage',
        loadComponent: () => import('./admin/usage-dashboard/usage-dashboard').then((m) => m.UsageDashboardComponent)
      },
      {
        path: 'writes',
        loadComponent: () => import('./admin/failed-writes/failed-writes').then((m) => m.FailedWritesComponent)
      },
      {
        path: 'users',
        loadComponent: () => import('./admin/user-management/user-management').then((m) => m.UserManagementComponent)
      }
    ]
  },
  {
    path: '**',
    redirectTo: 'intakes'
  }
];