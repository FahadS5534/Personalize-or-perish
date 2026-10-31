import { Injectable, signal, computed, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { User } from '../models/models';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  private apiUrl = `${environment.apiUrl}/api/auth`;

  // Signals
  currentUser = signal<User | null>(null);
  isAuthenticated = computed(() => this.currentUser() !== null);
  isFacilitator = computed(() => this.currentUser()?.role === 'FACILITATOR');

  constructor() {
    this.loadToken();
  }

  register(name: string, email: string, password: String, role: 'STUDENT' | 'FACILITATOR'): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/register`, { name, email, password, role }).pipe(
      tap(res => {
        const token = res?.accessToken || res?.token;
        if (token) {
          this.saveToken(token);
        }
      })
    );
  }

  login(email: string, password: String): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/login`, { email, password }).pipe(
      tap(res => {
        const token = res?.accessToken || res?.token;
        if (token) {
          this.saveToken(token);
        }
      })
    );
  }

  guestLogin(role: 'STUDENT' | 'FACILITATOR' = 'STUDENT'): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/guest?role=${role}`, {}).pipe(
      tap(res => {
        const token = res?.accessToken || res?.token;
        if (token) {
          this.saveToken(token);
        }
      })
    );
  }

  // Fallback demo/guest sign-in (e.g. if backend is cold-starting)
  setGuestUser(role: 'STUDENT' | 'FACILITATOR') {
    const isStudent = role === 'STUDENT';
    const fakeToken = this.createMockJwt({
      userId: isStudent ? 1 : 2,
      sub: isStudent ? 'guest.student@personalize.io' : 'guest.facilitator@personalize.io',
      name: isStudent ? 'Guest Student' : 'Guest Facilitator',
      role: role
    });
    this.saveToken(fakeToken);
  }

  logout() {
    localStorage.removeItem('access_token');
    this.currentUser.set(null);
    this.router.navigate(['/auth/login']);
  }

  saveToken(token: string) {
    if (!token) return;
    localStorage.setItem('access_token', token);
    this.decodeAndSetUser(token);
  }

  private loadToken() {
    const token = localStorage.getItem('access_token');
    if (token) {
      try {
        this.decodeAndSetUser(token);
      } catch (e) {
        this.logout();
      }
    }
  }

  private decodeAndSetUser(token: string) {
    if (!token || typeof token !== 'string') return;
    try {
      const parts = token.split('.');
      if (parts.length < 2) return;
      const base64Url = parts[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const payloadJson = decodeURIComponent(atob(base64).split('').map(c => {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
      }).join(''));
      const payload = JSON.parse(payloadJson);

      let role = payload.role;
      if (!role && payload.roles && payload.roles.length > 0) {
        role = payload.roles[0].replace('ROLE_', '');
      }
      role = (role || 'STUDENT').toUpperCase();

      const name = payload.name || payload.sub || 'User';
      const userId = Number(payload.userId || payload.id || 1);

      this.currentUser.set({
        id: userId,
        email: payload.sub || payload.email || '',
        name: name,
        role: role as 'STUDENT' | 'FACILITATOR' | 'ADMIN',
        token: token
      });
    } catch (e) {
      console.error('Failed to decode JWT token:', e);
      this.logout();
    }
  }

  private createMockJwt(claims: { userId: number; sub: string; name: string; role: string }): string {
    const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
    const payload = btoa(JSON.stringify({
      userId: claims.userId,
      sub: claims.sub,
      name: claims.name,
      role: claims.role,
      exp: Math.floor(Date.now() / 1000) + 86400
    }));
    return `${header}.${payload}.mockSignature`;
  }

  getToken(): string | null {
    return localStorage.getItem('access_token');
  }
}
