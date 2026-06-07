import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, NavigationEnd } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { TwoFactorAuthService } from '../services/two-factor.service';
import { TwoFactorModalComponent } from '../../features/two-factor/two-factor.component';
import { Subscription, filter } from 'rxjs';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule, TwoFactorModalComponent],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit, OnDestroy {
  role: string | null = null;
  twoFactorEnabled = false;
  show2FAModal = false;
  modalMode: 'enable' | 'disable' = 'enable';
  private authStateSub?: Subscription;
  private routerSub?: Subscription;

  constructor(
    private authService: AuthService,
    private router: Router,
    private twoFactorService: TwoFactorAuthService
  ) {}

  ngOnInit(): void {
    this.role = this.authService.getRole();
    this.fetch2FAStatus();
    
    this.authStateSub = this.authService.authStateChanged$.subscribe(() => {
      console.log('Auth state changed, refreshing...');
      this.refreshAll();
    });

    this.routerSub = this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe(() => {
      console.log('Navigation ended, refreshing...');
      this.refreshAll();
    });
  }

  ngOnDestroy(): void {
    this.authStateSub?.unsubscribe();
    this.routerSub?.unsubscribe();
  }

  private refreshAll() {
    this.role = this.authService.getRole();
    if (this.authService.getToken()){
      this.fetch2FAStatus();
    }
  }

  fetch2FAStatus() {
     if (!this.authService.getToken()) {
        return;
    }

    if (this.authService.is2FARequired()) {
      console.log('2FA flow active, setting twoFactorEnabled to true');
      this.twoFactorEnabled = true;
      return;
    }

    console.log('Fetching 2FA status from server...');
    this.twoFactorService.getStatus().subscribe({
      next: (res) => {
        console.log('2FA status:', res.enabled);
        this.twoFactorEnabled = res.enabled;
      },
      error: (err) => {
        console.error('Failed to fetch 2FA status:', err);
        this.twoFactorEnabled = false;
      }
    });
  }

  toggle2FA() {
    if (this.twoFactorEnabled) {
      this.modalMode = 'disable';
    } else {
      this.modalMode = 'enable';
    }
    this.show2FAModal = true;
  }

  on2FAModalClose() {
    this.show2FAModal = false;
  }

  on2FASuccess(enabled: boolean) {
    console.log('2FA success, new state:', enabled);
    this.twoFactorEnabled = enabled;
    this.show2FAModal = false;
  }

  logout(): void {
    this.authService.logout();
  }
}