import { Injectable, computed, inject, signal } from '@angular/core';
import { HelpdeskApiService } from '../../api/helpdesk-api.service';

const USERNAME_FALLBACK = 'User';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly api = inject(HelpdeskApiService);
  private readonly usernameSignal = signal(USERNAME_FALLBACK);
  private readonly isLoadingUsernameSignal = signal(false);
  private loadPromise: Promise<void> | null = null;

  readonly username = this.usernameSignal.asReadonly();
  readonly isLoadingUsername = this.isLoadingUsernameSignal.asReadonly();
  readonly signedInLabel = computed(() => `Signed in as ${this.usernameSignal()}`);
  readonly welcomeLabel = computed(() => `Welcome back, ${this.usernameSignal()}`);

  async loadCurrentUser(): Promise<void> {
    if (this.loadPromise) {
      return this.loadPromise;
    }

    this.isLoadingUsernameSignal.set(true);
    this.loadPromise = this.api.getLoggedInUsername()
      .then((username) => {
        this.setUsername(username);
      })
      .catch(() => {
        this.usernameSignal.set(USERNAME_FALLBACK);
      })
      .finally(() => {
        this.isLoadingUsernameSignal.set(false);
        this.loadPromise = null;
      });

    return this.loadPromise;
  }

  setUsername(username: string | null | undefined): void {
    const normalizedUsername = username?.trim();
    this.usernameSignal.set(normalizedUsername || USERNAME_FALLBACK);
  }

  reset(): void {
    this.usernameSignal.set(USERNAME_FALLBACK);
    this.isLoadingUsernameSignal.set(false);
    this.loadPromise = null;
  }
}
