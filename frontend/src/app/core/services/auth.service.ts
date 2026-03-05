import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { AuthUser, LoginRequest, RegisterRequest, AuthResponse } from '../models/auth.model';
import { environment } from '../../../environments/environment';

const TOKEN_KEY = 'auth_token_v2';
const USER_KEY  = 'auth_user_v2';
const API       = `${environment.apiUrl}/auth`;

@Injectable({ providedIn: 'root' })
export class AuthService {

  private _user = signal<AuthUser | null>(this.loadUser());

  readonly user     = this._user.asReadonly();
  readonly isLoggedIn = computed(() => !!this._user());
  readonly isAdmin    = computed(() => this._user()?.role === 'ADMIN');

  constructor(private http: HttpClient, private router: Router) {
    // Remove stale v1 keys from previous app versions
    localStorage.removeItem('auth_token');
    localStorage.removeItem('auth_user');
  }

  login(req: LoginRequest) {
    return this.http.post<AuthResponse>(`${API}/login`, req).pipe(
      tap(res => this.saveSession(res))
    );
  }

  register(req: RegisterRequest) {
    return this.http.post<AuthResponse>(`${API}/register`, req).pipe(
      tap(res => this.saveSession(res))
    );
  }

  /** Called by the OAuth2CallbackComponent after Google redirects back */
  handleOAuthToken(token: string) {
    // Decode the payload to extract user info
    const payload = JSON.parse(atob(token.split('.')[1]));
    const user: AuthUser = {
      token,
      email: payload.sub,
      username: payload.sub,   // will be updated on next API call if needed
      role: payload.role ?? 'USER'
    };
    this.saveSession({ token, email: user.email, username: user.username, role: user.role });
  }

  logout() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this._user.set(null);
    this.router.navigate(['/']);
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  private saveSession(res: AuthResponse) {
    localStorage.setItem(TOKEN_KEY, res.token);
    localStorage.setItem(USER_KEY, JSON.stringify(res));
    this._user.set(res);
  }

  private loadUser(): AuthUser | null {
    try {
      const raw = localStorage.getItem(USER_KEY);
      if (!raw) return null;
      const user: AuthUser = JSON.parse(raw);

      // Validate the JWT contains a role claim — old tokens didn't have it.
      // If role is missing or token is malformed, force a fresh login.
      const token = localStorage.getItem(TOKEN_KEY);
      if (token) {
        try {
          const payload = JSON.parse(atob(token.split('.')[1]));
          if (!payload.role) {
            console.warn('[AuthService] Stale JWT detected (no role claim) — clearing session, please log in again.');
            localStorage.removeItem(TOKEN_KEY);
            localStorage.removeItem(USER_KEY);
            return null;
          }
          // Also sync the role from the JWT payload (source of truth)
          user.role = payload.role;
        } catch {
          // Malformed token — clear it
          localStorage.removeItem(TOKEN_KEY);
          localStorage.removeItem(USER_KEY);
          return null;
        }
      }
      return user;
    } catch {
      return null;
    }
  }
}

