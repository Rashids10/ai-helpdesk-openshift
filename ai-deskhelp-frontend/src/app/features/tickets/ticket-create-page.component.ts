import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule, Validators } from '@angular/forms';
import { FormBuilder } from '@angular/forms';
import { Subscription } from 'rxjs';
import { HelpdeskApiService, TicketListItem } from '../../api/helpdesk-api.service';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-ticket-create-page',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './ticket-create-page.component.html',
  styleUrl: './ticket-create-page.component.css',
})
export class TicketCreatePageComponent implements OnInit, OnDestroy {
  private readonly formBuilder = inject(FormBuilder);
  private readonly api = inject(HelpdeskApiService);
  private readonly auth = inject(AuthService);
  private readonly subscriptions = new Subscription();

  protected readonly form = this.formBuilder.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(120)]],
    description: ['', [Validators.required, Validators.maxLength(2000)]],
  });

  protected readonly isSubmitting = signal(false);
  protected readonly successMessage = signal('');
  protected readonly errorMessage = signal('');
  protected readonly isLoadingTickets = signal(true);
  protected readonly ticketsErrorMessage = signal('');
  protected readonly tickets = signal<TicketListItem[]>([]);
  protected readonly signedInLabel = this.auth.signedInLabel;

  protected get title() {
    return this.form.controls.title;
  }

  protected get description() {
    return this.form.controls.description;
  }

  ngOnInit(): void {
    this.subscriptions.add(
      this.api.ticketsChanged$.subscribe(() => {
        void this.loadTickets();
      }),
    );

    void this.loadTickets();
  }

  protected async onSubmit(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    this.successMessage.set('');
    this.errorMessage.set('');

    try {
      const { title, description } = this.form.getRawValue();
      const { response, body } = await this.api.createTicket({
        title: title.trim(),
        description: description.trim(),
      });

      if (!response.ok) {
        this.errorMessage.set(
          this.api.extractErrorMessage(body, 'Ticket creation failed. Please try again.'),
        );
        return;
      }

      this.successMessage.set('Ticket created successfully.');
      this.form.reset({
        title: '',
        description: '',
      });
      this.form.markAsPristine();
      this.form.markAsUntouched();
      this.api.notifyTicketsChanged();
    } catch (error) {
      this.errorMessage.set(
        this.api.extractErrorMessage(error, 'Ticket creation failed. Please try again.'),
      );
    } finally {
      this.isSubmitting.set(false);
    }
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  private async loadTickets(): Promise<void> {
    this.isLoadingTickets.set(true);
    this.ticketsErrorMessage.set('');

    try {
      const result = await this.api.getMyTickets();

      if (!result.response.ok) {
        this.tickets.set([]);
        this.ticketsErrorMessage.set(
          this.api.extractErrorMessage(result.body, 'Could not load your tickets.'),
        );
        return;
      }

      this.tickets.set(result.tickets);
    } catch (error) {
      this.tickets.set([]);
      this.ticketsErrorMessage.set(
        this.api.extractErrorMessage(error, 'Could not load your tickets.'),
      );
    } finally {
      this.isLoadingTickets.set(false);
    }
  }
}
