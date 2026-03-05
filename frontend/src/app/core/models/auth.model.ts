export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  username: string;
  firstName: string;
  lastName: string;
}

export interface AuthResponse {
  token: string;
  email: string;
  username: string;
  role: string;
}

export interface AuthUser {
  token: string;
  email: string;
  username: string;
  role: string;
}

