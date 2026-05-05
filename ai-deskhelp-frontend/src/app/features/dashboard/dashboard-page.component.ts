import { Component } from '@angular/core';

interface StatCard {
  label: string;
  value: string;
  note: string;
}

interface TicketRow {
  title: string;
  status: string;
  created: string;
  priority: string;
  statusTone: 'success' | 'warning' | 'neutral';
  priorityTone: 'high' | 'medium' | 'low';
}

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  templateUrl: './dashboard-page.component.html',
  styleUrl: './dashboard-page.component.css',
})
export class DashboardPageComponent {
  protected readonly statCards: StatCard[] = [
    { label: 'Open Tickets', value: '12', note: '+3 from yesterday' },
    { label: 'In Progress', value: '05', note: 'Waiting on updates' },
    { label: 'Resolved', value: '28', note: 'This month' },
  ];

  protected readonly tickets: TicketRow[] = [
    {
      title: 'Laptop access request',
      status: 'Open',
      created: '2026-05-04',
      priority: 'High',
      statusTone: 'warning',
      priorityTone: 'high',
    },
    {
      title: 'Password reset for mailbox',
      status: 'In Progress',
      created: '2026-05-03',
      priority: 'Medium',
      statusTone: 'neutral',
      priorityTone: 'medium',
    },
    {
      title: 'VPN setup confirmation',
      status: 'Resolved',
      created: '2026-05-02',
      priority: 'Low',
      statusTone: 'success',
      priorityTone: 'low',
    },
  ];
}
