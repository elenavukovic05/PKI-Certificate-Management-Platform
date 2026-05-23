import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../env/environment';
import { Observable } from 'rxjs';

export interface TwoFactorSetupResponse {
  secret: string;
  qrUrl: string;
}

@Injectable({ providedIn: 'root' })
export class TwoFactorAuthService {
  private baseUrl = `${environment.apiHost}/auth/2fa`;

  constructor(private http: HttpClient) {}

  getStatus(): Observable<{ enabled: boolean }> {
    return this.http.get<{ enabled: boolean }>(`${this.baseUrl}/status`);
  }

  setup(): Observable<TwoFactorSetupResponse> {
    return this.http.post<TwoFactorSetupResponse>(`${this.baseUrl}/setup`, {});
  }

  enable(secret: string, code: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/enable`, { secret, code });
    
  }

  disable(code: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/disable`, { code });
  }
}