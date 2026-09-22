import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { IntakeService } from '../../intake/intake';
import { IntakeDetails } from '../../intake/intake.model';
import { AnalysisSseService } from '../../analysis/analysis-sse';
import { AnalysisStatusComponent } from '../../analysis/analysis-status/analysis-status';
import { ReviewService } from '../review';
import { AnalysisDraft } from '../review.model';
import { DraftViewerComponent } from '../draft-viewer/draft-viewer';
import { DraftEditorComponent } from '../draft-editor/draft-editor';
import { BoardTriggerComponent } from '../../board/board-trigger/board-trigger';
import { AuditTimelineComponent } from '../../audit/audit-timeline/audit-timeline';
import { AuditEventItem } from '../../audit/audit.model';

@Component({
  selector: 'app-review-page',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    AnalysisStatusComponent,
    DraftViewerComponent,
    DraftEditorComponent,
    BoardTriggerComponent,
    AuditTimelineComponent
  ],
  templateUrl: './review-page.html',
  styleUrl: './review-page.scss'
})
export class ReviewPageComponent implements OnInit, OnDestroy {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly intakeService = inject(IntakeService);
  private readonly reviewService = inject(ReviewService);
  private readonly sseService = inject(AnalysisSseService);

  intakeId = '';
  clientLabelHint = signal<string>('');
  intake = signal<IntakeDetails | null>(null);
  draft = signal<AnalysisDraft | null>(null);
  auditTrail = signal<AuditEventItem[]>([]);
  analysisStatus = signal<string>('');
  analysisError = signal<string | null>(null);
  isEditing = signal(false);
  approving = signal(false);
  approvedApprovalId = signal<string | null>(null);
  hasLoaded = signal(false);
  showDeleteConfirm = signal(false);
  deleting = signal(false);


  private sseSub?: Subscription;
  private pollTimer?: ReturnType<typeof setInterval>;

  ngOnInit(): void {
    this.intakeId = this.route.snapshot.paramMap.get('id') || '';
    if (!this.intakeId) return;

    // Check list cache for immediate client label display (avoids title snap)
    const summary = this.intakeService.cachedList()?.find(i => i.intakeId === this.intakeId);
    if (summary) {
      this.clientLabelHint.set(summary.clientLabel);
      this.analysisStatus.set(summary.status);
    }

    // Check per-intake cache for instant re-entry
    const cachedIntake = this.intakeService.getCachedIntake(this.intakeId);
    const cachedAudit = this.intakeService.getCachedAudit(this.intakeId);
    const cachedDraft = cachedIntake?.draftId ? this.reviewService.getCachedDraft(cachedIntake.draftId) : undefined;

    if (cachedIntake) {
      this.intake.set(cachedIntake);
      this.analysisStatus.set(cachedIntake.status);
      this.hasLoaded.set(true);

      if (cachedDraft) {
        this.draft.set(cachedDraft);
      }
      if (cachedAudit) {
        this.auditTrail.set(cachedAudit);
      }

      if (cachedIntake.status === 'ANALYSIS_PENDING') {
        this.subscribeToSse();
      } else {
        // Silently revalidate in background without wiping current view
        this.silentRevalidate();
      }
    } else {
      this.loadIntakeData();
    }
  }

  loadIntakeData(): void {
    this.intakeService.getIntake(this.intakeId).subscribe({
      next: (details) => {
        this.intake.set(details);
        this.analysisStatus.set(details.status);

        if (details.status === 'ANALYSIS_PENDING') {
          this.hasLoaded.set(true);
          this.subscribeToSse();
        } else {
          this.stopPoll();
          if (details.draftId) {
            this.reviewService.getDraft(details.draftId).subscribe({
              next: (d) => {
                this.draft.set(d);
                this.hasLoaded.set(true);
              },
              error: () => this.hasLoaded.set(true)
            });
          } else {
            this.hasLoaded.set(true);
          }
        }
        this.loadAudit();
      },
      error: () => {
        this.hasLoaded.set(true);
      }
    });
  }

