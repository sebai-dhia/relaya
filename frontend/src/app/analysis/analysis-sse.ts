import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthService } from '../core/auth/auth';

export interface SseEventPayload {
  status: string;
  draftId?: string;
  error?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AnalysisSseService {
  private readonly authService = inject(AuthService);

  subscribeToAnalysis(intakeId: string): Observable<SseEventPayload> {
    return new Observable<SseEventPayload>((observer) => {
      let eventSource: EventSource | null = null;
      let retryCount = 0;
      const maxRetries = 5;
      let isClosed = false;

      const connect = () => {
        if (isClosed) return;

        this.authService.getSseToken().subscribe({
          next: (tokenRes) => {
            const url = `/api/v1/intakes/${intakeId}/events?sseToken=${tokenRes.sseToken}`;
            eventSource = new EventSource(url);

            eventSource.addEventListener('ANALYSIS_STARTED', (e: MessageEvent) => {
              retryCount = 0;
              observer.next({ status: 'ANALYSIS_PENDING' });
            });

            eventSource.addEventListener('ANALYSIS_COMPLETED', (e: MessageEvent) => {
              try {
                const data = JSON.parse(e.data);
                observer.next({ status: 'ANALYSIS_READY', draftId: data.draftId });
              } catch {
                observer.next({ status: 'ANALYSIS_READY' });
              }
              cleanup();
              observer.complete();
            });

            eventSource.addEventListener('ANALYSIS_FAILED', (e: MessageEvent) => {
              let errorMsg = 'AI extraction failed';
              try {
                const data = JSON.parse(e.data);
                errorMsg = data.error || errorMsg;
              } catch {}
              observer.next({ status: 'ANALYSIS_FAILED', error: errorMsg });
              cleanup();
              observer.complete();
            });

            eventSource.onerror = () => {
              if (eventSource) {
                eventSource.close();
              }
              if (retryCount < maxRetries && !isClosed) {
                const delayMs = Math.min(Math.pow(2, retryCount) * 1000, 10000);
                retryCount++;
                setTimeout(connect, delayMs);
              } else if (!isClosed) {
                observer.error(new Error('SSE connection terminated after maximum retries.'));
              }
            };
          },
          error: (err) => {
            if (!isClosed) {
              observer.error(err);
            }
          }
        });
      };

      const cleanup = () => {
        isClosed = true;
        if (eventSource) {
          eventSource.close();
          eventSource = null;
        }
      };

      connect();
      return cleanup;
    });
  }
}