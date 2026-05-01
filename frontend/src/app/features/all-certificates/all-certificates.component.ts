  import { ChangeDetectorRef, Component } from '@angular/core';
  import { CertificateService } from '../../core/services/certificate.service';
  import { CertificatePreviewDTO } from '../../core/models/certificate.model';
  import { ReactiveFormsModule } from '@angular/forms';
  import { CommonModule } from '@angular/common';

  @Component({
    selector: 'app-all-certificates',
    imports: [ReactiveFormsModule, CommonModule],
    templateUrl: './all-certificates.component.html',
    styleUrl: './all-certificates.component.css',
  })
  export class AllCertificatesComponent {

    constructor(private certificateService : CertificateService,private cdr: ChangeDetectorRef){}
    
    errorMessage : string = '';
    certificates : CertificatePreviewDTO[] = [];

    loadCertificates(){
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
    }
    ngOnInit(){
      this.loadCertificates();
    }
    

  }
