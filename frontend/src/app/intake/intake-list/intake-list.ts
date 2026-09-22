import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { IntakeService } from '../intake';
import { IntakeSummary } from '../intake.model';
import { formatStatus } from '../../core/utils/display-labels';

@Component({
  selector: 'app-intake-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './intake-list.html',
  styleUrl: './intake-list.scss'
})
export class IntakeListComponent implements OnInit {
  private readonly intakeService = inject(IntakeService);
  readonly formatStatus = formatStatus;

  readonly intakes = signal<IntakeSummary[]>([]);
  readonly loading = signal(true);

  ngOnInit(): void {
    const cached = this.intakeService.cachedList();

    if (cached !== null) {
      // Instant display — show stale data immediately, no skeleton
      this.intakes.set(cached);
      this.loading.set(false);
      // Silently refresh in background so data stays current
      this.silentRefresh();
    } else {
      // First visit — show skeleton until data arrives
      this.loadIntakes();
    }
  }

  loadIntakes(): void {
    this.loading.set(true);
    this.intakeService.listIntakes().subscribe({
      next: (res) => {
        this.intakes.set(res.content);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  private silentRefresh(): void {
    this.intakeService.listIntakes().subscribe({
      next: (res) => this.intakes.set(res.content),
      error: () => {} // silent — stale data is still valid, don't surface error
    });
  }

  getStatusBadgeClass(status: string): string {
    switch (status) {
      case 'ANALYSIS_READY': return 'badge-ready';
      case 'ANALYSIS_PENDING':
      case 'WRITE_PENDING': return 'badge-pending';
      case 'APPROVED': return 'badge-approved';
      case 'WRITE_SUCCESS': return 'badge-success';
      case 'ANALYSIS_FAILED':
      case 'WRITE_FAILED': return 'badge-failed';
      default: return 'badge-neutral';
    }
  }

  getRowCtaLabel(status: string): string {
    switch (status) {
      case 'INTAKE_SUBMITTED': return 'Start Analysis →';
      case 'ANALYSIS_PENDING': return 'Analyzing…';
      case 'ANALYSIS_FAILED':  return 'Retry Analysis →';
      case 'WRITE_PENDING':    return 'Writing to Board…';
      case 'WRITE_SUCCESS':    return 'View Card →';
      case 'WRITE_FAILED':     return 'Resolve Write →';
      default:                 return 'Review Draft →';
    }
  }

  isCtaDisabled(status: string): boolean {
    return status === 'ANALYSIS_PENDING' || status === 'WRITE_PENDING';
  }
}