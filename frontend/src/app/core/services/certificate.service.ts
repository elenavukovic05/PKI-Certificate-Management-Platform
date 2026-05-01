import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CACertificatePreviewDTO, CertificatePreviewDTO, CertificateRequestDTO } from '../models/certificate.model';
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
}