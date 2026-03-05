import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgClass, UpperCasePipe } from '@angular/common';
import { ApiService, UserProfile } from '../../core/services/api.service';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [FormsModule, NgClass, UpperCasePipe],
  templateUrl: './settings.component.html',
  styleUrl: './settings.component.css'
})
export class SettingsComponent implements OnInit {

  profile   = signal<UserProfile | null>(null);
  loading   = signal(true);

  // password form
  currentPassword = '';
  newPassword     = '';
  confirmPassword = '';
  saving  = signal(false);
  msg     = signal('');
  isError = signal(false);

  constructor(private api: ApiService) {}

  ngOnInit() {
    this.api.getMe().subscribe({
      next: p  => { this.profile.set(p); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  get isFirstTime(): boolean {
    return !this.profile()?.hasPassword;
  }

  submit() {
    if (this.newPassword !== this.confirmPassword) {
      this.flash('Passwords do not match.', true); return;
    }
    if (this.newPassword.length < 8) {
      this.flash('Password must be at least 8 characters.', true); return;
    }

    this.saving.set(true);
    const req = this.isFirstTime
      ? { newPassword: this.newPassword }
      : { currentPassword: this.currentPassword, newPassword: this.newPassword };

    this.api.setPassword(req).subscribe({
      next: () => {
        this.saving.set(false);
        this.currentPassword = '';
        this.newPassword     = '';
        this.confirmPassword = '';
        // Update local profile so form switches from "Set" to "Change"
        this.profile.update(p => p ? { ...p, hasPassword: true } : p);
        this.flash('✔ Password saved! You can now log in with email + password.', false);
      },
      error: err => {
        this.saving.set(false);
        const msg = err?.error?.message ?? 'Failed to set password.';
        this.flash(msg, true);
      }
    });
  }

  private flash(message: string, error: boolean) {
    this.msg.set(message);
    this.isError.set(error);
    if (!error) setTimeout(() => this.msg.set(''), 4000);
  }
}


