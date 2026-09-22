import { Component, computed, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { formatStatus } from '../../core/utils/display-labels';

@Component({
  selector: 'app-analysis-status',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './analysis-status.html',
  styleUrl: './analysis-status.scss'
})
export class AnalysisStatusComponent {
  readonly status = input.required<string>();
  readonly errorMessage = input<string | null>(null);
  readonly retryTrigger = output<void>();
  readonly formatStatus = formatStatus;

  protected readonly VISIBLE_STATES = new Set(['ANALYSIS_PENDING', 'ANALYSIS_FAILED']);
  readonly shouldShow = computed(() => this.VISIBLE_STATES.has(this.status()));
}