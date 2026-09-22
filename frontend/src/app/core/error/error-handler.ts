import { Injectable, computed, signal } from '@angular/core';

export interface ToastMessage {
  id: string;
  type: 'error' | 'success' | 'info';
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class ErrorHandlerService {
  private readonly _toasts = signal<ToastMessage[]>([]);
  readonly toasts = computed(() => this._toasts());

  showError(message: string): void {
    this.addToast('error', message);
  }

  showSuccess(message: string): void {
    this.addToast('success', message);
  }

  dismiss(id: string): void {
    this._toasts.update((items) => items.filter((t) => t.id !== id));
  }

  private addToast(type: 'error' | 'success' | 'info', message: string): void {
    const id = Math.random().toString(36).substring(2, 9);
    this._toasts.update((items) => [...items, { id, type, message }]);
    setTimeout(() => this.dismiss(id), 5000);
  }
}