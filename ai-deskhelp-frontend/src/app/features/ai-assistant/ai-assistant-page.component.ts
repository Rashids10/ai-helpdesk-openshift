import { Component, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize, take } from 'rxjs';
import { HelpdeskApiService } from '../../api/helpdesk-api.service';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-ai-assistant-page',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './ai-assistant-page.component.html',
  styleUrl: './ai-assistant-page.component.css',
})
export class AiAssistantPageComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly api = inject(HelpdeskApiService);
  private readonly auth = inject(AuthService);

  protected readonly form = this.formBuilder.nonNullable.group({
    query: ['', [Validators.required, Validators.maxLength(2000)]],
  });

  protected readonly isLoading = signal(false);
  protected readonly aiResponse = signal('');
  protected readonly errorMessage = signal('');
  protected readonly signedInLabel = this.auth.signedInLabel;

  protected get query() {
    return this.form.controls.query;
  }

  protected onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.aiResponse.set('');
    this.errorMessage.set('');

    const { query } = this.form.getRawValue();

    console.log('[AI Assistant] submit', { query: query.trim(), isLoading: this.isLoading() });

    this.api.askAiAssistant(query.trim())
      .pipe(
        take(1),
        finalize(() => {
          console.log('[AI Assistant] finalize before state update', {
            aiResponse: this.aiResponse(),
            errorMessage: this.errorMessage(),
            isLoading: this.isLoading(),
          });
          this.isLoading.set(false);
          console.log('[AI Assistant] finalize after state update', {
            isLoading: this.isLoading(),
          });
        }),
      )
      .subscribe({
        next: (responseText) => {
          console.log('[AI Assistant] next', { responseText });
          this.aiResponse.set(responseText.trim());
          console.log('[AI Assistant] aiResponse assigned', { aiResponse: this.aiResponse() });
        },
        error: (error: unknown) => {
          console.error('[AI Assistant] error', error);
          this.errorMessage.set(this.extractAssistantError(error));
          console.log('[AI Assistant] errorMessage assigned', { errorMessage: this.errorMessage() });
        },
      });
  }

  private extractAssistantError(error: unknown): string {
    if (error instanceof HttpErrorResponse && typeof error.error === 'string' && error.error.trim()) {
      return error.error;
    }

    return this.api.extractErrorMessage(error, 'The AI assistant could not answer right now.');
  }
}
