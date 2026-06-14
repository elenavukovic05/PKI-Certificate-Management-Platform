import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { UserService } from '../../core/services/user.service';
import { ChangePasswordDTO } from '../../core/models/change-password.model';

@Component({
  selector: 'app-change-password',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './change-password.component.html',
  styleUrl: './change-password.component.css',
})
export class ChangePasswordComponent {
  username = '';
  errorMessage = '';
  successMessage = '';

  changePasswordForm = new FormGroup({
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
  
  get newPassword() {
    return this.changePasswordForm.controls.newPassword;
  }

  get confirmPassword() {
    return this.changePasswordForm.controls.confirmPassword;
  } 
  ngOnInit(){
    this.username = this.route.snapshot.queryParamMap.get('username') ?? '';

    if(!this.username){
      this.errorMessage = "Username is null";
    }
  }

  onSubmit() {
    if (this.changePasswordForm.invalid) {
      this.changePasswordForm.markAllAsTouched();
      return;
    }

    const newPassword = this.newPassword.value ?? '';
    const confirmPassword = this.confirmPassword.value ?? '';

    if (newPassword !== confirmPassword) {
      this.errorMessage = 'Passwords do not match.';
      return;
    }
    const dto : ChangePasswordDTO = {
      username : this.username,
      newPassword : newPassword
    }
    this.userService.changePassword(dto).subscribe({
      next: () => {
        this.successMessage = 'Password changed successfully. You can now log in.';
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
