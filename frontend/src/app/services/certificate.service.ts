import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { CertificatePreviewDTO } from '../models/certificate.model';
import { HttpClient } from '@angular/common/http';
import { CertificateRequestDTO } from '../models/certificate.model';
import { environment } from '../../env/environment';

@Injectable({
  providedIn: 'root'
})
export class CertificateService {

  constructor(private http: HttpClient) { }

  getMyCertificates(): Observable<CertificatePreviewDTO[]> {
    // Pravi poziv kada proradi login:
    // return this.http.get<CertificatePreviewDTO[]>('/api/v1/certificates/my');

    // HC vrednosti za razvoj UI-ja
    const mockData: CertificatePreviewDTO[] = [
      {
        id: 1,
        subjectName: 'CN=*.excalidraw.com, O=Excalidraw Inc, C=US',
        issuerName: 'CN=R12, O=Let\'s Encrypt, C=US',
        type: 'END_ENTITY',
        status: 'VALID',
        startDate: new Date('2026-03-20T07:05:38'),
        endDate: new Date('2026-06-18T08:05:37'),
        isCA: false,
        digitalSignature: true,
        keyEncipherment: true,
        keyCertSign: false,
        crlSign: false,
        certificatePem: '-----BEGIN CERTIFICATE-----\nMIIGEzCCA/ugAwIBAgIQf+...\n-----END CERTIFICATE-----'
      }
    ];

    return of(mockData);
  }

  getOrganizationCertificates(): Observable<CertificatePreviewDTO[]> {
    return this.http.get<CertificatePreviewDTO[]>(`${environment.apiHost}/certificates/organization`);
  }

  createCertificateAsCAUser(dto: CertificateRequestDTO): Observable<string> {
    return this.http.post(`${environment.apiHost}/certificates/ca/create-certificate`, dto, { responseType: 'text' });
  }
}