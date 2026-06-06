import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { UserService } from '../../core/services/user.service';

@Component({
  selector: 'app-activate-ca-user',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './activate-ca-user.html',
  styleUrl: './activate-ca-user.css',
})
export class ActivateCaUser {
  token = '';
  errorMessage = '';
  successMessage = '';

  activationForm = new FormGroup({
    newPassword: new FormControl('', [
      Validators.required,
      Validators.minLength(8)
    ]),
    confirmPassword: new FormControl('', [
      Validators.required
    ])
  });

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private userService: UserService
  ) {}

  ngOnInit() {
    this.token = this.route.snapshot.queryParamMap.get('token') ?? '';

    if (!this.token) {
      this.errorMessage = 'Activation token is missing.';
    }
  }

  get newPassword() {
    return this.activationForm.controls.newPassword;
  }

  get confirmPassword() {
    return this.activationForm.controls.confirmPassword;
  }

  onSubmit() {
    if (this.activationForm.invalid) {
      this.activationForm.markAllAsTouched();
      return;
    }

    const newPassword = this.newPassword.value ?? '';
    const confirmPassword = this.confirmPassword.value ?? '';

    if (newPassword !== confirmPassword) {
      this.errorMessage = 'Passwords do not match.';
      return;
    }

    this.userService.activateCAUser({
      token: this.token,
      newPassword
    }).subscribe({
      next: () => {
        this.successMessage = 'Account activated successfully. You can now log in.';
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
