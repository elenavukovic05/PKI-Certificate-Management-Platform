import { ChangeDetectorRef, Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent {
  username = '';
  password = '';
  errorMessage = '';

  constructor(private authService: AuthService, private router: Router, private cdr: ChangeDetectorRef) {}

  login() {
    this.errorMessage = '';
    this.authService.login(this.username, this.password).subscribe({
      next: () => {
        const token = this.authService.getToken();
        console.log('TOKEN:', token);

        const role = this.authService.getRole();
        console.log('ROLE:', role);

        if (role === 'CA_USER') {
          this.router.navigate(['/issue-certificate']);
        } else {
          this.router.navigate(['/my-certificates']);
        }
      },
      error: () => {
        this.errorMessage = 'Invalid username or password.';
        this.cdr.detectChanges();
      }
    });
  }
}
