import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { AdminUserItem, CreateUserRequest, FailedWritesResponse, UsageSummaryResponse } from './admin.model';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private readonly http = inject(HttpClient);

  private readonly _cachedUsage = signal<UsageSummaryResponse | null>(null);
  readonly cachedUsage = this._cachedUsage.asReadonly();

  private readonly _cachedFailedWrites = signal<FailedWritesResponse | null>(null);
  readonly cachedFailedWrites = this._cachedFailedWrites.asReadonly();

  private readonly _cachedUsers = signal<AdminUserItem[] | null>(null);
  readonly cachedUsers = this._cachedUsers.asReadonly();

  getUsageSummary(): Observable<UsageSummaryResponse> {
    return this.http.get<UsageSummaryResponse>('/api/v1/admin/usage').pipe(
      tap(res => this._cachedUsage.set(res))
    );
  }

  getFailedWrites(): Observable<FailedWritesResponse> {
    return this.http.get<FailedWritesResponse>('/api/v1/admin/writes/failed').pipe(
      tap(res => this._cachedFailedWrites.set(res))
    );
  }

  listUsers(): Observable<AdminUserItem[]> {
    return this.http.get<AdminUserItem[]>('/api/v1/admin/users').pipe(
      tap(res => this._cachedUsers.set(res))
    );
  }

  createUser(req: CreateUserRequest): Observable<AdminUserItem> {
    return this.http.post<AdminUserItem>('/api/v1/admin/users', req).pipe(
      tap(() => this._cachedUsers.set(null))
    );
  }

  updateUser(id: string, update: { active?: boolean; role?: string }): Observable<AdminUserItem> {
    return this.http.patch<AdminUserItem>(`/api/v1/admin/users/${id}`, update).pipe(
      tap(() => this._cachedUsers.set(null))
    );
  }
}