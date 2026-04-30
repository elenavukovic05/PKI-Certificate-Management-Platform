import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CertificatePreviewDTO } from '../models/certificate.model';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../env/environment';

@Injectable({
  providedIn: 'root'
})
export class CertificateService {

  constructor(private http: HttpClient) { }

  getMyCertificates(): Observable<CertificatePreviewDTO[]> {
    return this.http.get<CertificatePreviewDTO[]>(`${environment.apiHost}/certificates/my`);
  }
}