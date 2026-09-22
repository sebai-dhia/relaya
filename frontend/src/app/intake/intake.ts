import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import {
  AuditEventItem,
  CreateIntakeRequest,
  CreateIntakeResponse,
  IntakeAuditTrailResponse,
  IntakeDetails,
  IntakeSummary,
  PaginatedIntakesResponse
} from './intake.model';

@Injectable({
  providedIn: 'root'
})
export class IntakeService {
  private readonly http = inject(HttpClient);

  /** Last successful list result — used by IntakeListComponent for instant re-entry */
  private readonly _cachedList = signal<IntakeSummary[] | null>(null);
  readonly cachedList = this._cachedList.asReadonly();

  /** Per-intake detail cache for instant review page navigation */
  private readonly intakeCache = new Map<string, IntakeDetails>();
  /** Per-intake audit trail cache */
  private readonly auditCache = new Map<string, AuditEventItem[]>();

  createIntake(request: CreateIntakeRequest): Observable<CreateIntakeResponse> {
    return this.http.post<CreateIntakeResponse>('/api/v1/intakes', request);
  }

  listIntakes(page = 0, size = 20, status?: string): Observable<PaginatedIntakesResponse> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (status) {
      params = params.set('status', status);
    }
    return this.http.get<PaginatedIntakesResponse>('/api/v1/intakes', { params }).pipe(
      tap(res => this._cachedList.set(res.content))
    );
  }

  /** Invalidate the list cache — call after creating a new intake so next list visit re-fetches */
  invalidateListCache(): void {
    this._cachedList.set(null);
  }

  getIntake(id: string): Observable<IntakeDetails> {
    return this.http.get<IntakeDetails>(`/api/v1/intakes/${id}`).pipe(
      tap(details => this.intakeCache.set(id, details))
    );
  }

  getCachedIntake(id: string): IntakeDetails | undefined {
    return this.intakeCache.get(id);
  }

  getAuditTrail(id: string): Observable<IntakeAuditTrailResponse> {
    return this.http.get<IntakeAuditTrailResponse>(`/api/v1/intakes/${id}/audit`).pipe(
      tap(res => this.auditCache.set(id, res.events))
    );
  }

  getCachedAudit(id: string): AuditEventItem[] | undefined {
    return this.auditCache.get(id);
  }

  invalidateIntakeCache(id?: string): void {
    if (id) {
      this.intakeCache.delete(id);
      this.auditCache.delete(id);
    } else {
      this.intakeCache.clear();
      this.auditCache.clear();
    }
  }

  deleteIntake(id: string): Observable<void> {
    return this.http.delete<void>(`/api/v1/intakes/${id}`).pipe(
      tap(() => {
        this.invalidateIntakeCache(id);
        this.invalidateListCache();
      })
    );
  }

  triggerAnalysis(id: string): Observable<{ message: string; status: string }> {
    return this.http.post<{ message: string; status: string }>(`/api/v1/intakes/${id}/analyze`, {});
  }
}