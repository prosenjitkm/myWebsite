import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  template: `
    <nav class="navbar">
      <div class="navbar-brand">
        <a routerLink="/">MyWebsite</a>
      </div>
      <ul class="navbar-links">
        <li><a routerLink="/" routerLinkActive="active" [routerLinkActiveOptions]="{exact:true}">Home</a></li>
        <li><a routerLink="/blog" routerLinkActive="active">Blog</a></li>
        <li><a routerLink="/resume" routerLinkActive="active">Resume</a></li>
        @if (auth.isLoggedIn()) {
          <li><a routerLink="/settings" routerLinkActive="active">Settings</a></li>
          <li class="user-info">Hi, {{ auth.user()?.username }}</li>
          <li><button class="btn-logout" (click)="auth.logout()">Logout</button></li>
        } @else {
          <li><a routerLink="/login" routerLinkActive="active" class="btn-login">Login</a></li>
          <li><a routerLink="/register" routerLinkActive="active" class="btn-register">Sign Up</a></li>
        }
      </ul>
    </nav>
  `,
  styleUrl: './navbar.component.css'
})
export class NavbarComponent {
  protected auth = inject(AuthService);
}

