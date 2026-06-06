import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs';
import { environment } from '../../../env/environment';

@Injectable({ providedIn: 'root' })
export class AuthService {
  constructor(private http: HttpClient, private router: Router) {}

  login(username: string, password: string) {
    return this.http.post<{ accessToken: string, refreshToken: string }>(
      `${environment.apiHost}/auth/login`,
      { username, password }
    ).pipe(
      tap(response => {
        localStorage.setItem('access_token', response.accessToken);
        localStorage.setItem('refresh_token', response.refreshToken )
      })
    );
  }

  logout() {
    const refreshToken = this.getRefreshToken();
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    this.http.post(`${environment.apiHost}/auth/logout`, { refreshToken })
      .subscribe({
        next: () => this.router.navigate(['/login']),
        error: () => this.router.navigate(['/login'])
      });
  }

  getToken(): string | null {
    return localStorage.getItem('access_token');
  }

  getRefreshToken(): string | null {
    return localStorage.getItem('refresh_token');
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  getRole(): string | null {
    const token = this.getToken();
    if (!token) return null;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.role;
    } catch {
      return null;
    }
  }

  refreshTokens() {
    const refreshToken = this.getRefreshToken();
    return this.http.post< {accessToken: string, refreshToken: string} >(
      `${environment.apiHost}/auth/refresh`,
      { refreshToken }
    ).pipe(
      tap(response => {
        localStorage.setItem('access_token', response.accessToken);
        localStorage.setItem('refresh_token', response.refreshToken);
      })
    )
  }
}