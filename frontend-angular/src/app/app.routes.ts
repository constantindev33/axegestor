import { Routes } from '@angular/router';

import { authGuard } from './core/auth.guard';
import { LayoutComponent } from './layout/layout.component';
import { AssistenciasComponent } from './pages/assistencias/assistencias.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { EstoqueComponent } from './pages/estoque/estoque.component';
import { FinanceiroComponent } from './pages/financeiro/financeiro.component';
import { LoginComponent } from './pages/login/login.component';
import { MembrosComponent } from './pages/membros/membros.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  {
    path: '',
    component: LayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      { path: 'dashboard', component: DashboardComponent },
      { path: 'assistencias', component: AssistenciasComponent },
      { path: 'financeiro', component: FinanceiroComponent },
      { path: 'estoque', component: EstoqueComponent },
      { path: 'membros', component: MembrosComponent },
    ],
  },
  { path: '**', redirectTo: 'dashboard' },
];
