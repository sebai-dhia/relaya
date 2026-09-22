import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { AnalysisDraft, ApprovalResponse, UpdateDraftRequest, UpdateDraftResponse } from './review.model';

@Injectable({
  providedIn: 'root'
})
export class ReviewService {
  private readonly http = inject(HttpClient);
  private readonly draftCache = new Map<string, AnalysisDraft>();

  getDraft(draftId: string): Observable<AnalysisDraft> {
    return this.http.get<AnalysisDraft>(`/api/v1/drafts/${draftId}`).pipe(
      tap(draft => this.draftCache.set(draftId, draft))
    );
  }

  getCachedDraft(draftId: string): AnalysisDraft | undefined {
    return this.draftCache.get(draftId);
  }

  updateDraft(draftId: string, request: UpdateDraftRequest): Observable<UpdateDraftResponse> {
    return this.http.put<UpdateDraftResponse>(`/api/v1/drafts/${draftId}`, request).pipe(
      tap(() => this.draftCache.delete(draftId))
    );
  }

  approveDraft(draftId: string): Observable<ApprovalResponse> {
    return this.http.post<ApprovalResponse>(`/api/v1/drafts/${draftId}/approve`, {}).pipe(
      tap(() => this.draftCache.delete(draftId))
    );
  }

  invalidateDraftCache(draftId?: string): void {
    if (draftId) {
      this.draftCache.delete(draftId);
    } else {
      this.draftCache.clear();
    }
  }
}