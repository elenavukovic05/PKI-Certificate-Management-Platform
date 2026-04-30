import { ChangeDetectorRef, Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CertificateService } from '../../services/certificate.service';
import { CertificatePreviewDTO, CertificateRequestDTO } from '../../models/certificate.model';

@Component({
  selector: 'app-ca-certificate-form',
  imports: [CommonModule, FormsModule],
  templateUrl: './ca-certificate-form.component.html',
  styleUrl: './ca-certificate-form.component.css',
})
export class CaCertificateFormComponent {
  issuers: CertificatePreviewDTO[] = [];
  successMessage = '';
  errorMessage = '';

  dto: CertificateRequestDTO = {
    commonName: '',
    name: '',
    lastName: '',
    organization: '',
    email: '',
    organizationalUnit: '',
    country: '',
    startDate: '',
    endDate: '',
    issuerId: null,
    isCA: false,
    digitalSignature: false,
    keyEncipherment: false,
    keyCertSign: false,
    crlSign: false,
    organizationId: null,
    type: 'END_ENTITY'
  };

  constructor(
    private certService: CertificateService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.certService.getOrganizationCertificates().subscribe({
      next: (data) => {
        this.issuers = data.filter(c => c.isCA);
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Failed to load issuers.';
        this.cdr.detectChanges();
      }
    });
  }

  submit(): void {
    this.successMessage = '';
    this.errorMessage = '';
    this.cdr.detectChanges();

    
    if (!this.dto.issuerId) {
      this.errorMessage = 'Please select an issuer.';
      this.cdr.detectChanges();
      return;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(this.dto.email)) {
      this.errorMessage = 'Please enter a valid email address.';
      this.cdr.detectChanges();
      return;
    }

    this.certService.createCertificateAsCAUser(this.dto).subscribe({
      next: () => {
        this.successMessage = 'Certificate successfully created!';
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.errorMessage = err.error || 'Failed to create certificate.';
        this.cdr.detectChanges();
      }
    });
  }

  extractCN(subjectName: string): string {
    const match = subjectName.match(/CN=([^,]+)/);
    return match ? match[1] : subjectName;
  }

  onTypeChange(): void {
    if (this.dto.type === 'INTERMEDIATE') {
      this.dto.isCA = true;
      this.dto.keyCertSign = true;
      this.dto.digitalSignature = true;
    } else {
      this.dto.isCA = false;
      this.dto.keyCertSign = false;
    }
    this.cdr.detectChanges();
  }
}
