export interface Post {
  id: string;
  title: string;
  slug: string;
  summary: string;
  body: string;
  coverImage: string | null;
  tags: string[];
  authorUsername: string;
  publishedAt: string;
}

export interface PostPage {
  content: Post[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface CommentResponse {
  id: string;
  body: string;
  authorUsername: string;
  parentId: string | null;
  createdAt: string;
}

export interface CommentRequest {
  body: string;
  parentId: string | null;
}

