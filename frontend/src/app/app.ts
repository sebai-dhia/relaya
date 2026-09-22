import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from './core/auth/auth';
import { formatRole } from './core/utils/display-labels';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class AppComponent {
  readonly authService = inject(AuthService);
  readonly formatRole = formatRole;
  private readonly router = inject(Router);

  readonly navOpen = signal(false);

  toggleNav(): void {
    this.navOpen.set(!this.navOpen());
  }

  closeNav(): void {
    this.navOpen.set(false);
  }

  getUserInitial(): string {
    const email = this.authService.currentUser()?.email;
    return email ? email.charAt(0).toUpperCase() : 'U';
  }

  getUsername(): string {
    const email = this.authService.currentUser()?.email;
    if (!email) return 'user';
    return email.split('@')[0];
  }

  onLogout(): void {
    this.closeNav();
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}