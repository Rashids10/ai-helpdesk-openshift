import { Routes } from '@angular/router';
import { AppLayoutComponent } from './core/layout/app-layout/app-layout.component';
import { DashboardPageComponent } from './features/dashboard/dashboard-page.component';
import { LoginPageComponent } from './login-page.component';
import { SignupPageComponent } from './features/sign-up/signup-page.component';
import { TicketCreatePageComponent } from './features/tickets/ticket-create-page.component';
import { SectionPageComponent } from './features/section-page/section-page.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  { path: 'signup', component: SignupPageComponent },
  { path: 'login', component: LoginPageComponent },
  {
    path: '',
    component: AppLayoutComponent,
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      { path: 'dashboard', component: DashboardPageComponent, data: { title: 'Dashboard overview' } },
      { path: 'tickets', component: TicketCreatePageComponent, data: { title: 'Create Ticket' } },
      {
        path: 'chat',
        component: SectionPageComponent,
        data: {
          title: 'AI Chat',
          eyebrow: 'Assistant workspace',
          description: 'Use the shared AI helper shell for future chat and support interactions.',
          actionLabel: 'Ask AI Assistant',
          actionNote: 'Placeholder view',
        },
      },
      {
        path: 'settings',
        component: SectionPageComponent,
        data: {
          title: 'Settings',
          eyebrow: 'Preferences',
          description: 'Manage account preferences, notifications, and workspace options.',
          actionLabel: 'Save Changes',
          actionNote: 'Future configuration',
        },
      },
    ],
  },
  { path: '**', redirectTo: 'signup' }
];
