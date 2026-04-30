import { Component } from '@angular/core';
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
    private router: Router
  ) {}

  ngOnInit(): void {
    this.certService.getOrganizationCertificates().subscribe({
      next: (data) => {
        this.issuers = data.filter(c => c.isCA);
      },
      error: () => {
        this.errorMessage = 'Failed to load issuers.';
      }
    });
  }

  submit(): void {
    this.successMessage = '';
    this.errorMessage = '';

    this.certService.createCertificateAsCAUser(this.dto).subscribe({
      next: () => {
        this.successMessage = 'Certificate successfully created!';
      },
      error: (err) => {
        this.errorMessage = err.error || 'Failed to create certificate.';
      }
    });
  }
}
