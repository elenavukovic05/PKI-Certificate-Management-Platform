import { Routes } from '@angular/router';
import { CertificatePreviewComponent } from './features/certificate-preview/certificate-preview.component';
import { LoginComponent } from './features/login/login.component';
import { authGuard } from './core/guards/auth.guard';
import { CaCertificateFormComponent } from './features/ca-certificate-form/ca-certificate-form.component';
import { AllCertificatesComponent } from './features/all-certificates/all-certificates.component';
import { CreateCertificatesComponent } from './features/create-certificates/create-certificates.component';
import { AddCaUsersComponent } from './features/add-ca-users/add-ca-users.component';
import { AddOrganizationsComponent } from './features/add-organizations/add-organizations.component';
import { ActivateCaUser } from './features/activate-ca-user/activate-ca-user';
import { CsrFormComponent } from './features/csr-form/csr-form.component';
import { RegisterComponent } from './features/register/register.component';
import { ActivateUserComponent } from './features/activate-user/activate-user.component';
import { ChangePasswordComponent } from './features/change-password/change-password.component';
import { VerificationForPasswordRecoveryComponent } from './features/verification-for-password-recovery/verification-for-password-recovery.component';

export const routes: Routes = [
    {
        path: '',
        redirectTo: 'login',
        pathMatch: 'full'
    },
    {
        path: 'my-certificates',
        component: CertificatePreviewComponent,
        canActivate: [authGuard]
    },
    {
        path : 'create-certificates',
        component : CreateCertificatesComponent,
        canActivate : [authGuard]
    },
    {
        path : 'add-ca-user',
        component : AddCaUsersComponent,
        canActivate : [authGuard]
    },
    {
        path : 'add-organization',
        component : AddOrganizationsComponent,
        canActivate : [authGuard]
    },
    {
        path : 'activate-ca-user',
        component : ActivateCaUser
    },
    {
        path: 'activate-user',
        component : ActivateUserComponent
    },
    {
        path: 'login',
        component: LoginComponent
    },
    {
        path: 'change-password',
        component : ChangePasswordComponent
    },
    {
        path : 'verification-for-password-recovery',
        component : VerificationForPasswordRecoveryComponent
    },
    {
        path: 'register',
        component : RegisterComponent
    },
    {
        path: 'issue-certificate',
        component: CaCertificateFormComponent,
        canActivate: [authGuard]
    },
    {
        path : 'all-certificates',
        component : AllCertificatesComponent,
        canActivate : [authGuard]
    },
    {
        path: 'csr',
        component: CsrFormComponent,
    },
    {
        path: '**',
        redirectTo: 'login'
    }
];
