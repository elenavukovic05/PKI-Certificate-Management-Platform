import { Component, OnInit } from '@angular/core';
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

  ag = {
    commonName: '', name: '', lastName: '',
    organization: '', organizationalUnit: '',
    country: '', email: '',
    issuerId: null as number | null,
    startDate: '', endDate: ''
  };
  agResult: AutogenerateResponse | null = null;
  agError: string = '';

  constructor(private certService: CertificateService, private authService: AuthService) {}

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
    if (!this.csrPem || !this.csrIssuerId || !this.csrStartDate || !this.csrEndDate) {
      this.csrError = 'input all fields.';
      return;
    }
    this.csrError = '';
    this.csrMessage = '';
    this.certService.uploadCsr({
      csrPem: this.csrPem,
      issuerId: this.csrIssuerId,
      startDate: new Date(this.csrStartDate).toISOString(),
      endDate: new Date(this.csrEndDate).toISOString()
    }).subscribe({
      next: (msg) => {this.csrMessage = 'certificate created ' + msg;
      this.modalMessage = 'Certificate has been successfully issued.';
      this.showSuccessModal = true;
      }, 
      error: (err) => this.csrError = err.error || 'error while issuing certificate'
    });
  }

  submitAutogenerate(): void {
    if (!this.ag.commonName || !this.ag.country || !this.ag.issuerId || !this.ag.startDate || !this.ag.endDate) {
      this.agError = 'you need to input all fileds';
      return;
    }
    this.agError = '';
    this.agResult = null;
    this.certService.autogenerate({
      ...this.ag,
      issuerId: this.ag.issuerId!,
      startDate: new Date(this.ag.startDate).toISOString(),
      endDate: new Date(this.ag.endDate).toISOString()
    }).subscribe({
      next: (result) => {
        this.agResult = result;
        this.modalMessage = 'Certificate has been successfully generated.';
        this.showSuccessModal = true;
      },
      error: (err) => this.agError = err.error || 'error while issuing!'
    });
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
}
