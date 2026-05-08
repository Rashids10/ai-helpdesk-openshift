import { Component, Input, OnInit, inject } from '@angular/core';
import { HelpdeskApiService } from '../../../api/helpdesk-api.service';

@Component({
  selector: 'app-header',
  standalone: true,
  templateUrl: './header.component.html',
  styleUrl: './header.component.css',
})
export class HeaderComponent implements OnInit {
  private readonly api = inject(HelpdeskApiService);

  @Input() title = 'Dashboard overview';
  protected username = 'Signed in';
  protected userRole = 'Support Agent';
  protected isLoadingUsername = true;

  ngOnInit(): void {
    void this.loadUsername();
  }

  protected get avatarInitials(): string {
    const value = this.username.trim();

    if (!value || value === 'Signed in') {
      return 'AD';
    }

    const parts = value.split(/\s+/).filter(Boolean);
    const initials = parts.slice(0, 2).map((part) => part[0]?.toUpperCase() ?? '');

    return initials.join('').slice(0, 2) || 'AD';
  }

  private async loadUsername(): Promise<void> {
    this.isLoadingUsername = true;

    try {
      const username = await this.api.getLoggedInUsername();

      if (username) {
        this.username = username;
      }
    } catch {
      this.username = 'Signed in';
    } finally {
      this.isLoadingUsername = false;
    }
  }
}
