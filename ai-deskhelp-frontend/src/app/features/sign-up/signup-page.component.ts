import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy, inject, signal } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize, take, timeout } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { environment } from '../../../environments/environment';

interface SignupResponse {
  accessToken?: string;
  token?: string;
  jwt?: string;
  message?: string;
  [key: string]: unknown;
}

const SIGNUP_ENDPOINT = `${environment.apiBaseUrl}/auth/signup`;
const SIGNUP_REQUEST_TIMEOUT_MS = 5000;

const passwordsMatchValidator: ValidatorFn = (
  control: AbstractControl,
): ValidationErrors | null => {
  const password = control.get('password')?.value as string | undefined;
  const confirmPassword = control.get('confirmPassword')?.value as string | undefined;

  if (!password || !confirmPassword) {
    return null;
  }

  return password === confirmPassword ? null : { passwordMismatch: true };
};

@Component({
  selector: 'app-signup-page',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './signup-page.component.html',
  styleUrl: './signup-page.component.css',
})
export class SignupPageComponent implements OnDestroy {
  private readonly formBuilder = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly auth = inject(AuthService);
  private readonly http = inject(HttpClient);
  private redirectTimeoutId: ReturnType<typeof setTimeout> | null = null;

  protected readonly form = this.formBuilder.nonNullable.group(
    {
      email: ['', [Validators.required, Validators.email, Validators.maxLength(50)]],
      username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(20)]],
      password: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(60)]],
      confirmPassword: ['', [Validators.required]],
    },
    { validators: [passwordsMatchValidator] },
  );

  protected readonly isSubmitting = signal(false);
  protected readonly errorMessage = signal('');
  protected readonly emailServerError = signal('');
  protected readonly successMessage = signal('');
  protected showPassword = false;
  protected showConfirmPassword = false;

  protected get email() {
    return this.form.controls.email;
  }

  protected get username() {
    return this.form.controls.username;
  }

  protected get password() {
    return this.form.controls.password;
  }

  protected get confirmPassword() {
    return this.form.controls.confirmPassword;
  }

  protected get passwordInputType(): 'text' | 'password' {
    return this.showPassword ? 'text' : 'password';
  }

  protected get confirmPasswordInputType(): 'text' | 'password' {
    return this.showConfirmPassword ? 'text' : 'password';
  }

  protected onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    this.errorMessage.set('');
    this.emailServerError.set('');
    this.successMessage.set('');

    const { email, username, password } = this.form.getRawValue();

    this.http.post<SignupResponse>(
      SIGNUP_ENDPOINT,
      {
        email,
        username,
        password,
      },
      { observe: 'response' },
    )
      .pipe(
        timeout(SIGNUP_REQUEST_TIMEOUT_MS),
        take(1),
        finalize(() => {
          this.isSubmitting.set(false);
        }),
      )
      .subscribe({
        next: ({ body }) => {
          const token = this.extractToken(body);
          if (token) {
            localStorage.setItem('accessToken', token);
            const username = body?.['username'];
            this.auth.setUsername(typeof username === 'string' ? username : null);
          }

          this.successMessage.set('Account created successfully. Redirecting to dashboard...');
          this.redirectTimeoutId = setTimeout(() => {
            void this.router.navigate(['/dashboard'], { replaceUrl: true });
          }, 900);
        },
        error: (error: unknown) => {
          const backendMessage = this.extractErrorMessage(error);
          this.emailServerError.set(backendMessage);
          this.errorMessage.set(backendMessage);
        },
      });
  }

  protected togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  protected toggleConfirmPasswordVisibility(): void {
    this.showConfirmPassword = !this.showConfirmPassword;
  }

  ngOnDestroy(): void {
    if (this.redirectTimeoutId) {
      clearTimeout(this.redirectTimeoutId);
      this.redirectTimeoutId = null;
    }
  }

  private extractToken(body: SignupResponse | null): string | null {
    const token = body?.accessToken ?? body?.token ?? body?.jwt;

    return typeof token === 'string' && token.trim() ? token : null;
  }

  private extractErrorMessage(error: unknown): string {
    const fallback = 'Signup failed. Please try again.';

    if (error instanceof HttpErrorResponse) {
      if (typeof error.error === 'string' && error.error.trim()) {
        return error.error;
      }

      if (error.error && typeof error.error === 'object') {
        const backendMessage = (error.error as { message?: unknown }).message;
        if (typeof backendMessage === 'string' && backendMessage.trim()) {
          return backendMessage;
        }
      }

      if (error.message?.trim()) {
        return error.message;
      }
    }

    if (!error || typeof error !== 'object') {
      return fallback;
    }

    const typedError = error as {
      error?: { message?: unknown } | unknown;
      message?: unknown;
    };

    if (typedError.error && typeof typedError.error === 'object') {
      const backendMessage = (typedError.error as { message?: unknown }).message;
      if (typeof backendMessage === 'string' && backendMessage.trim()) {
        return backendMessage;
      }
    }

    if (typeof typedError.message === 'string' && typedError.message.trim()) {
      return typedError.message;
    }

    return fallback;
  }
}
