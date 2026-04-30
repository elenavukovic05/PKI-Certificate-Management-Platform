import { Routes } from '@angular/router';
import { CertificatePreviewComponent } from './features/certificate-preview/certificate-preview.component';
import { LoginComponent } from './features/login/login.component';
import { authGuard } from './core/guards/auth.guard';
import { CaCertificateFormComponent } from './features/ca-certificate-form/ca-certificate-form.component';

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
        path: '**',
        redirectTo: 'login'
    },
    {
        path: 'login',
        component: LoginComponent
    },
    {
    path: 'issue-certificate',
    component: CaCertificateFormComponent,
    canActivate: [authGuard]
    }
];
