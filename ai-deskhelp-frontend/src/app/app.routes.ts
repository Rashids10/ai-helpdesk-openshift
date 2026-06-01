import { Routes } from '@angular/router';
import { AppLayoutComponent } from './core/layout/app-layout/app-layout.component';
import { DashboardPageComponent } from './features/dashboard/dashboard-page.component';
import { LoginPageComponent } from './login-page.component';
import { SignupPageComponent } from './features/sign-up/signup-page.component';
import { TicketCreatePageComponent } from './features/tickets/ticket-create-page.component';
import { SectionPageComponent } from './features/section-page/section-page.component';
import { AiAssistantPageComponent } from './features/ai-assistant/ai-assistant-page.component';

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
        component: AiAssistantPageComponent,
        data: {
          title: 'AI Assistant',
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
