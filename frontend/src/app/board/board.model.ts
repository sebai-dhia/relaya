export interface BoardWriteResponse {
  boardWriteId: string;
  status: string;
  destinationBoard: string;
  trelloCardId?: string;
  trelloCardUrl?: string;
  message?: string;
}