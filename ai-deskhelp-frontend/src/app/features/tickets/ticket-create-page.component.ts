import { Component, inject } from '@angular/core';
import { ReactiveFormsModule, Validators } from '@angular/forms';
import { FormBuilder } from '@angular/forms';

interface TicketCreateResponse {
  message?: string;
  [key: string]: unknown;
}

const TICKET_ENDPOINT = 'http://localhost:8089/api/ticket/createTicket';

@Component({
  selector: 'app-ticket-create-page',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './ticket-create-page.component.html',
  styleUrl: './ticket-create-page.component.css',
})
export class TicketCreatePageComponent {
  private readonly formBuilder = inject(FormBuilder);

  protected readonly form = this.formBuilder.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(120)]],
    description: ['', [Validators.required, Validators.maxLength(2000)]],
  });

  protected isSubmitting = false;
  protected successMessage = '';
  protected errorMessage = '';

  protected get title() {
    return this.form.controls.title;
  }

  protected get description() {
    return this.form.controls.description;
  }

  protected async onSubmit(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.successMessage = '';
    this.errorMessage = '';

    try {
      const { title, description } = this.form.getRawValue();
      const response = await fetch(TICKET_ENDPOINT, {
        method: 'POST',
        headers: this.buildHeaders(),
        body: JSON.stringify({
          title: title.trim(),
          description: description.trim(),
        }),
      });

      const body = await this.safeJson(response);

      if (!response.ok) {
        this.errorMessage = this.extractErrorMessage(body);
        return;
      }

      this.successMessage = 'Ticket created successfully.';
      this.form.reset({
        title: '',
        description: '',
      });
      this.form.markAsPristine();
      this.form.markAsUntouched();
    } catch (error) {
      this.errorMessage = this.extractErrorMessage(error);
    } finally {
      this.isSubmitting = false;
    }
  }

  private buildHeaders(): HeadersInit {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    const token = localStorage.getItem('accessToken');

    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    return headers;
  }

  private async safeJson(response: Response): Promise<TicketCreateResponse | null> {
    try {
      return (await response.json()) as TicketCreateResponse;
    } catch {
      return null;
    }
  }

  private extractErrorMessage(error: unknown): string {
    const fallback = 'Ticket creation failed. Please try again.';

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

    if (typeof (error as TicketCreateResponse).message === 'string') {
      const backendMessage = (error as TicketCreateResponse).message;
      if (backendMessage?.trim()) {
        return backendMessage;
      }
    }

    return fallback;
  }
}
