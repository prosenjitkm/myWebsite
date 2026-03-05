import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '',          loadComponent: () => import('./pages/home/home.component').then(m => m.HomeComponent) },
  { path: 'blog',      loadComponent: () => import('./pages/blog/blog.component').then(m => m.BlogComponent) },
  { path: 'blog/:slug',loadComponent: () => import('./pages/post/post.component').then(m => m.PostComponent) },
  { path: 'resume',    loadComponent: () => import('./pages/resume/resume.component').then(m => m.ResumeComponent) },
  { path: 'login',     loadComponent: () => import('./pages/login/login.component').then(m => m.LoginComponent) },
  { path: 'register',  loadComponent: () => import('./pages/register/register.component').then(m => m.RegisterComponent) },
  { path: 'oauth2/callback', loadComponent: () => import('./pages/oauth2-callback/oauth2-callback.component').then(m => m.OAuth2CallbackComponent) },
  { path: 'settings',  loadComponent: () => import('./pages/settings/settings.component').then(m => m.SettingsComponent), canActivate: [authGuard] },
  { path: '**', redirectTo: '' }
];
