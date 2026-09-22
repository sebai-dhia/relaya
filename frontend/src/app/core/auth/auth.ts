import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { AuthTokens, LoginResponse, RefreshResponse, SseTokenResponse, User } from './auth.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly _currentUser = signal<User | null>(this.getStoredUser());
  private readonly _accessToken = signal<string | null>(localStorage.getItem('relaya_access_token'));
  private readonly _refreshToken = signal<string | null>(localStorage.getItem('relaya_refresh_token'));

  readonly currentUser = computed(() => this._currentUser());
  readonly accessToken = computed(() => this._accessToken());
  readonly isAuthenticated = computed(() => !!this._currentUser() && !!this._accessToken());
  readonly isAdmin = computed(() => this._currentUser()?.role === 'ROLE_ADMIN');

  login(credentials: { email: string; password: string }): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('/api/v1/auth/login', credentials).pipe(
      tap((res) => {
        this._accessToken.set(res.accessToken);
        this._refreshToken.set(res.refreshToken);
        this._currentUser.set(res.user);
        localStorage.setItem('relaya_access_token', res.accessToken);
        localStorage.setItem('relaya_refresh_token', res.refreshToken);
        localStorage.setItem('relaya_user', JSON.stringify(res.user));
      })
    );
  }

  refreshToken(): Observable<RefreshResponse> {
    const refreshToken = this._refreshToken();
    return this.http.post<RefreshResponse>('/api/v1/auth/refresh', { refreshToken }).pipe(
      tap((res) => {
        this._accessToken.set(res.accessToken);
        localStorage.setItem('relaya_access_token', res.accessToken);
      })
    );
  }

  getSseToken(): Observable<SseTokenResponse> {
    return this.http.post<SseTokenResponse>('/api/v1/auth/sse-token', {});
  }

  logout(): void {
    const refreshToken = this._refreshToken();
    if (refreshToken) {
      this.http.post('/api/v1/auth/logout', { refreshToken }).subscribe({
        error: () => {}
      });
    }
    this._accessToken.set(null);
    this._refreshToken.set(null);
    this._currentUser.set(null);
    localStorage.removeItem('relaya_access_token');
    localStorage.removeItem('relaya_refresh_token');
    localStorage.removeItem('relaya_user');
    this.router.navigate(['/login']);
  }

  private getStoredUser(): User | null {
    try {
      const data = localStorage.getItem('relaya_user');
      return data ? JSON.parse(data) : null;
    } catch {
      return null;
    }
  }
}