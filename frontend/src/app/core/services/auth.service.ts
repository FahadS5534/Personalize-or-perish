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

  register(name: string, email: string, password: String, role: 'STUDENT' | 'FACILITATOR'): Observable<User> {
    return this.http.post<User>(`${this.apiUrl}/register`, { name, email, password, role });
  }

  login(email: string, password: String): Observable<{ token: string }> {
    return this.http.post<{ token: string }>(`${this.apiUrl}/login`, { email, password }).pipe(
      tap(res => {
        this.saveToken(res.token);
      })
    );
  }

  logout() {
    localStorage.removeItem('access_token');
    this.currentUser.set(null);
    this.router.navigate(['/auth/login']);
  }

  private saveToken(token: string) {
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
    const payloadBase64 = token.split('.')[1];
    const payloadJson = atob(payloadBase64);
    const payload = JSON.parse(payloadJson);

    // Mappings from standard JWT claim structures
    const role = payload.role || (payload.roles && payload.roles[0]) || 'STUDENT';
    const name = payload.name || payload.sub || 'User';

    this.currentUser.set({
      id: payload.userId || 0,
      email: payload.sub || '',
      name: name,
      role: role as 'STUDENT' | 'FACILITATOR' | 'ADMIN',
      token: token
    });
  }

  getToken(): string | null {
    return localStorage.getItem('access_token');
  }
}
