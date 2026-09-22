import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../admin';
import { AdminUserItem } from '../admin.model';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './user-management.html',
  styleUrl: './user-management.scss'
})
export class UserManagementComponent implements OnInit {
  private readonly adminService = inject(AdminService);

  readonly users = signal<AdminUserItem[]>([]);
  readonly showCreateModal = signal(false);

  newEmail = '';
  newRole = 'ROLE_REVIEWER';

  ngOnInit(): void {
    const cached = this.adminService.cachedUsers();
    if (cached) {
      this.users.set(cached);
      this.silentRefresh();
    } else {
      this.loadUsers();
    }
  }

  loadUsers(): void {
    this.adminService.listUsers().subscribe({
      next: (list) => this.users.set(list)
    });
  }

  private silentRefresh(): void {
    this.adminService.listUsers().subscribe({
      next: (list) => this.users.set(list),
      error: () => {}
    });
  }

  onCreateUser(): void {
    if (!this.newEmail) return;
    this.adminService.createUser({
      email: this.newEmail,
      role: this.newRole,
      password: 'changeit'
    }).subscribe({
      next: () => {
        this.newEmail = '';
        this.showCreateModal.set(false);
        this.loadUsers();
      }
    });
  }

  toggleActive(user: AdminUserItem): void {
    this.adminService.updateUser(user.id, { active: !user.active }).subscribe({
      next: () => this.loadUsers()
    });
  }
}