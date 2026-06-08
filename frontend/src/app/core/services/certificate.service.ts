import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AutogenerateRequest, AutogenerateResponse, CACertificatePreviewDTO, CertificatePreviewDTO, CertificateRequestDTO, CsrRequest, Pkcs12RequestDTO } from '../models/certificate.model';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../env/environment';
import { OrganizationDTO } from '../models/organization.model';

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
  createCertificateAsAdmin(dto : CertificateRequestDTO) : Observable<string> {
    return this.http.post(`${environment.apiHost}/certificates/create-certificate`, dto, { responseType: 'text' });
  }
  getOrganizations(): Observable<OrganizationDTO[]> {
    return this.http.get<OrganizationDTO[]>(`${environment.apiHost}/organizations/all`);
  }
  addOrganization(dto: string): Observable<string> {
    return this.http.post(`${environment.apiHost}/admin/add-organization`, dto, {
      responseType: 'text',
      headers: { 'Content-Type': 'text/plain' }
    }
  );
  }
  getCACertificates(): Observable<CACertificatePreviewDTO[]> {
    return this.http.get<CACertificatePreviewDTO[]>(`${environment.apiHost}/certificates/ca-certificates`);
  }
  getAllCertificates() : Observable<CertificatePreviewDTO[]>{
      return this.http.get<CertificatePreviewDTO[]>(`${environment.apiHost}/certificates/all`);
  }

  downloadPem(id: number): Observable<Blob> {
    return this.http.get(`${environment.apiHost}/certificates/${id}/download/pem`, {
      responseType: 'blob'
    });
  }

  downloadCer(id: number): Observable<Blob> {
    return this.http.get(`${environment.apiHost}/certificates/${id}/download/cer`, {
      responseType: 'blob'
    });
  }

  downloadPkcs12(id: number, dto: Pkcs12RequestDTO) : Observable<Blob> {
    return this.http.post(`${environment.apiHost}/certificates/${id}/download/pkcs12`, dto, {
      responseType: 'blob'
    });
  }

  getAvailableCAs() {
    return this.http.get<CACertificatePreviewDTO[]>(`${environment.apiHost}/certificates/ca-certificates`);
  }

  uploadCsr(request: CsrRequest) {
    return this.http.post<string>(`${environment.apiHost}/certificates/csr/upload`, request, { responseType: 'text' as 'json' });
  }

  autogenerate(request: AutogenerateRequest) {
    return this.http.post<AutogenerateResponse>(`${environment.apiHost}/certificates/csr/autogenerate`, request);
  }
}