import { Component, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
import { Subscription } from 'rxjs';
import { HelpdeskApiService, TicketListItem } from '../../api/helpdesk-api.service';
import { AuthService } from '../../core/auth/auth.service';

interface StatCard {
  label: string;
  value: string;
  note: string;
}

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  templateUrl: './dashboard-page.component.html',
  styleUrl: './dashboard-page.component.css',
})
export class DashboardPageComponent implements OnInit, OnDestroy {
  private readonly api = inject(HelpdeskApiService);
  private readonly auth = inject(AuthService);
  private readonly subscriptions = new Subscription();

  protected readonly statCards = signal<StatCard[]>([]);
  protected readonly tickets = signal<TicketListItem[]>([]);
  protected readonly isLoading = signal(true);
  protected readonly errorMessage = signal('');
  protected readonly hasTickets = computed(() => this.tickets().length > 0);
  protected readonly welcomeLabel = this.auth.welcomeLabel;
  protected readonly signedInLabel = this.auth.signedInLabel;

  ngOnInit(): void {
    this.subscriptions.add(
      this.api.ticketsChanged$.subscribe(() => {
        void this.loadTickets();
      }),
    );

    void this.loadTickets();
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  private async loadTickets(): Promise<void> {
    this.isLoading.set(true);
    this.errorMessage.set('');

    try {
      const result = await this.api.getMyTickets();

      if (!result.response.ok) {
        this.tickets.set([]);
        this.statCards.set(this.buildStatCards([]));
        this.errorMessage.set(
          this.api.extractErrorMessage(result.body, 'Could not load your tickets.'),
        );
        return;
      }

      this.tickets.set(result.tickets);
      this.statCards.set(this.buildStatCards(result.tickets));
    } catch (error) {
      this.tickets.set([]);
      this.statCards.set(this.buildStatCards([]));
      this.errorMessage.set(
        this.api.extractErrorMessage(error, 'Could not load your tickets.'),
      );
    } finally {
      this.isLoading.set(false);
    }
  }

  private buildStatCards(tickets: TicketListItem[]): StatCard[] {
    const openTickets = tickets.filter((ticket) => ticket.statusKey === 'open').length;
    const inProgressTickets = tickets.filter((ticket) => ticket.statusKey === 'in_progress').length;
    const resolvedTickets = tickets.filter(
      (ticket) => ticket.statusKey === 'closed',
    ).length;

    return [
      { label: 'Open Tickets', value: String(openTickets), note: 'From your inbox' },
      { label: 'In Progress', value: String(inProgressTickets), note: 'Waiting on updates' },
      { label: 'Resolved / Closed', value: String(resolvedTickets), note: 'Completed tickets' },
    ];
  }
}
