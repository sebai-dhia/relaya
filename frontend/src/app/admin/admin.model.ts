export interface UsageModelBreakdown {
  model: string;
  calls: number;
  promptTokens: number;
  completionTokens: number;
  costUsd: number;
}

export interface UsageSummaryResponse {
  billingCycle: string;
  totalSpentUsd: number;
  budgetLimitUsd: number;
  remainingUsd: number;
  totalTokens: number;
  breakdownByModel: UsageModelBreakdown[];
}

export interface FailedWriteItem {
  boardWriteId: string;
  approvalId: string;
  intakeClientLabel: string;
  status: string;
  errorDetails?: string;
  createdAt: string;
}

export interface FailedWritesResponse {
  content: FailedWriteItem[];
}

export interface AdminUserItem {
  id: string;
  email: string;
  role: string;
  active: boolean;
  createdAt: string;
}

export interface CreateUserRequest {
  email: string;
  password?: string;
  role: string;
}