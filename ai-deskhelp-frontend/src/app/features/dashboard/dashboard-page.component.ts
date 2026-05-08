import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { Subscription } from 'rxjs';
import { HelpdeskApiService, TicketListItem } from '../../api/helpdesk-api.service';

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
  private readonly subscriptions = new Subscription();

  protected statCards: StatCard[] = [];
  protected tickets: TicketListItem[] = [];
  protected isLoading = true;
  protected errorMessage = '';

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

  protected get hasTickets(): boolean {
    return this.tickets.length > 0;
  }

  private async loadTickets(): Promise<void> {
    this.isLoading = true;
    this.errorMessage = '';

    try {
      const result = await this.api.getMyTickets();

      if (!result.response.ok) {
        this.tickets = [];
        this.statCards = this.buildStatCards([]);
        this.errorMessage = this.api.extractErrorMessage(
          result.body,
          'Could not load your tickets.',
        );
        return;
      }

      this.tickets = result.tickets;
      this.statCards = this.buildStatCards(this.tickets);
    } catch (error) {
      this.tickets = [];
      this.statCards = this.buildStatCards([]);
      this.errorMessage = this.api.extractErrorMessage(error, 'Could not load your tickets.');
    } finally {
      this.isLoading = false;
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
