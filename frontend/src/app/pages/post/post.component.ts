import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';
import { Post } from '../../core/models/post.model';
import { CommentResponse } from '../../core/models/post.model';

@Component({
  selector: 'app-post',
  standalone: true,
  imports: [RouterLink, DatePipe, FormsModule],
  templateUrl: './post.component.html',
  styleUrl: './post.component.css'
})
export class PostComponent implements OnInit {
  post     = signal<Post | null>(null);
  comments = signal<CommentResponse[]>([]);
  loading  = signal(true);
  error    = signal('');
  commentBody = '';
  submitting  = false;
  commentError = '';

  constructor(
    private route: ActivatedRoute,
    private api: ApiService,
    protected auth: AuthService
  ) {}

  ngOnInit() {
    const slug = this.route.snapshot.paramMap.get('slug')!;
    this.api.getPost(slug).subscribe({
      next: post => {
        this.post.set(post);
        this.loading.set(false);
        this.loadComments(post.id);
      },
      error: () => { this.error.set('Post not found.'); this.loading.set(false); }
    });
  }

  loadComments(postId: string) {
    this.api.getComments(postId).subscribe(c => this.comments.set(c));
  }

  submitComment() {
    if (!this.commentBody.trim()) return;
    this.submitting = true;
    this.commentError = '';
    this.api.addComment(this.post()!.id, { body: this.commentBody, parentId: null }).subscribe({
      next: c => {
        this.comments.update(list => [...list, c]);
        this.commentBody = '';
        this.submitting = false;
      },
      error: () => { this.commentError = 'Failed to post comment.'; this.submitting = false; }
    });
  }
}

