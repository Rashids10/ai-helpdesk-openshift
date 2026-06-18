import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HelpdeskApiService } from './api/helpdesk-api.service';
import { AuthService } from './core/auth/auth.service';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login-page.component.html',
  styleUrl: './login-page.component.css',
})
export class LoginPageComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly api = inject(HelpdeskApiService);
  private readonly auth = inject(AuthService);

  protected readonly form = this.formBuilder.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]],
  });

  protected isSubmitting = false;
  protected errorMessage = '';

  protected get email() {
    return this.form.controls.email;
  }

  protected get password() {
    return this.form.controls.password;
  }

  protected async onSubmit(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';

    try {
      const { email, password } = this.form.getRawValue();
      const { response, body } = await this.api.login({
        userEmail: email.trim(),
        password,
      });

      if (!response.ok) {
        this.errorMessage = this.api.extractErrorMessage(body, 'Login failed. Please try again.');
        return;
      }

      const token = this.api.storeToken(body);

      if (!token) {
        this.errorMessage = 'Login succeeded, but no access token was returned.';
        return;
      }

      const username = body?.['username'];
      if (typeof username === 'string') {
        this.auth.setUsername(username);
      } else {
        await this.auth.loadCurrentUser();
      }

      await this.router.navigate(['/dashboard'], { replaceUrl: true });
    } catch (error) {
      this.errorMessage = this.api.extractErrorMessage(error, 'Login failed. Please try again.');
    } finally {
      this.isSubmitting = false;
    }
  }
}
