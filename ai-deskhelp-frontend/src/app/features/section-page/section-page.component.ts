import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-section-page',
  standalone: true,
  templateUrl: './section-page.component.html',
  styleUrl: './section-page.component.css',
})
export class SectionPageComponent {
  private readonly route = inject(ActivatedRoute);

  protected readonly title = (this.route.snapshot.data['title'] as string | undefined) ?? 'Section';
  protected readonly eyebrow = (this.route.snapshot.data['eyebrow'] as string | undefined) ?? 'Workspace';
  protected readonly description =
    (this.route.snapshot.data['description'] as string | undefined) ??
    'This section uses the shared app layout and placeholder content for now.';
  protected readonly actionLabel =
    (this.route.snapshot.data['actionLabel'] as string | undefined) ?? 'Continue';
  protected readonly actionNote = (this.route.snapshot.data['actionNote'] as string | undefined) ?? 'Demo page';
}