  private silentRevalidate(): void {
    this.intakeService.getIntake(this.intakeId).subscribe({
      next: (details) => {
        this.intake.set(details);
        this.analysisStatus.set(details.status);
        if (details.draftId) {
          this.reviewService.getDraft(details.draftId).subscribe({
            next: (d) => this.draft.set(d)
          });
        }
      }
    });

    this.intakeService.getAuditTrail(this.intakeId).subscribe({
      next: (res) => this.auditTrail.set(res.events)
    });
  }

  loadAudit(): void {
    this.intakeService.getAuditTrail(this.intakeId).subscribe({
      next: (res) => this.auditTrail.set(res.events)
    });
  }

  subscribeToSse(): void {
    this.sseSub?.unsubscribe();
    this.sseSub = this.sseService.subscribeToAnalysis(this.intakeId).subscribe({
      next: (event) => {
        this.analysisStatus.set(event.status);
        if (event.draftId) {
          this.reviewService.getDraft(event.draftId).subscribe({
            next: (d) => this.draft.set(d)
          });
          this.loadIntakeData();
        }
        if (event.error) {
          this.analysisError.set(event.error);
        }
      },
      complete: () => this.stopPoll()
    });

    setTimeout(() => this.pollIfStillPending(), 1500);
    this.startPoll();
  }

  private startPoll(): void {
    this.stopPoll();
    this.pollTimer = setInterval(() => this.pollIfStillPending(), 3000);
  }

  private stopPoll(): void {
    if (this.pollTimer !== undefined) {
      clearInterval(this.pollTimer);
      this.pollTimer = undefined;
    }
  }

  private pollIfStillPending(): void {
    if (this.analysisStatus() !== 'ANALYSIS_PENDING') {
      this.stopPoll();
      return;
    }
    this.intakeService.getIntake(this.intakeId).subscribe({
      next: (details) => {
        if (details.status !== 'ANALYSIS_PENDING') {
          this.stopPoll();
          this.sseSub?.unsubscribe();
          this.intake.set(details);
          this.analysisStatus.set(details.status);
          if (details.draftId) {
            this.reviewService.getDraft(details.draftId).subscribe({
              next: (d) => this.draft.set(d)
            });
          }
          this.loadAudit();
        }
      }
    });
  }

  onRetryAnalysis(): void {
    this.intakeService.triggerAnalysis(this.intakeId).subscribe({
      next: () => {
        this.analysisStatus.set('ANALYSIS_PENDING');
        this.analysisError.set(null);
        this.subscribeToSse();
      }
    });
  }

  onDraftSaved(): void {
    this.isEditing.set(false);
    this.loadIntakeData();
  }

  onApprove(): void {
    const d = this.draft();
    if (!d) return;

    this.approving.set(true);
    this.reviewService.approveDraft(d.draftId).subscribe({
      next: (res) => {
        // Optimistically update signals synchronously so approving -> false and isApproved -> true
        // occur in the exact same render cycle, completely eliminating button flicker/jitter
        this.draft.update((current) =>
          current ? { ...current, isApproved: true, approvalInvalidated: false } : null
        );
        this.intake.update((current) =>
          current ? { ...current, status: 'APPROVED', approvalId: res.approvalId } : null
        );
        this.approvedApprovalId.set(res.approvalId);
        this.approving.set(false);

        this.intakeService.invalidateListCache();
        this.silentRevalidate();
      },
      error: () => this.approving.set(false)
    });
  }

  confirmDelete(): void {
    if (!this.intakeId) return;

    this.deleting.set(true);
    this.intakeService.deleteIntake(this.intakeId).subscribe({
      next: () => {
        this.deleting.set(false);
        this.showDeleteConfirm.set(false);
        this.router.navigate(['/intakes']);
      },
      error: () => {
        this.deleting.set(false);
      }
    });
  }

  ngOnDestroy(): void {
    this.sseSub?.unsubscribe();
    this.stopPoll();
  }
}