import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CACertificatePreviewDTO, CertificatePreviewDTO, CertificateRequestDTO } from '../../core/models/certificate.model';
import { OrganizationDTO } from '../../core/models/organization.model';
import { CertificateService } from '../../core/services/certificate.service';

@Component({
  selector: 'app-create-certificates',
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './create-certificates.component.html',
  styleUrl: './create-certificates.component.css',
})
export class CreateCertificatesComponent {

  constructor(private certificateService: CertificateService,
    private cdr: ChangeDetectorRef
  ) {}
  
  issuers: CACertificatePreviewDTO[] = [];
  organizations : OrganizationDTO[] = [];
  users : any[] = []

  errorMessage: string = '';
  successMessage: string = '';

  certificateForm = new FormGroup({
    commonName: new FormControl('', [Validators.required]),
    name: new FormControl('', [Validators.required]),
    lastName: new FormControl('', [Validators.required]),
    organization: new FormControl('', Validators.required),
    organizationalUnit: new FormControl('', Validators.required),
    email: new FormControl('', Validators.required),
    country: new FormControl('', [Validators.required]),
    type : new FormControl('ROOT', Validators.required),
    startDate : new FormControl('', [Validators.required]),
    endDate : new FormControl('', [Validators.required]),
    issuerId: new FormControl<number | null>(null),
    organizationId: new FormControl<number | null>(null),
    isCA: new FormControl(false),
    digitalSignature: new FormControl(false),
    keyEncipherment: new FormControl(false),
    keyCertSign: new FormControl(false),
    crlSign: new FormControl(false),
    ownerId: new FormControl<number | null>(null),
  }); 
  

  get isRoot() {
    return this.certificateForm.controls.type.value === 'ROOT';
  }
  get isEndEntity(){
    return this.certificateForm.controls.type.value === 'END_ENTITY';
  }

  get isNotRoot() {
    return this.certificateForm.controls.type.value === 'INTERMEDIATE'
        || this.certificateForm.controls.type.value === 'END_ENTITY';
  }
  get email() {
    return this.certificateForm.controls.email;
  }
  get commonName() {
    return this.certificateForm.controls.commonName;
  }
  get name() {
    return this.certificateForm.controls.name;
  }
  get lastName() {
    return this.certificateForm.controls.lastName;
  }
  get organization() {
    return this.certificateForm.controls.organization;
  }
  get country() {
    return this.certificateForm.controls.country;
  }
  get organizationalUnit() {
    return this.certificateForm.controls.organizationalUnit;
  }
  get startDate(){
    return this.certificateForm.controls.startDate;
  }
  get endDate(){
    return this.certificateForm.controls.endDate;
  }
  get type(){
    return this.certificateForm.controls.type;
  }
  ngOnInit() {
    this.loadIssuers();
    this.loadOrganizations();
    this.loadUsers();

    this.certificateForm.controls.type.valueChanges.subscribe(type => {
    if (type === 'ROOT' || type === 'INTERMEDIATE') {
      this.certificateForm.patchValue({
        isCA: true,
        digitalSignature: true,
        keyEncipherment: false,
        keyCertSign: true,
        crlSign: true
      });
    }

    if (type === 'END_ENTITY') {
      this.certificateForm.patchValue({
        isCA: false,
        digitalSignature: true,
        keyEncipherment: true,
        keyCertSign: false,
        crlSign: false
      });
    }
  });
  }
  loadUsers(){
    this.certificateService.getUsers().subscribe({
      next: (data) => {
        this.users = data;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Failed to load users.';
        this.cdr.detectChanges();
      }
    });
  }
  loadIssuers() {
    this.certificateService.getCACertificates().subscribe({
      next: (data) => {
        this.issuers = data;
      },
      error: (err) => {
        this.errorMessage = err.error || 'Error loading issuers';
      }
    });
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
    if (this.certificateForm.invalid) {
      this.errorMessage = 'Please fill all required fields correctly';
      this.certificateForm.markAllAsTouched();
      return;
    }
    const formValue = this.certificateForm.getRawValue();
    if (formValue.type === 'ROOT' && !formValue.organizationId) {
      this.errorMessage = 'Please choose organization for ROOT certificate';
      return;
    }

    if (formValue.type !== 'ROOT' && !formValue.issuerId) {
      this.errorMessage = 'Please choose issuer';
      return;
    }
    this.successMessage = '';
    this.errorMessage = '';
    this.cdr.detectChanges();
    
    const dto: CertificateRequestDTO = {
      commonName: formValue.commonName ?? '',
      name: formValue.name ?? '',
      lastName: formValue.lastName ?? '',
      organization: formValue.organization ?? '',
      email: formValue.email ?? '',
      organizationalUnit: formValue.organizationalUnit ?? '',
      country: formValue.country ?? '',
      startDate: formValue.startDate ?? '',
      endDate: formValue.endDate ?? '',
      issuerId: formValue.type === 'ROOT' ? null : formValue.issuerId,
      organizationId: formValue.type === 'ROOT' ? formValue.organizationId : null,
      isCA: formValue.isCA ?? false,
      digitalSignature: formValue.digitalSignature ?? false,
      keyEncipherment: formValue.keyEncipherment ?? false,
      keyCertSign: formValue.keyCertSign ?? false,
      crlSign: formValue.crlSign ?? false,
      type: formValue.type as 'ROOT' | 'INTERMEDIATE' | 'END_ENTITY',
      ownerId : formValue.type === 'END_ENTITY' ? formValue.ownerId : null,
  };
    this.certificateService.createCertificateAsAdmin(dto).subscribe({
      next: () => {
        this.successMessage = 'Certificate created successfully';
        this.errorMessage = '';
        this.certificateForm.reset();
      },
      error: (err) => {
        this.errorMessage = err.error || 'Something went wrong';
        this.successMessage = '';
      }
    });
  }


}
