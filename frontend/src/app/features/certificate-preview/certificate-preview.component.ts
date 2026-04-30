import { Component, OnInit } from '@angular/core';
import { CertificatePreviewDTO } from '../../models/certificate.model';
import { CertificateService } from '../../services/certificate.service';
import { CommonModule } from '@angular/common';

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

  mockThumbprint = '2b7ccc918c8c51eaa694b7f253263c2ae861304bf2f5c26eea8e6eca9b72cb73';
  mockPublicKey = '7da1faa3668504bf1bae26a1cb436c4b065b5457aaa3c88246de5f5491ab7a39';

  constructor(private certService: CertificateService) {}

  ngOnInit(): void {
    this.certService.getMyCertificates().subscribe({
      next: (data) => {
        this.certificates = data;
        if (data.length > 0) {
          this.selectedCert = data[0]; 
        }
      },
      error: (err) => console.error('Error fetching certificates', err)
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