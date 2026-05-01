import { ChangeDetectorRef, Component } from '@angular/core';
import { CertificateService } from '../../core/services/certificate.service';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { OrganizationDTO } from '../../core/models/organization.model';

@Component({
  selector: 'app-add-organizations',
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './add-organizations.component.html',
  styleUrl: './add-organizations.component.css',
})
export class AddOrganizationsComponent {
  constructor(private certificateService : CertificateService,private cdr: ChangeDetectorRef
  ) {}

  errorMessage : string = '';
  successMessage : string = '';

  organizationForm = new FormGroup({
    name: new FormControl('', [Validators.required]),
  });
  get name() {
    return this.organizationForm.controls.name;
  } 
  
  onSubmit(){
      if (this.organizationForm.invalid) {
        this.errorMessage = 'Please fill all required fields correctly';
        this.organizationForm.markAllAsTouched();
        return;
      }
      const formValue = this.organizationForm.getRawValue();
      
      this.successMessage = '';
      this.errorMessage = '';
      
      const dto: string = formValue.name ?? '';
    
      this.certificateService.addOrganization(dto).subscribe({
        next: () => {
          this.successMessage = 'Organization created successfully';
          this.errorMessage = '';
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.errorMessage = err.error || 'Something went wrong';
          this.successMessage = '';
        }
      });
    }
}
