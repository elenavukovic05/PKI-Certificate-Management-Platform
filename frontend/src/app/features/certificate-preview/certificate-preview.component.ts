import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CertificatePreviewDTO } from '../../core/models/certificate.model';
import { CommonModule } from '@angular/common';
import { CertificateService } from '../../core/services/certificate.service';
import { FormsModule } from '@angular/forms';
import { RevokeCertificateRequestDTO } from '../../core/models/revoke-certificate-request.model';

@Component({
  selector: 'app-certificate-preview',
  imports: [CommonModule, FormsModule],
  templateUrl: './certificate-preview.component.html',
  styleUrls: ['./certificate-preview.component.css']
})
export class CertificatePreviewComponent implements OnInit {
  certificates: CertificatePreviewDTO[] = [];
  selectedCert: CertificatePreviewDTO | null = null;
  activeTab: 'general' | 'details' = 'general';
  errorMessage = '';
  successMessage = '';
  showPkcs12Modal = false;
  pkcs12Password = '';
  showPassword = false;
  selectedCertId: number | null = null;
  showRevokeCertificateModal = false;
  revocationReason = 'UNSPECIFIED';

  constructor(private certService: CertificateService, private cdr: ChangeDetectorRef) {}

  revokeCertificate(certId : number) : void{
    this.selectedCertId = certId;
    this.showRevokeCertificateModal = true;
    this.cdr.detectChanges();
  }
  chooseRevocationReason(){
    if(!this.selectedCertId)
        return;
  
    const dto : RevokeCertificateRequestDTO = {
        reason : this.revocationReason
    };
    this.certService.revokeCertificate(this.selectedCertId, dto).subscribe({
        next: () => {
          this.successMessage = 'Certificate revoked successfully';
          this.errorMessage = '';
          this.showRevokeCertificateModal = false;
          this.selectedCertId = null;
          this.revocationReason = 'UNSPECIFIED';
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.log('SERVER ERROR:', err);
          console.log('SERVER ERROR BODY:', err.error);
  
          this.errorMessage =
            typeof err.error === 'string'
              ? err.error
              : err.error?.message || err.message || 'Something went wrong';
  
            this.successMessage = '';
            this.cdr.detectChanges();
        }
    });
  }

  ngOnInit(): void {
    this.certService.getMyCertificates().subscribe({
      next: (data) => {
        console.log('data arrived:', data);
        console.log('num of certificates:', data?.length);
        
        if (data && data.length > 0) {
          this.certificates = [...data];
          this.selectedCert = data[0];
          console.log('selected cert:', this.selectedCert);
          this.cdr.detectChanges();
        }
      },
      error: (err) => console.error('error:', err)
    });
  }


  setTab(tab: 'general' | 'details'): void {
    this.activeTab = tab;
  }

  extractField(dn: string, field: string): string {
    if (!dn) return '<Not part of certificate>';
    const parts = dn.split(',');
    for (let part of parts) {
      part = part.trim();
      if (part.startsWith(field + '=')) {
        return part.substring(field.length + 1);
      }
    }
    return '<Not part of certificate>';
  }

  downloadCertificate(format: 'pem' | 'cer'): void {
    if (!this.selectedCert) return;

    const certId = this.selectedCert.id;
    const filename = `certificate-${certId}.${format}`;

    const downloadMethod = format === 'pem' 
      ? this.certService.downloadPem(certId) 
      : this.certService.downloadCer(certId);

    downloadMethod.subscribe({
      next: (blob: Blob) => {
        // create url from blob
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = filename;
        
        document.body.appendChild(a);
        a.click();
        
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
      },
      error: (err) => {
        console.error(`error while downloading ${format.toUpperCase()} file:`, err);
        alert('you do not have permission to download this certificate or the file is unavailable');
      }
    });
  }

  openPkcs12Modal(): void {
    if (!this.selectedCert) return;

    this.pkcs12Password = '';
    this.showPkcs12Modal = true;
    this.cdr.detectChanges();
  }

  confirmPkcs12Download(): void {
    if (!this.selectedCert || !this.pkcs12Password.trim()) return;

    const certId = this.selectedCert.id;

    this.certService.downloadPkcs12(
        certId, 
        { password: this.pkcs12Password }
    ).subscribe({
        next: (blob: Blob) => {
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `certificate-${certId}.p12`;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            window.URL.revokeObjectURL(url);
            this.showPkcs12Modal = false;
            this.pkcs12Password = '';
            this.cdr.detectChanges();
          },
          error: () => {
              alert('Failed to download PKCS12');
              this.cdr.detectChanges();
          }
      });
    }

}