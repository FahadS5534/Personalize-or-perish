import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  private authService = inject(AuthService);
  private router = inject(Router);

  email = '';
  password = '';
  isLoading = false;
  errorMessage = '';

  onSubmit() {
    this.isLoading = true;
    this.errorMessage = '';

    this.authService.login(this.email, this.password).subscribe({
      next: () => {
        this.isLoading = false;
        this.redirectUser();
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error?.message || 'Invalid email or password. Please try again.';
      }
    });
  }

  onGuestSignIn(role: 'STUDENT' | 'FACILITATOR') {
    this.isLoading = true;
    this.errorMessage = '';

    this.authService.guestLogin(role).subscribe({
      next: () => {
        this.isLoading = false;
        this.navigateToRole(role);
      },
      error: (err) => {
        // Fallback to local guest session if backend is cold-starting or offline
        console.warn('Backend guest auth unreachable, activating fallback guest session:', err);
        this.isLoading = false;
        this.authService.setGuestUser(role);
        this.navigateToRole(role);
      }
    });
  }

  private redirectUser() {
    const user = this.authService.currentUser();
    if (user) {
      this.navigateToRole(user.role);
    } else {
      this.router.navigate(['/']);
    }
  }

  private navigateToRole(role: string) {
    if (role === 'FACILITATOR') {
      this.router.navigate(['/facilitator']);
    } else {
      this.router.navigate(['/student']);
    }
  }
}
