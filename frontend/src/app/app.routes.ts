import { Routes } from '@angular/router';
import { CertificatePreviewComponent } from './features/certificate-preview/certificate-preview.component';
import { LoginComponent } from './features/login/login.component';
import { authGuard } from './core/guards/auth.guard';
import { CaCertificateFormComponent } from './features/ca-certificate-form/ca-certificate-form.component';
import { AllCertificatesComponent } from './features/all-certificates/all-certificates.component';
import { CreateCertificatesComponent } from './features/create-certificates/create-certificates.component';
import { AddCaUsersComponent } from './features/add-ca-users/add-ca-users.component';

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
        path: 'login',
        component: LoginComponent
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
        path: '**',
        redirectTo: 'login'
    }
];
