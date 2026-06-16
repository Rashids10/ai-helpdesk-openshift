import { Component, OnDestroy, inject } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
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

  protected isSubmitting = false;
  protected errorMessage = '';
  protected emailServerError = '';
  protected successMessage = '';
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

  protected async onSubmit(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';
    this.emailServerError = '';
    this.successMessage = '';

    try {
      const { email, username, password } = this.form.getRawValue();
      const { response, body } = await this.submitSignup({
        email,
        username,
        password,
      });

      if (!response.ok) {
        const backendMessage = this.extractErrorMessage(body);
        this.emailServerError = backendMessage;
        this.errorMessage = backendMessage;
        return;
      }

      const token = this.extractToken(body);
      if (token) {
        localStorage.setItem('accessToken', token);
      }

      this.successMessage = 'Account created successfully. Redirecting to dashboard...';
      this.redirectTimeoutId = setTimeout(() => {
        void this.router.navigate(['/dashboard'], { replaceUrl: true });
      }, 900);
    } catch (error) {
      const backendMessage = this.extractErrorMessage(error);
      this.emailServerError = backendMessage;
      this.errorMessage = backendMessage;
    } finally {
      this.isSubmitting = false;
    }
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

  private async submitSignup(payload: {
    email: string;
    username: string;
    password: string;
  }): Promise<{ response: Response; body: SignupResponse | null }> {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), SIGNUP_REQUEST_TIMEOUT_MS);

    try {
      const response = await fetch(SIGNUP_ENDPOINT, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
        signal: controller.signal,
      });

      const body = await this.safeJson(response);

      return { response, body };
    } finally {
      clearTimeout(timeoutId);
    }
  }

  private async safeJson(response: Response): Promise<SignupResponse | null> {
    try {
      return (await response.json()) as SignupResponse;
    } catch {
      return null;
    }
  }

  private extractToken(body: SignupResponse | null): string | null {
    const token = body?.accessToken ?? body?.token ?? body?.jwt;

    return typeof token === 'string' && token.trim() ? token : null;
  }

  private extractErrorMessage(error: unknown): string {
    const fallback = 'Signup failed. Please try again.';

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
