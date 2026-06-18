import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { clearStoredAuthTokens } from '../../auth/auth-storage';
import { AuthService } from '../../auth/auth.service';

interface NavigationItem {
  label: string;
  path: string;
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css',
})
export class SidebarComponent {
  private readonly router = inject(Router);
  private readonly auth = inject(AuthService);

  protected readonly navigationItems: NavigationItem[] = [
    { label: 'Dashboard', path: '/dashboard' },
    { label: 'Tickets', path: '/tickets' },
    { label: 'AI Assistant', path: '/chat' },
    { label: 'Settings', path: '/settings' },
  ];

  protected logout(): void {
    clearStoredAuthTokens();
    this.auth.reset();
    void this.router.navigate(['/login'], { replaceUrl: true });
  }
}
