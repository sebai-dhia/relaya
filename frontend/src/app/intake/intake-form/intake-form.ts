import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { IntakeService } from '../intake';

@Component({
  selector: 'app-intake-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './intake-form.html',
  styleUrl: './intake-form.scss'
})
export class IntakeFormComponent {
  private readonly intakeService = inject(IntakeService);
  private readonly router = inject(Router);

  clientLabel = '';
  serviceType = 'WEBSITE_DELIVERY';
  rawText = '';
  submitting = signal(false);
  errorMessage = signal<string | null>(null);

  loadSampleBrief(): void {
    this.clientLabel = 'Northstar Architecture Studio';
    this.rawText = `Hi Relaya Team,
We need a full website refresh for our boutique architecture practice.
Target delivery: Must be live before October 15th for our biennial design showcase.
Requirements:
1. Interactive portfolio gallery showcasing completed commercial & residential projects.
2. Filterable project archive by year and typology.
3. Clean team bio page with high-res headshots.
4. Contact page with enquiry form linked to our internal inbox.
Constraints:
- We have an existing brand guideline book with exact HEX colors and typography.
- Mobile experience must feel silky smooth on iOS Safari.
- Total budget allocated is $8,500 fixed.`;
  }

  onSubmit(): void {
    if (!this.clientLabel || !this.rawText) return;

    this.submitting.set(true);
    this.errorMessage.set(null);

    this.intakeService.createIntake({
      clientLabel: this.clientLabel,
      serviceType: this.serviceType,
      rawText: this.rawText
    }).subscribe({
      next: (res) => {
        // Invalidate cached list so next visit to /intakes re-fetches
        this.intakeService.invalidateListCache();
        this.intakeService.triggerAnalysis(res.intakeId).subscribe({
          next: () => {
            this.submitting.set(false);
            this.router.navigate(['/intakes', res.intakeId, 'review']);
          },
          error: (err) => {
            this.submitting.set(false);
            this.router.navigate(['/intakes', res.intakeId, 'review']);
          }
        });
      },
      error: (err) => {
        this.submitting.set(false);
        this.errorMessage.set(err?.error?.message || 'Failed to create intake brief.');
      }
    });
  }
}