import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AnalysisDraft } from '../review.model';

@Component({
  selector: 'app-draft-viewer',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './draft-viewer.html',
  styleUrl: './draft-viewer.scss'
})
export class DraftViewerComponent {
  readonly draft = input.required<AnalysisDraft>();

  truncateHash(hash: string | undefined | null): string {
    return hash ? hash.substring(0, 12) + '…' : '';
  }
}