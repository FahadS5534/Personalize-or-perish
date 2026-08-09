import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'auth/login',
    pathMatch: 'full'
  },
  {
    path: 'auth/login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'auth/register',
    loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent)
  },
  {
    path: 'student',
    loadComponent: () => import('./features/student/student.component').then(m => m.StudentComponent),
    canActivate: [authGuard],
    data: { roles: ['STUDENT'] }
  },
  {
    path: 'facilitator',
    loadComponent: () => import('./features/facilitator/facilitator.component').then(m => m.FacilitatorComponent),
    canActivate: [authGuard],
    data: { roles: ['FACILITATOR'] }
  },
  {
    path: '**',
    redirectTo: ''
  }
];
