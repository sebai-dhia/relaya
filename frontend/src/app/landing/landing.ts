import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../core/auth/auth';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './landing.html',
  styleUrl: './landing.scss'
})
export class LandingComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly activeStep = signal<1 | 2 | 3 | 4>(1);
  readonly loadingRole = signal<'reviewer' | 'admin' | null>(null);
  readonly errorMessage = signal<string | null>(null);
  readonly mobileNavOpen = signal<boolean>(false);

  ngOnInit(): void {
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/intakes']);
    }
  }

  setStep(step: 1 | 2 | 3 | 4): void {
    this.activeStep.set(step);
  }

  toggleMobileNav(): void {
    this.mobileNavOpen.update(v => !v);
  }

  closeMobileNav(): void {
    this.mobileNavOpen.set(false);
  }

  launchDemo(role: 'reviewer' | 'admin'): void {
    this.errorMessage.set(null);
    this.loadingRole.set(role);

    const email = role === 'admin' ? 'admin@relaya.demo' : 'reviewer@relaya.demo';
    const password = 'changeit';

    this.authService.login({ email, password }).subscribe({
      next: () => {
        if (role === 'admin') {
          this.router.navigate(['/admin/usage']);
        } else {
          this.router.navigate(['/intakes']);
        }
      },
      error: () => {
        this.loadingRole.set(null);
        this.errorMessage.set('Demo authentication failed. Please launch via Sign In.');
      }
    });
  }
}
