import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { environment } from '../../../environments/environment';

// Strip the /api suffix to get the backend root URL
const BACKEND_ROOT = environment.apiUrl.replace(/\/api$/, '');
const GOOGLE_AUTH_URL = `${BACKEND_ROOT}/oauth2/authorization/google`;

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  email    = '';
  password = '';
  error    = signal('');
  loading  = signal(false);

  constructor(private auth: AuthService, private router: Router) {}

  submit() {
    if (!this.email || !this.password) return;
    this.loading.set(true);
    this.error.set('');
    this.auth.login({ email: this.email, password: this.password }).subscribe({
      next: () => this.router.navigate(['/']),
      error: () => { this.error.set('Invalid email or password.'); this.loading.set(false); }
    });
  }

  loginWithGoogle() {
    window.location.href = GOOGLE_AUTH_URL;
  }
}

