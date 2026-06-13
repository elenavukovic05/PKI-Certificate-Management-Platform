import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { RegisterDTO } from '../../core/models/register.model';
import { OrganizationDTO } from '../../core/models/organization.model';
import { CertificateService } from '../../core/services/certificate.service';
import { CryptoService } from '../../core/services/crypto-service';

@Component({
  selector: 'app-register',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css',
})
export class RegisterComponent {
  constructor(private authService : AuthService, 
    private certificateService : CertificateService ,
    private cryptoService : CryptoService,
    private cdr: ChangeDetectorRef){}

  errorMessage : string = '';
  successMessage : string = '';
  organizations : OrganizationDTO[] = [];
  publicKeyPem : string = '';

  registerForm = new FormGroup({
    username : new FormControl('', [Validators.required]),
    password : new FormControl('', [Validators.required]),
    confirmedPassword : new FormControl('', Validators.required),
    name : new FormControl('', [Validators.required]),
    lastName : new FormControl('', [Validators.required]),
    organizationId: new FormControl<number | null>(null),
    publicKey : new FormControl('', [Validators.required]),
  })

  get username(){
    return this.registerForm.controls.username;
  }
  get password(){
    return this.registerForm.controls.password;
  }
  get confirmedPassword(){
    return this.registerForm.controls.confirmedPassword;
  }
  get name(){
    return this.registerForm.controls.name;
  }
  get lastName(){
    return this.registerForm.controls.lastName;
  }
  get organizationId(){
    return this.registerForm.controls.organizationId;
  }
  get pulbicKey(){
    return this.registerForm.controls.publicKey;
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
  async generateKeyPair(){
    try{
      const publicKeyPem = await this.cryptoService.generateKeyPair();
      this.registerForm.patchValue({
        publicKey: publicKeyPem
      });

      this.successMessage = 'Key pair generated. Public key added to form. Private key downloaded.Save the downloaded private key. You will need it for account activation and password recovery.';
    } catch (error) {
      console.error(error);
      this.errorMessage = 'Error while generating key pair.';
      this.successMessage = '';
    }
    
  }

  ngOnInit(){
    this.loadOrganizations();
  }
  containsDigit(password : string) : boolean{
    for(const ch of password){
      if(ch >= '0' && ch <= '9')
        return true;
    }
    return false;
  }
  containsSpecialCharacters(password: string): boolean {
    for (const ch of password) {
      const isLetter =
        (ch >= 'a' && ch <= 'z') ||
        (ch >= 'A' && ch <= 'Z');

      const isDigit = ch >= '0' && ch <= '9';

      if (!isLetter && !isDigit) {
        return true;
      }
    }

    return false;
  }
  register(){
    
    if (this.registerForm.invalid){
      this.errorMessage = 'Please fill all required fields correctly';
      this.registerForm.markAllAsTouched();
      return ;
    }
    const formValue = this.registerForm.getRawValue();
    
    if(formValue.password != null && formValue.password.length < 8 ){
      this.errorMessage = 'Password must have at least 8 characters';
      return;
    }
    if(formValue.password != null && !this.containsSpecialCharacters(formValue.password)){
      this.errorMessage = 'Password must have at least one special character';
      return;
    }
    if(formValue.password != null && !this.containsDigit(formValue.password)){
      this.errorMessage = 'Password must have at least one digit';
      return;
    }
    if(formValue.password !== formValue.confirmedPassword){
      this.errorMessage = 'Password and confirmed password must match';
      return;
    }
    const dto : RegisterDTO = {
      username : formValue.username ?? '',
      password : formValue.password ?? '',
      confirmedPassword : formValue.confirmedPassword ?? '',
      name : formValue.name ?? '',
      lastName : formValue.lastName ?? '',
      organizationId : formValue.organizationId ,
      publicKey : formValue.publicKey ?? ''

    };
    this.authService.register(dto).subscribe({
      next : () => {
        this.successMessage = 'Successfully registered!';
        this.errorMessage = '';
        this.registerForm.reset();
      },
      error : (err) => {
        console.log('SERVER ERROR:', err);
        console.log('SERVER ERROR BODY:', err.error);

        this.errorMessage =
          typeof err.error === 'string'
            ? err.error
            : err.error?.message || err.message || 'Something went wrong';

          this.successMessage = '';
          this.cdr.detectChanges();
      }
    });
  }
}
