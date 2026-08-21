import { Routes } from '@angular/router';

import { authGuard, perfilGuard } from './core/auth.guard';
import { LayoutComponent } from './layout/layout.component';
import { AuditoriaComponent } from './pages/auditoria/auditoria.component';
import { AssistenciasComponent } from './pages/assistencias/assistencias.component';
import { AtendimentoComponent } from './pages/atendimento/atendimento.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { EstoqueComponent } from './pages/estoque/estoque.component';
import { FinanceiroComponent } from './pages/financeiro/financeiro.component';
import { LoginComponent } from './pages/login/login.component';
import { MembrosComponent } from './pages/membros/membros.component';
import { RelatoriosComponent } from './pages/relatorios/relatorios.component';
import { UsuariosComponent } from './pages/usuarios/usuarios.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  {
    path: '',
    component: LayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      { path: 'dashboard', component: DashboardComponent },
      { path: 'atendimento', component: AtendimentoComponent, canActivate: [perfilGuard], data: { perfis: ['ADMIN', 'ASSISTENCIA'] } },
      { path: 'assistencias', component: AssistenciasComponent, canActivate: [perfilGuard], data: { perfis: ['ADMIN', 'ASSISTENCIA'] } },
      { path: 'financeiro', component: FinanceiroComponent, canActivate: [perfilGuard], data: { perfis: ['ADMIN', 'FINANCEIRO'] } },
      { path: 'relatorios', component: RelatoriosComponent, canActivate: [perfilGuard], data: { perfis: ['ADMIN', 'FINANCEIRO'] } },
      { path: 'estoque', component: EstoqueComponent, canActivate: [perfilGuard], data: { perfis: ['ADMIN', 'ESTOQUE'] } },
      { path: 'membros', component: MembrosComponent, canActivate: [perfilGuard], data: { perfis: ['ADMIN', 'FINANCEIRO', 'ASSISTENCIA', 'ESTOQUE'] } },
      { path: 'usuarios', component: UsuariosComponent, canActivate: [perfilGuard], data: { perfis: ['ADMIN'] } },
      { path: 'auditoria', component: AuditoriaComponent, canActivate: [perfilGuard], data: { perfis: ['ADMIN'] } },
    ],
  },
  { path: '**', redirectTo: 'dashboard' },
];
