import { Component, OnInit, inject, input, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReviewService } from '../review';
import { AnalysisDraft, UpdateDraftRequest } from '../review.model';

@Component({
  selector: 'app-draft-editor',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './draft-editor.html',
  styleUrl: './draft-editor.scss'
})
export class DraftEditorComponent implements OnInit {
  private readonly reviewService = inject(ReviewService);

  readonly draft = input.required<AnalysisDraft>();
  readonly draftSaved = output<void>();
  readonly cancelEdit = output<void>();
  readonly deleteTrigger = output<void>();

  deliverablesText = '';
  objectivesText = '';
  timelineNotes = '';
  budgetNotes = '';
  saving = signal(false);

  ngOnInit(): void {
    const d = this.draft();
    this.deliverablesText = d.deliverables.map((x) => x.text).join('\n');
    this.objectivesText = d.objectives.map((x) => x.text).join('\n');
    this.timelineNotes = d.timelineNotes || '';
    this.budgetNotes = d.budgetNotes || '';
  }

  onSave(): void {
    this.saving.set(true);
    const d = this.draft();

    const newDeliverables = this.deliverablesText
      .split('\n')
      .map((s) => s.trim())
      .filter((s) => s.length > 0)
      .map((text) => ({ text, sourceExcerpt: '' }));

    const newObjectives = this.objectivesText
      .split('\n')
      .map((s) => s.trim())
      .filter((s) => s.length > 0)
      .map((text) => ({ text, sourceExcerpt: '' }));

    const req: UpdateDraftRequest = {
      objectives: newObjectives,
      deliverables: newDeliverables,
      constraints: d.constraints,
      timelineNotes: this.timelineNotes,
      budgetNotes: this.budgetNotes,
      unknowns: d.unknowns,
      proposedTasks: d.proposedTasks
    };

    this.reviewService.updateDraft(d.draftId, req).subscribe({
      next: () => {
        this.saving.set(false);
        this.draftSaved.emit();
      },
      error: () => this.saving.set(false)
    });
  }
}