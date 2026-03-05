import { Component, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { ApiService } from '../../core/services/api.service';
import { Post, PostPage } from '../../core/models/post.model';

@Component({
  selector: 'app-blog',
  standalone: true,
  imports: [RouterLink, DatePipe],
  templateUrl: './blog.component.html',
  styleUrl: './blog.component.css'
})
export class BlogComponent implements OnInit {
  posts   = signal<Post[]>([]);
  page    = signal(0);
  total   = signal(0);
  loading = signal(true);
  error   = signal('');
  pageSize = 10;

  constructor(private api: ApiService) {}

  ngOnInit() { this.load(); }

  load() {
    this.loading.set(true);
    this.api.getPosts(this.page(), this.pageSize).subscribe({
      next: (res: PostPage) => {
        this.posts.set(res.content);
        this.total.set(res.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Failed to load posts.');
        this.loading.set(false);
      }
    });
  }

  prev() { this.page.update(p => p - 1); this.load(); }
  next() { this.page.update(p => p + 1); this.load(); }

  get totalPages() { return Math.ceil(this.total() / this.pageSize); }
}


