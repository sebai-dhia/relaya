import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService } from '../admin';
import { FailedWriteItem } from '../admin.model';

@Component({
  selector: 'app-failed-writes',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './failed-writes.html',
  styleUrl: './failed-writes.scss'
})
export class FailedWritesComponent implements OnInit {
  private readonly adminService = inject(AdminService);

  readonly writes = signal<FailedWriteItem[]>([]);
  readonly loading = signal(true);

  ngOnInit(): void {
    const cached = this.adminService.cachedFailedWrites();
    if (cached) {
      this.writes.set(cached.content);
      this.loading.set(false);
      this.silentRefresh();
    } else {
      this.loadWrites();
    }
  }

  loadWrites(): void {
    this.loading.set(true);
    this.adminService.getFailedWrites().subscribe({
      next: (res) => {
        this.writes.set(res.content);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  private silentRefresh(): void {
    this.adminService.getFailedWrites().subscribe({
      next: (res) => this.writes.set(res.content),
      error: () => {}
    });
  }
}