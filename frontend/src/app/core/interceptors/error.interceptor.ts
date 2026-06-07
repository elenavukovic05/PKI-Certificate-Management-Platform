import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return next(req).pipe(
    catchError(err => {
      if (err.status === 401 && !req.url.includes('/auth/refresh') 
                             && !req.url.includes('/auth/logout')) {
        const refreshToken = authService.getRefreshToken();

        if(refreshToken){
          return authService.refreshTokens().pipe(
            switchMap(() => {
              const newToken = authService.getToken();
              const cloned = req.clone({
                headers: req.headers.set('Authorization', `Bearer ${newToken}`)
              });
              return next(cloned);
            }),
            catchError(() => {
              authService.logout();
              return throwError(() => err);
            })
          )
        }

        authService.logout();
      }
      return throwError(() => err);
    })
  );
};