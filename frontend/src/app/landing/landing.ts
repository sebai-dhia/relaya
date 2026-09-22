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
      return;
    }

    // Scroll to section if opened with hash (e.g. #pipeline, #architecture)
    if (typeof window !== 'undefined' && window.location.hash) {
      const targetId = window.location.hash.replace('#', '');
      setTimeout(() => this.performScroll(targetId), 100);
    }
  }

  scrollToSection(event: Event, sectionId: string): void {
    event.preventDefault();
    this.performScroll(sectionId);
    if (typeof window !== 'undefined' && window.history) {
      window.history.replaceState(null, '', `#${sectionId}`);
    }
  }

  private performScroll(sectionId: string): void {
    if (typeof document === 'undefined') return;
    const element = document.getElementById(sectionId);
    if (element) {
      element.scrollIntoView({ behavior: 'smooth', block: 'start' });
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
    const targetRole = role === 'admin' ? 'ROLE_ADMIN' : 'ROLE_REVIEWER';

    // If already authenticated with the desired role, navigate immediately with 0 latency
    if (this.authService.isAuthenticated() && this.authService.currentUser()?.role === targetRole) {
      if (role === 'admin') {
        this.router.navigate(['/admin/usage']);
      } else {
        this.router.navigate(['/intakes']);
      }
      return;
    }

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
