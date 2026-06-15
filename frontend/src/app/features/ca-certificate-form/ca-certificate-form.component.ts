import { ChangeDetectorRef, Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CertificateService } from '../../core/services/certificate.service';
import { CertificatePreviewDTO, CertificateRequestDTO } from '../../core/models/certificate.model';

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
  users: any[] = [];
  isDevice: boolean = false;

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
    pathLength :null,
    subjectAlternativeNames : [],
    organizationId: null,
    type: 'END_ENTITY',
    ownerId: null
  };

  constructor(
    private certService: CertificateService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const today = new Date().toISOString().split('T')[0];
    this.dto.startDate = today;
    
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

    this.certService.getUsers().subscribe({
      next: (data) => {
        this.users = data;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Failed to load users.';
        this.cdr.detectChanges();
      }
    });
  }
  addSan(): void {
    this.dto.subjectAlternativeNames.push('');
  }

  removeSan(index: number): void {
    this.dto.subjectAlternativeNames.splice(index, 1);
  }
  submit(): void {
    this.successMessage = '';
    this.errorMessage = '';
    this.cdr.detectChanges();

    if(this.dto.type !== 'INTERMEDIATE'){
      this.dto.pathLength = null;
    }
    if (this.dto.type === 'INTERMEDIATE' && !Number.isInteger(this.dto.pathLength)) {
      this.errorMessage = 'Path length must be a whole number.';
      this.cdr.detectChanges();
      return;
    }
    if (this.dto.type === 'INTERMEDIATE' && this.dto.pathLength !== null && this.dto.pathLength < 0) {
      this.errorMessage = 'Path length cannot be negative.';
      this.cdr.detectChanges();
      return;
    }

    if (!this.dto.issuerId) {
      this.errorMessage = 'Please select an issuer.';
      this.cdr.detectChanges();
      return;
    }
    const endDate = new Date(this.dto.endDate);
    const now = new Date();

    if (endDate <= now) {
      this.errorMessage = 'End date must be in the future';
      this.cdr.detectChanges();
      return;
    }
    this.successMessage = '';
    this.errorMessage = '';
    this.cdr.detectChanges();
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(this.dto.email)) {
      this.errorMessage = 'Please enter a valid email address.';
      this.cdr.detectChanges();
      return;
    }

    if (this.dto.type === 'END_ENTITY') {
      if (!this.isDevice && !this.dto.ownerId) {
        this.errorMessage = 'Please select a user.';
        this.cdr.detectChanges();
        return;
      }

      if (this.isDevice) {
        this.dto.ownerId = null;
      }
    }

    if (this.dto.type !== 'END_ENTITY') {
      this.dto.subjectAlternativeNames = [];
    }
    if (this.dto.type === 'END_ENTITY') {
      this.dto.subjectAlternativeNames = this.dto.subjectAlternativeNames.filter(san => san.trim() !== '');
    }
     //adjusted because of time zone difference in java date format on server
    const startDateAdjusted = new Date(this.dto.startDate);
    startDateAdjusted.setHours(startDateAdjusted.getHours() - 2);

    const endDateAdjusted = new Date(this.dto.endDate);
    endDateAdjusted.setHours(endDateAdjusted.getHours() - 2);

    const dtoToSend: CertificateRequestDTO = {
      ...this.dto,
      startDate: startDateAdjusted.toISOString(),
      endDate: endDateAdjusted.toISOString()
    };

    this.certService.createCertificateAsCAUser(dtoToSend).subscribe({
      next: () => {
        this.successMessage = 'Certificate successfully created!';
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.errorMessage =
          typeof err.error === 'string'
            ? err.error
            : err.error?.message || err.message || 'Something went wrong';

          this.successMessage = '';
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
      this.dto.ownerId = null;
    } else {
      this.dto.isCA = false;
      this.dto.keyCertSign = false;
      this.dto.pathLength = null
    }
    this.cdr.detectChanges();
  }
}
