import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { UserService } from '../../core/services/user.service';
import { CertificateService } from '../../core/services/certificate.service';
import { OrganizationDTO } from '../../core/models/organization.model';
import { CAUserDTO } from '../../core/models/ca-user.model';

@Component({
  selector: 'app-add-ca-users',
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './add-ca-users.component.html',
  styleUrl: './add-ca-users.component.css',
})
export class AddCaUsersComponent {
  constructor(private userService: UserService,
    private cdr: ChangeDetectorRef, private certificateService : CertificateService
  ) {}

  errorMessage : string = '';
  successMessage : string = '';
  organizations : OrganizationDTO[] = [];

  caUserForm = new FormGroup({
    firstName: new FormControl('', [Validators.required]),
    lastName: new FormControl('', [Validators.required]),
    username: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', Validators.required),
    organizationId: new FormControl<number | null>(null, [Validators.required]),
  });
  get username() {
    return this.caUserForm.controls.username;
  } 
  get password() {
    return this.caUserForm.controls.password;
  }
  get firstName() {
    return this.caUserForm.controls.firstName;
  }
  get lastName() {
    return this.caUserForm.controls.lastName;
  }
  get organizationId(){
    return this.caUserForm.controls.organizationId;
  }

  ngOnInit() {
    this.loadOrganizations();
  }
  loadOrganizations() {
    this.certificateService.getOrganizations().subscribe({
      next: (data) => {
        this.organizations = data;
      },
      error: (err) => {
        this.errorMessage = err.error || 'Error loading organizations';
      }
    });
  }

  onSubmit(){
      if (this.caUserForm.invalid) {
        this.errorMessage = 'Please fill all required fields correctly';
        this.caUserForm.markAllAsTouched();
        return;
      }
      const formValue = this.caUserForm.getRawValue();
      
      this.successMessage = '';
      this.errorMessage = '';
      this.cdr.detectChanges();
      
      const dto: CAUserDTO = {
        username: formValue.username ?? '',
        password: formValue.password ?? '',
        firstName: formValue.firstName ?? '',
        lastName: formValue.lastName ?? '',
        organizationId: formValue.organizationId!
      };
      this.userService.addCAUser(dto).subscribe({
        next: () => {
          this.successMessage = 'CA User created successfully. Activation link has been sent.';
          this.errorMessage = '';
          this.caUserForm.reset();
        },
        error: (err) => {
          this.errorMessage = err.error || 'Something went wrong';
          this.successMessage = '';
        }
      });
    }
}
