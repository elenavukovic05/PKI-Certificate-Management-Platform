  import { ChangeDetectorRef, Component } from '@angular/core';
  import { CertificateService } from '../../core/services/certificate.service';
  import { CertificatePreviewDTO } from '../../core/models/certificate.model';
  import { FormsModule, ReactiveFormsModule } from '@angular/forms';
  import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { RevokeCertificateRequestDTO } from '../../core/models/revoke-certificate-request.model';

  @Component({
    selector: 'app-all-certificates',
    imports: [ReactiveFormsModule, CommonModule, FormsModule],
    templateUrl: './all-certificates.component.html',
    styleUrl: './all-certificates.component.css',
  })
  export class AllCertificatesComponent {

    constructor(private certificateService : CertificateService, private cdr: ChangeDetectorRef, private authService : AuthService){}
    role: string | null = null;
    errorMessage : string = '';
    successMessage : string = '';
    certificates : CertificatePreviewDTO[] = [];
    revocationReason = 'UNSPECIFIED';
    showPkcs12Modal = false;
    showRevokeCertificateModal = false;
    pkcs12Password = '';
    showPassword = false;
    selectedCertId: number | null = null;

    loadCertificates(){
      if (this.role == 'ADMIN'){
        this.certificateService.getAllCertificates().subscribe({
          next: (data) => {
            this.certificates = data;
            this.cdr.detectChanges();
          },
          error: () => {
            this.errorMessage = 'Failed to load users.';
            this.cdr.detectChanges();
          }
        });
      } else if (this.role == 'CA_USER'){
        this.certificateService.getOrganizationCertificates().subscribe({
          next: (data) => {
            this.certificates = data;
            this.cdr.detectChanges();
          },
          error: () => {
            this.errorMessage = 'Failed to load ceritificates.';
            this.cdr.detectChanges();
          }
        });
      }
      
    }
    ngOnInit(){
      this.role = this.authService.getRole();
      this.loadCertificates();
    }
    
    downloadCertificate(certId: number, format: 'pem' | 'cer'): void {
      if (!certId) return;

      const filename = `certificate-${certId}.${format}`;
      const downloadMethod = format === 'pem' 
        ? this.certificateService.downloadPem(certId) 
        : this.certificateService.downloadCer(certId);

      downloadMethod.subscribe({
        next: (blob: Blob) => {
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

    openPkcs12Modal(certId: number): void {
      this.selectedCertId = certId;
      this.pkcs12Password = '';
      this.showPkcs12Modal = true;
      this.cdr.detectChanges();
    }

    confirmPkcs12Download(): void {
      if (!this.selectedCertId || !this.pkcs12Password) return;

      this.certificateService.downloadPkcs12(
          this.selectedCertId, 
          { password: this.pkcs12Password }
      ).subscribe({
          next: (blob: Blob) => {
              const url = window.URL.createObjectURL(blob);
              const a = document.createElement('a');
              a.href = url;
              a.download = `certificate-${this.selectedCertId}.p12`;
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
      this.certificateService.revokeCertificate(this.selectedCertId, dto).subscribe({
      next: () => {
        this.successMessage = 'Certificate revoked successfully';
        this.errorMessage = '';
        this.showRevokeCertificateModal = false;
        this.selectedCertId = null;
        this.revocationReason = 'UNSPECIFIED';
        this.loadCertificates();
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

  }
