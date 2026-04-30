import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CertificatePreviewDTO } from '../../core/models/certificate.model';
import { CommonModule } from '@angular/common';
import { CertificateService } from '../../core/services/certificate.service';

@Component({
  selector: 'app-certificate-preview',
  imports: [CommonModule],
  templateUrl: './certificate-preview.component.html',
  styleUrls: ['./certificate-preview.component.css']
})
export class CertificatePreviewComponent implements OnInit {
  certificates: CertificatePreviewDTO[] = [];
  selectedCert: CertificatePreviewDTO | null = null;
  activeTab: 'general' | 'details' = 'general';

  constructor(private certService: CertificateService, private cdr: ChangeDetectorRef) {}

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
}