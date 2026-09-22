import { Component, computed, input, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuditEventItem } from '../audit.model';
import { formatEventType } from '../../core/utils/display-labels';

export interface FormattedPayload {
  type: 'badge' | 'error' | 'text';
  text: string;
}

@Component({
  selector: 'app-audit-timeline',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './audit-timeline.html',
  styleUrl: './audit-timeline.scss'
})
export class AuditTimelineComponent {
  readonly events = input.required<AuditEventItem[]>();
  readonly formatEventType = formatEventType;

  readonly expanded = signal(false);
  readonly visibleEvents = computed(() =>
    this.expanded() ? this.events() : this.events().slice(0, 3)
  );

  get hasMore(): boolean {
    return this.events().length > 3;
  }

  toggleExpand(): void {
    this.expanded.set(!this.expanded());
  }

  formatPayload(payload: any): FormattedPayload | null {
    if (!payload) return null;

    let parsed = payload;
    if (typeof payload === 'string') {
      try {
        parsed = JSON.parse(payload);
      } catch {
        return { type: 'text', text: payload };
      }
    }

    if (typeof parsed === 'object' && parsed !== null) {
      if (parsed.error) {
        return { type: 'error', text: String(parsed.error) };
      }
      if (parsed.mode === 'STUB') {
        return { type: 'badge', text: 'Stub Connector · Demo Mode' };
      }
      const entries = Object.entries(parsed).map(([k, v]) => `${k}: ${v}`).join(' · ');
      return { type: 'badge', text: entries };
    }

    return { type: 'text', text: String(parsed) };
  }
}