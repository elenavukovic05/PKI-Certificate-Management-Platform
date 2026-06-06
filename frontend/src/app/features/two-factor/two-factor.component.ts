import { Component, EventEmitter, Input, Output, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TwoFactorAuthService } from '../../core/services/two-factor.service';
import * as QRCode from 'qrcode';

@Component({
  selector: 'app-two-factor-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './two-factor.component.html',
  styleUrls: ['./two-factor.component.css']
})
export class TwoFactorModalComponent implements OnInit {
  @Input() mode: 'enable' | 'disable' = 'enable';
  @Output() closed = new EventEmitter<void>();
  @Output() success = new EventEmitter<boolean>();

  step: 'qr' | 'verify' = 'qr';
  secret: string = '';
  qrUrl: string = '';
  code: string = '';
  errorMessage: string = '';
  loading: boolean = false;

  constructor(private twoFactorService: TwoFactorAuthService) {}

  ngOnInit(): void {
    if (this.mode === 'enable') {
      this.startSetup();
    } else {
      this.step = 'verify';
    }
  }

  private startSetup(): void {
    this.loading = true;
    this.errorMessage = '';
    
    this.twoFactorService.setup().subscribe({
      next: async (res) => {
        this.secret = res.secret;
        try {
          this.qrUrl = await QRCode.toDataURL(res.qrUrl);
        } catch (err) {
          this.errorMessage = 'Failed to generate QR code.';
        }
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = err.error || 'Failed to setup 2FA. Please try again.';
        this.loading = false;
      }
    });
  }

  proceedToVerify(): void {
    this.step = 'verify';
    this.errorMessage = '';
  }

  confirm(): void {
  if (this.code.length !== 6) {
    this.errorMessage = 'Please enter a 6-digit code.';
    return;
  }

  this.loading = true;
  this.errorMessage = '';

  const code = parseInt(this.code, 10);

  if (this.mode === 'enable') {
    console.log('Enabling 2FA with:', { secret: this.secret, code: code });
    
    this.twoFactorService.enable(this.secret, code).subscribe({
      next: (response) => {
        console.log('Enable success:', response);
        this.loading = false;
        this.success.emit(true);
      },
      error: (err) => {
        console.error('Enable error details:', {
          status: err.status,
          statusText: err.statusText,
          body: err.error,
          message: err.message
        });
        
        if (typeof err.error === 'string') {
          this.errorMessage = err.error;
        } else if (err.error?.message) {
          this.errorMessage = err.error.message;
        } else if (err.message) {
          this.errorMessage = err.message;
        } else {
          this.errorMessage = `Error ${err.status}: Invalid code. Please try again.`;
        }
        
        this.loading = false;
      }
    });
  } else {
    console.log('Disabling 2FA with code:', code);
    
    this.twoFactorService.disable(code).subscribe({
      next: (response) => {
        console.log('Disable success:', response);
        this.loading = false;
        this.success.emit(false);
      },
      error: (err) => {
        console.error('Disable error details:', {
          status: err.status,
          statusText: err.statusText,
          body: err.error,
          message: err.message
        });
        
        if (typeof err.error === 'string') {
          this.errorMessage = err.error;
        } else if (err.error?.message) {
          this.errorMessage = err.error.message;
        } else if (err.message) {
          this.errorMessage = err.message;
        } else {
          this.errorMessage = `Error ${err.status}: Invalid code. Please try again.`;
        }
        
        this.loading = false;
      }
    });
  }
}

  cancel(): void {
    this.closed.emit();
  }
}