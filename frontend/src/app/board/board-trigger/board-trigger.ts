import { Component, effect, inject, input, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BoardService } from '../board';
import { BoardWriteResponse } from '../board.model';
import { formatBoardStatus } from '../../core/utils/display-labels';

@Component({
  selector: 'app-board-trigger',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './board-trigger.html',
  styleUrl: './board-trigger.scss'
})
export class BoardTriggerComponent {
  private readonly boardService = inject(BoardService);
  readonly formatBoardStatus = formatBoardStatus;

  readonly approvalId = input<string | null>(null);
  readonly canWrite = input<boolean>(false);
  readonly intakeStatus = input<string | null>(null);

  writing = signal(false);
  writeResult = signal<BoardWriteResponse | null>(null);

  constructor() {
    effect(() => {
      const status = this.intakeStatus();
      if (status === 'WRITE_SUCCESS' && !this.writeResult()) {
        this.writeResult.set({
          boardWriteId: 'Verified',
          status: 'STUB',
          destinationBoard: 'TRELLO',
          message: 'Card created on dedicated Trello demo board (Stub mode).'
        });
      }
    });
  }

  onTriggerWrite(): void {
    const id = this.approvalId();
    if (!id) {
      this.writeResult.set({
        boardWriteId: '',
        status: 'WRITE_FAILED',
        destinationBoard: 'TRELLO',
        message: 'Cannot write to Trello: Scope must be approved first.'
      });
      return;
    }

    this.writing.set(true);
    this.boardService.triggerBoardWrite(id).subscribe({
      next: (res) => {
        this.writing.set(false);
        this.writeResult.set(res);
      },
      error: (err) => {
        this.writing.set(false);
        this.writeResult.set({
          boardWriteId: '',
          status: 'WRITE_FAILED',
          destinationBoard: 'TRELLO',
          message: err?.error?.message || 'Board write failed.'
        });
      }
    });
  }
}