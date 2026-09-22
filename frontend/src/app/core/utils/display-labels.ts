/**
 * Display label mapping utilities for Relaya.
 * Pure TypeScript functions - zero Angular framework coupling.
 */

const STATUS_LABELS: Record<string, string> = {
  INTAKE_SUBMITTED: 'Submitted',
  ANALYSIS_PENDING: 'Analyzing Scope...',
  ANALYSIS_READY: 'Ready for Review',
  ANALYSIS_FAILED: 'Analysis Failed',
  DRAFT_EDITED: 'Draft Edited',
  APPROVED: 'Scope Approved',
  WRITE_PENDING: 'Pushing to Board...',
  WRITE_SUCCESS: 'Card Created',
  WRITE_FAILED: 'Write Failed',
  INVALIDATED: 'Approval Invalidated'
};

const BOARD_STATUS_LABELS: Record<string, string> = {
  PENDING: 'Pushing...',
  SUCCESS: 'Card Created',
  FAILED: 'Write Failed',
  STUB: 'Stub Mode'
};

const ROLE_LABELS: Record<string, string> = {
  ROLE_REVIEWER: 'Reviewer',
  ROLE_ADMIN: 'Admin'
};

const EVENT_TYPE_LABELS: Record<string, string> = {
  INTAKE_SUBMITTED: 'Brief submitted',
  ANALYSIS_STARTED: 'Analysis started',
  ANALYSIS_COMPLETED: 'Draft generated',
  ANALYSIS_FAILED: 'Analysis failed',
  DRAFT_EDITED: 'Draft edited',
  DRAFT_APPROVED: 'Scope approved',
  APPROVAL_INVALIDATED: 'Approval invalidated',
  BOARD_WRITE_INITIATED: 'Board write started',
  BOARD_WRITE_COMPLETED: 'Card created on Trello',
  BOARD_WRITE_FAILED: 'Board write failed'
};

export function formatStatus(status: string | null | undefined): string {
  if (!status) return '';
  return STATUS_LABELS[status] ?? status;
}

export function formatBoardStatus(status: string | null | undefined): string {
  if (!status) return '';
  return BOARD_STATUS_LABELS[status] ?? status;
}

export function formatRole(role: string | null | undefined): string {
  if (!role) return '';
  return ROLE_LABELS[role] ?? role;
}

export function formatEventType(type: string | null | undefined): string {
  if (!type) return '';
  return EVENT_TYPE_LABELS[type] ?? type;
}
