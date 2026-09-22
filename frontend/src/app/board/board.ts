import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BoardWriteResponse } from './board.model';

@Injectable({
  providedIn: 'root'
})
export class BoardService {
  private readonly http = inject(HttpClient);

  triggerBoardWrite(approvalId: string): Observable<BoardWriteResponse> {
    return this.http.post<BoardWriteResponse>(`/api/v1/approvals/${approvalId}/write`, {});
  }
}