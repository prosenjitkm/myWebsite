import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Post, PostPage, CommentResponse, CommentRequest } from '../models/post.model';
import { ResumeSection } from '../models/resume.model';
import { environment } from '../../../environments/environment';

const BASE = environment.apiUrl;

export interface UserProfile {
  email: string;
  username: string;
  firstName: string;
  lastName: string;
  role: string;
  hasPassword: boolean;
  oauthProvider: string | null;
}

@Injectable({ providedIn: 'root' })
export class ApiService {

  constructor(private http: HttpClient) {}

  // ---- Posts ----
  getPosts(page = 0, size = 10): Observable<PostPage> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PostPage>(`${BASE}/posts`, { params });
  }

  getPost(slug: string): Observable<Post> {
    return this.http.get<Post>(`${BASE}/posts/${slug}`);
  }

  // ---- Comments ----
  getComments(postId: string): Observable<CommentResponse[]> {
    return this.http.get<CommentResponse[]>(`${BASE}/posts/${postId}/comments`);
  }

  addComment(postId: string, req: CommentRequest): Observable<CommentResponse> {
    return this.http.post<CommentResponse>(`${BASE}/posts/${postId}/comments`, req);
  }

  // ---- Resume (public) ----
  getResume(): Observable<ResumeSection[]> {
    return this.http.get<ResumeSection[]>(`${BASE}/resume`);
  }

  // ---- Resume (admin) ----
  getAllResumeSections(): Observable<ResumeSection[]> {
    return this.http.get<ResumeSection[]>(`${BASE}/resume/all`);
  }

  updateResumeSection(id: number, section: ResumeSection): Observable<ResumeSection> {
    return this.http.put<ResumeSection>(`${BASE}/resume/${id}`, section);
  }

  createResumeSection(section: Partial<ResumeSection>): Observable<ResumeSection> {
    return this.http.post<ResumeSection>(`${BASE}/resume`, section);
  }

  deleteResumeSection(id: number): Observable<void> {
    return this.http.delete<void>(`${BASE}/resume/${id}`);
  }

  reorderResumeSections(orders: { id: number; sortOrder: number }[]): Observable<void> {
    return this.http.patch<void>(`${BASE}/resume/reorder`, orders);
  }

  // ---- Profile / Settings ----
  getMe(): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${BASE}/auth/me`);
  }

  setPassword(req: { currentPassword?: string; newPassword: string }): Observable<void> {
    return this.http.post<void>(`${BASE}/auth/set-password`, req);
  }
}


