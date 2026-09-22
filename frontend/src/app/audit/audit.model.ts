export interface AuditEventItem {
  eventType: string;
  occurredAt: string;
  userId?: string;
  payload?: any;
}