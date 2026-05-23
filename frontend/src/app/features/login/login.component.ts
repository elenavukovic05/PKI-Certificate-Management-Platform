import { ChangeDetectorRef, Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent {
  username = '';
  password = '';
  code = '';
  errorMessage = '';
  requires2FA = false;

  constructor(
    private authService: AuthService, 
    private router: Router, 
    private cdr: ChangeDetectorRef
  ) {}

  login() {
    this.errorMessage = '';
    
    if (this.requires2FA) {
      this.verify2FACode();
    } else {
      this.performLogin();
    }
  }

  private performLogin() {
    this.authService.login(this.username, this.password).subscribe({
      next: (response) => {
        console.log('Login response:', response);

        if (response.requires2FA) {
          this.requires2FA = true;
          this.errorMessage = '';
          this.cdr.detectChanges();
        } else {
          this.navigateBasedOnRole();
        }
      },
      error: (err) => {
        this.errorMessage = 'invalid username or password';
        this.cdr.detectChanges();
      }
    });
  }

  private verify2FACode() {
  const code = parseInt(this.code, 10);
  
  if (!code || this.code.length !== 6) {
    this.errorMessage = 'please enter a valid 6-digit code';
    return;
  }

  this.authService.verify2FA(code).subscribe({
    next: (response) => {
      console.log('2FA verification successful');
      this.requires2FA = false;
      
      this.navigateBasedOnRole();
      
      setTimeout(() => {
        this.authService.notify2FAChanged();
      }, 100);
    },
    error: (err) => {
      this.errorMessage = err.error?.error || 'Invalid 2FA code. Please try again.';
      this.code = '';
      this.cdr.detectChanges();
    }
  });
}

  private navigateBasedOnRole() {
    const role = this.authService.getRole();
    
    if (role === 'CA_USER') {
      this.router.navigate(['/issue-certificate']);
    } else if (role === 'ADMIN') {
      this.router.navigate(['/create-certificates']);
    } else {
      this.router.navigate(['/my-certificates']);
    }
  }
}