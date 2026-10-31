import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
  private authService = inject(AuthService);
  private router = inject(Router);

  name = '';
  email = '';
  password = '';
  role: 'STUDENT' | 'FACILITATOR' = 'STUDENT';
  
  isLoading = false;
  errorMessage = '';
  successMessage = '';

  onSubmit() {
    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.authService.register(this.name, this.email, this.password, this.role).subscribe({
      next: (res: any) => {
        this.isLoading = false;
        this.successMessage = 'Account created successfully! Redirecting...';
        const token = res?.accessToken || res?.token;
        if (token) {
          this.authService.saveToken(token);
          setTimeout(() => {
            if (this.role === 'FACILITATOR') {
              this.router.navigate(['/facilitator']);
            } else {
              this.router.navigate(['/student']);
            }
          }, 800);
        } else {
          setTimeout(() => {
            this.router.navigate(['/auth/login']);
          }, 1200);
        }
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error?.message || 'Registration failed. Institutional email may already be in use.';
      }
    });
  }
}
