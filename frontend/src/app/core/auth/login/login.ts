import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../auth';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class LoginComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  email = 'reviewer@relaya.demo';
  password = 'changeit';
  loading = signal(false);
  errorMessage = signal<string | null>(null);

  fillCreds(targetEmail: string): void {
    this.email = targetEmail;
    this.password = 'changeit';
  }

  onSubmit(): void {
    if (!this.email || !this.password) return;
    this.loading.set(true);
    this.errorMessage.set(null);

    this.authService.login({ email: this.email, password: this.password }).subscribe({
      next: () => {
        this.loading.set(false);
        this.router.navigate(['/intakes']);
      },
      error: (err) => {
        this.loading.set(false);
        const msg = err?.error?.message || 'Login failed. Check your credentials.';
        this.errorMessage.set(msg);
      }
    });
  }
}