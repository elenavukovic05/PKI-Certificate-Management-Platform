import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CertificateService } from '../../core/services/certificate.service';
import { CACertificatePreviewDTO, AutogenerateResponse } from '../../core/models/certificate.model';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-csr-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './csr-form.component.html',
  styleUrls: ['./csr-form.component.css']
})
export class CsrFormComponent implements OnInit {
  activeTab: 'upload' | 'autogenerate' = 'upload';
  availableCAs: CACertificatePreviewDTO[] = [];

  csrPem: string = '';
  csrIssuerId: number | null = null;
  csrStartDate: string = '';
  csrEndDate: string = '';
  csrMessage: string = '';
  csrError: string = '';
  showSuccessModal = false;
  modalMessage = '';
  isSubmittingCsr = false;
  isSubmittingAg = false;

  ag = {
    commonName: '', name: '', lastName: '',
    organization: '', organizationalUnit: '',
    country: '', email: '',
    issuerId: null as number | null,
    startDate: '', endDate: ''
  };
  agResult: AutogenerateResponse | null = null;
  agError: string = '';

  constructor(private certService: CertificateService, private authService: AuthService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    const today = new Date().toISOString().split('T')[0];

    this.csrStartDate = today;
    this.ag.startDate = today;

    this.certService.getAvailableCAs().subscribe({
      next: (data) => this.availableCAs = data,
      error: (err) => console.error('error loading CAs', err)
    });
  }

  setTab(tab: 'upload' | 'autogenerate'): void {
    this.activeTab = tab;
    this.csrMessage = '';
    this.csrError = '';
    this.agResult = null;
    this.agError = '';
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;
    const file = input.files[0];
    const reader = new FileReader();
    reader.onload = () => {
      this.csrPem = reader.result as string;
    };
    reader.readAsText(file);
  }

  submitCsr(): void {
  if (this.isSubmittingCsr) return;
  
  if (!this.csrPem || !this.csrIssuerId || !this.csrStartDate || !this.csrEndDate) {
    this.csrError = 'input all fields.';
    return;
  }
  
  this.isSubmittingCsr = true;
  this.csrError = '';
  this.csrMessage = '';
  
  this.certService.uploadCsr({
    csrPem: this.csrPem,
    issuerId: this.csrIssuerId,
    startDate: new Date(this.csrStartDate).toISOString(),
    endDate: new Date(this.csrEndDate).toISOString()
  }).subscribe({
    next: (msg) => {
      console.log('SUCCESS - received message:', msg);
      this.csrMessage = msg;
      this.modalMessage = 'Certificate has been successfully issued.';
      this.showSuccessModal = true;
      this.isSubmittingCsr = false;
      this.cdr.detectChanges();
    },
    error: (err) => {
      console.error('ERROR in subscribe:', err);
      this.csrError = err.message || 'Error issuing certificate';
      this.isSubmittingCsr = false;
      this.cdr.detectChanges();
    },
    complete: () => {
      console.log('Observable completed');
    }
  });
}

  submitAutogenerate(): void {
    if (this.isSubmittingAg)
    
    if (!this.ag.commonName || !this.ag.country || !this.ag.issuerId || !this.ag.startDate || !this.ag.endDate) {
      this.agError = 'you need to input all fields';
      return;
    }
    
    const password = prompt("Enter password to protect your PKCS12 (.p12) file:");
    if (!password) {
      this.agError = 'Password is required to generate the certificate and private key store.';
      return;
    }
    this.isSubmittingAg = true;
    this.agError = '';
    this.agResult = null;
    this.certService.autogenerate({
      ...this.ag,
      issuerId: this.ag.issuerId!,
      startDate: new Date(this.ag.startDate).toISOString(),
      endDate: new Date(this.ag.endDate).toISOString(),
      password: password
    }).subscribe({
      next: (result: AutogenerateResponse) => {
        this.agResult = result;
        this.modalMessage = 'Certificate has been successfully generated.';
        this.showSuccessModal = true;
        this.downloadPkcs12FromBase64(result.pkcs12Base64, `certificate-${result.certificateId}.p12`);
        this.isSubmittingAg = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.agError = err.error || 'error while issuing!';
        this.isSubmittingAg = false;
        this.cdr.detectChanges();
      }
    });
  }

  downloadPkcs12FromBase64(base64String: string, filename: string): void {
    try {
      const byteCharacters = atob(base64String);
      const byteNumbers = new Array(byteCharacters.length);
      for (let i = 0; i < byteCharacters.length; i++) {
        byteNumbers[i] = byteCharacters.charCodeAt(i);
      }
      const byteArray = new Uint8Array(byteNumbers);
      const blob = new Blob([byteArray], { type: 'application/x-pkcs12' });

      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = filename;
      
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      
      window.URL.revokeObjectURL(url);
    } catch (e) {
      console.error("Failed to process and download PKCS12 file", e);
      this.agError = 'Certificate was created, but failed to download the PKCS12 file locally.';
    }
  }

  downloadPem(content: string, filename: string): void {
    const blob = new Blob([content], { type: 'text/plain' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    a.click();
    window.URL.revokeObjectURL(url);
  }

  extractField(dn: string, field: string): string {
    if (!dn) return '';
    const parts = dn.split(',');
    for (let part of parts) {
      part = part.trim();
      if (part.startsWith(field + '=')) return part.substring(field.length + 1);
    }
    return dn;
  }

  openPkcs12FromAutoGenerate(): void {
    if (!this.agResult) return;

    const certId = this.agResult.certificateId;

    const password = prompt("Enter PKCS12 password:");
    if (!password) return;

    this.certService.downloadPkcs12(certId, { password }).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');

        a.href = url;
        a.download = `certificate-${certId}.p12`;

        document.body.appendChild(a);
        a.click();

        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
      },
      error: () => alert("Failed to download PKCS12")
    });
  }
}
