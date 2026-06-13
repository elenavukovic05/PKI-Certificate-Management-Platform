import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { CryptoService } from '../../core/services/crypto-service';
import { AuthService } from '../../core/services/auth.service';
import { ActivateUserDTO } from '../../core/models/activate-user.model';

@Component({
  selector: 'app-activate-user',
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './activate-user.component.html',
  styleUrl: './activate-user.component.css',
})
export class ActivateUserComponent {
  errorMessage = '';
  successMessage = '';
  selectedPrivateKey : File | null = null;
  encryptedActivationChallenge : string | null = '';
  activationToken : string = '';

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
    this.activationToken = this.route.snapshot.queryParamMap.get('token') ?? '';

    if (!this.activationToken) {
      this.errorMessage = 'Activation token is missing.';
      return;
    }
    this.authService.getActivationChallenge(this.activationToken).subscribe({
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

    const dto : ActivateUserDTO = {
      activationToken : this.activationToken,
      decryptedActivationChallenge : decryptedActivationChallenge
    }
    this.authService.activateUser(dto).subscribe({
      next: () => {
        this.successMessage = 'Account activated successfully. You can now log in.';
        console.log("ACTIVATED")
        this.errorMessage = '';

        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 2000);
      },
      error: (err) => {
        this.errorMessage = err.error || 'Activation failed.';
        this.successMessage = '';
      }
    });
  }
}
