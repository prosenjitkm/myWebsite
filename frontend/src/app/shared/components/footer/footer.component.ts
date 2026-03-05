import { Component } from '@angular/core';

@Component({
  selector: 'app-footer',
  standalone: true,
  template: `
    <footer class="footer">
      <p>© {{ year }} MyWebsite · Built with Angular & Spring Boot</p>
    </footer>
  `,
  styles: [`
    .footer {
      text-align: center;
      padding: 1.5rem;
      background: #0f172a;
      color: #64748b;
      font-size: 0.85rem;
      margin-top: auto;
    }
  `]
})
export class FooterComponent {
  year = new Date().getFullYear();
}

