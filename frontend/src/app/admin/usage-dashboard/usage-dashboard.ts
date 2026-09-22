import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService } from '../admin';
import { UsageSummaryResponse } from '../admin.model';

@Component({
  selector: 'app-usage-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './usage-dashboard.html',
  styleUrl: './usage-dashboard.scss'
})
export class UsageDashboardComponent implements OnInit {
  private readonly adminService = inject(AdminService);

  readonly usage = signal<UsageSummaryResponse | null>(null);
  readonly loading = signal(true);

  ngOnInit(): void {
    const cached = this.adminService.cachedUsage();
    if (cached) {
      this.usage.set(cached);
      this.loading.set(false);
      this.silentRefresh();
    } else {
      this.loadUsage();
    }
  }

  loadUsage(): void {
    this.loading.set(true);
    this.adminService.getUsageSummary().subscribe({
      next: (data) => {
        this.usage.set(data);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  private silentRefresh(): void {
    this.adminService.getUsageSummary().subscribe({
      next: (data) => this.usage.set(data),
      error: () => {}
    });
  }
}