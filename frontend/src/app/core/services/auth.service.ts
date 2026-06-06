import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { Observable, Subject } from 'rxjs';
import { environment } from '../../../env/environment';

export interface LoginResponse {
  accessToken: string;
  refreshToken: string | null;
  requires2FA: boolean;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private tempToken: string | null = null;
  private authStateChanged = new Subject<void>();
  authStateChanged$ = this.authStateChanged.asObservable();

  constructor(private http: HttpClient, private router: Router) {}

  login(username: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(
      `${environment.apiHost}/auth/login`,
      { username, password }
    ).pipe(
      tap(response => {
        if (response.requires2FA) {
          this.tempToken = response.accessToken;
          localStorage.setItem('temp_token', response.accessToken);
        } else {
          localStorage.setItem('access_token', response.accessToken);
          if (response.refreshToken) {
            localStorage.setItem('refresh_token', response.refreshToken);
          }
          this.tempToken = null;
          localStorage.removeItem('temp_token');
          this.authStateChanged.next();
        }
      })
    );
  }

  verify2FA(code: number): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(
      `${environment.apiHost}/auth/2fa/verify`,
      { code },
      {
        headers: {
          Authorization: `Bearer ${this.tempToken || localStorage.getItem('temp_token')}`
        }
      }
    ).pipe(
      tap(response => {
        localStorage.setItem('access_token', response.accessToken);
        if (response.refreshToken) {
          localStorage.setItem('refresh_token', response.refreshToken);
        }
        localStorage.removeItem('temp_token');
        this.tempToken = null;
        this.authStateChanged.next();
      })
    );
  }

  is2FARequired(): boolean {
    return !!localStorage.getItem('temp_token');
  }

  getTempToken(): string | null {
    return this.tempToken || localStorage.getItem('temp_token');
  }

  logout() {
    const refreshToken = this.getRefreshToken();
    localStorage.removeItem('access_token');
    localStorage.removeItem('temp_token');
    this.tempToken = null;
    this.authStateChanged.next();
    localStorage.removeItem('refresh_token');
    this.http.post(`${environment.apiHost}/auth/logout`, { refreshToken })
      .subscribe({
        next: () => this.router.navigate(['/login']),
        error: () => this.router.navigate(['/login'])
      });
  }

  getToken(): string | null {
    return localStorage.getItem('access_token') || localStorage.getItem('temp_token');
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

  notify2FAChanged() {
    this.authStateChanged.next();
  }

  refreshTokens() {
    const refreshToken = this.getRefreshToken();
    return this.http.post< LoginResponse>(
      `${environment.apiHost}/auth/refresh`,
      { refreshToken }
    ).pipe(
      tap(response => {
        localStorage.setItem('access_token', response.accessToken);
        if (response.refreshToken) {
          localStorage.setItem('refresh_token', response.refreshToken);
        }
      })
    )
  }
}