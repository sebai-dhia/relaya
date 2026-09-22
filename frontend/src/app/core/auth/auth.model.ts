export interface User {
  id: string;
  email: string;
  role: string;
  tenantId: string;
}

export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
  expiresInSeconds: number;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  expiresInSeconds: number;
  user: User;
}

export interface RefreshResponse {
  accessToken: string;
  expiresInSeconds: number;
}

export interface SseTokenResponse {
  sseToken: string;
  expiresInSeconds: number;
}