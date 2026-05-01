import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CertificatePreviewDTO, CertificateRequestDTO } from '../models/certificate.model';
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

  getOrganizationCertificates(): Observable<CertificatePreviewDTO[]> {
    return this.http.get<CertificatePreviewDTO[]>(`${environment.apiHost}/certificates/organization`);
  }

  createCertificateAsCAUser(dto: CertificateRequestDTO): Observable<string> {
    return this.http.post(`${environment.apiHost}/certificates/ca/create-certificate`, dto, { responseType: 'text' });
  }

  getUsers(): Observable<any[]> {
    return this.http.get<any[]>(`${environment.apiHost}/users/regular`);
  }
}