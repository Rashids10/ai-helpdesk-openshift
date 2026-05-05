import { Routes } from '@angular/router';
import { AppLayoutComponent } from './core/layout/app-layout/app-layout.component';
import { DashboardPageComponent } from './features/dashboard/dashboard-page.component';
import { LoginPageComponent } from './login-page.component';
import { SectionPageComponent } from './features/section-page/section-page.component';
import { SignupPageComponent } from './features/sign-up/signup-page.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'signup' },
  { path: 'signup', component: SignupPageComponent },
  { path: 'login', component: LoginPageComponent },
  {
    path: '',
    component: AppLayoutComponent,
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      { path: 'dashboard', component: DashboardPageComponent, data: { title: 'Dashboard overview' } },
      {
        path: 'tickets',
        component: SectionPageComponent,
        data: {
          title: 'Tickets',
          eyebrow: 'Ticket workspace',
          description: 'Review mock tickets, organize priorities, and keep progress visible.',
          actionLabel: 'Create Ticket',
          actionNote: 'Demo content only',
        },
      },
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
