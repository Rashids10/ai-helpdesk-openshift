import { Component, Input, computed, inject } from '@angular/core';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-header',
  standalone: true,
  templateUrl: './header.component.html',
  styleUrl: './header.component.css',
})
export class HeaderComponent {
  private readonly auth = inject(AuthService);

  @Input() title = 'Dashboard overview';
  protected readonly username = this.auth.username;
  protected readonly isLoadingUsername = this.auth.isLoadingUsername;
  protected readonly userRole = this.auth.signedInLabel;
  protected readonly avatarInitials = computed(() => {
    const value = this.username().trim();

    if (!value || value === 'User') {
      return 'AD';
    }

    const parts = value.split(/\s+/).filter(Boolean);
    const initials = parts.slice(0, 2).map((part) => part[0]?.toUpperCase() ?? '');

    return initials.join('').slice(0, 2) || 'AD';
  });
}
