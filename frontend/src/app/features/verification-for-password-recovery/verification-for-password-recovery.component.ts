import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { CryptoService } from '../../core/services/crypto-service';
import { VerifyPasswordRecoveryDTO } from '../../core/models/verify-password-recovery.model';

@Component({
  selector: 'app-verification-for-password-recovery',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './verification-for-password-recovery.component.html',
  styleUrl: './verification-for-password-recovery.component.css',
})
export class VerificationForPasswordRecoveryComponent {
  errorMessage = '';
  successMessage = '';
  selectedPrivateKey : File | null = null;
  encryptedActivationChallenge : string | null = '';
  username : string = '';

  onPrivateKeySelected(event : Event){
    const input = event.target as HTMLInputElement;

    if (input.files && input.files.length > 0) {
      this.selectedPrivateKey = input.files[0];
    }
  }
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService,
    private cryptoService : CryptoService
  ) {}

  ngOnInit() {
    console.log('FULL URL:', this.router.url);
    console.log('QUERY PARAMS:', this.route.snapshot.queryParams);
    this.username = this.route.snapshot.queryParamMap.get('username') ?? '';

    if (!this.username) {
      this.errorMessage = 'Username is missing.';
      return;
    }
    this.authService.getActivationChallengeByUsername(this.username).subscribe({
      next: (activationChallenge) => {
        this.encryptedActivationChallenge = activationChallenge;
      },
      error: () => {
        this.errorMessage = 'Failed to load activation challenge.';
      }
    });
  } 

  async onSubmit() {
    event?.preventDefault();
    if(!this.selectedPrivateKey){
      this.errorMessage = 'You must import your private key for activation process!';
      return;
    }
    if (!this.encryptedActivationChallenge) {
      this.errorMessage ='Activation challenge not loaded.';
      return;
    }
    const privateKeyPem = await this.selectedPrivateKey.text();
    const decryptedActivationChallenge = await this.cryptoService.decryptWithPrivateKey(
      privateKeyPem, this.encryptedActivationChallenge);

    const dto : VerifyPasswordRecoveryDTO = {
      username : this.username,
      decryptedActivationChallenge : decryptedActivationChallenge
    }
    this.authService.verifyPasswordRecovery(dto).subscribe({
      next: () => {
        this.successMessage = 'Account verified successfully. You can now change your password';
        console.log("ACTIVATED")
        this.errorMessage = '';
        setTimeout(() => {
          this.router.navigate(['/change-password'], {
            queryParams: { username: this.username }
          });
        }, 2000);
      },
      error: (err) => {
        this.errorMessage = err.error || 'Activation failed.';
        this.successMessage = '';
      }
    });
  }
}
