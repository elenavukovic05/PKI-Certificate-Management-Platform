import { Routes } from '@angular/router';
import { CertificatePreviewComponent } from './features/certificate-preview/certificate-preview.component';

export const routes: Routes = [
    {
        path: '',
        redirectTo: 'my-certificates',
        pathMatch: 'full'
    },
    {
        path: 'my-certificates',
        component: CertificatePreviewComponent
    },
    {
        path: '**',
        redirectTo: 'my-certificates'
    }
];
