import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-oauth2-callback',
  standalone: true,
  template: `<p style="text-align:center;margin-top:4rem;color:#94a3b8">Signing you in…</p>`
})
export class OAuth2CallbackComponent implements OnInit {
  constructor(
    private route: ActivatedRoute,
    private auth: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    const token = this.route.snapshot.queryParamMap.get('token');
    if (token) {
      this.auth.handleOAuthToken(token);
      this.router.navigate(['/']);
    } else {
      this.router.navigate(['/login']);
    }
  }
}

