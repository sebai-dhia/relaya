export interface IntakeSummary {
  intakeId: string;
  clientLabel: string;
  status: string;
  createdAt: string;
}

export interface PaginatedIntakesResponse {
  content: IntakeSummary[];
  page: number;
  size: number;
  totalElements: number;
}

export interface CreateIntakeRequest {
  clientLabel: string;
  serviceType: string;
  rawText: string;
}

export interface CreateIntakeResponse {
  intakeId: string;
  version: number;
  status: string;
  createdAt: string;
}

export interface IntakeDetails {
  intakeId: string;
  clientLabel: string;
  status: string;
  currentVersion: number;
  draftId?: string;
  approvalId?: string;
  approvalInvalidated: boolean;
}

export interface AuditEventItem {
  eventType: string;
  occurredAt: string;
  userId?: string;
  payload?: any;
}

export interface IntakeAuditTrailResponse {
  intakeId: string;
  events: AuditEventItem[];
}