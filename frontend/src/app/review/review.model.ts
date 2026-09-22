export interface SourceExcerpt {
  text: string;
  sourceExcerpt: string;
}

export interface TaskProposal {
  title: string;
  estimateDays: number;
}

export interface AnalysisDraft {
  draftId: string;
  intakeId: string;
  contentHash: string;
  objectives: SourceExcerpt[];
  deliverables: SourceExcerpt[];
  constraints: SourceExcerpt[];
  timelineNotes?: string;
  budgetNotes?: string;
  unknowns: string[];
  proposedTasks: TaskProposal[];
  isApproved: boolean;
  approvalInvalidated: boolean;
}

export interface UpdateDraftRequest {
  objectives: SourceExcerpt[];
  deliverables: SourceExcerpt[];
  constraints: SourceExcerpt[];
  timelineNotes?: string;
  budgetNotes?: string;
  unknowns: string[];
  proposedTasks: TaskProposal[];
}

export interface UpdateDraftResponse {
  draftId: string;
  contentHash: string;
  approvalInvalidated: boolean;
  updatedAt: string;
}

export interface ApprovalResponse {
  approvalId: string;
  draftId: string;
  contentHashAtApproval: string;
  approvedAt: string;
}