import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { AuthService } from '../core/auth.service';

@Component({
  selector: 'app-layout',
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  template: `
    <div class="app-shell">
      <aside class="sidebar">
        <h2>AxeGestor</h2>
        <nav>
          <a routerLink="/dashboard" routerLinkActive="active">Dashboard</a>
          @if (podeVer(['ADMIN', 'ASSISTENCIA'])) {
            <a routerLink="/assistencias" routerLinkActive="active">Assistências</a>
          }
          @if (podeVer(['ADMIN', 'FINANCEIRO'])) {
            <a routerLink="/financeiro" routerLinkActive="active">Financeiro</a>
            <a routerLink="/relatorios" routerLinkActive="active">Relatórios</a>
          }
          @if (podeVer(['ADMIN', 'ESTOQUE'])) {
            <a routerLink="/estoque" routerLinkActive="active">Estoque</a>
          }
          @if (podeVer(['ADMIN', 'FINANCEIRO', 'ASSISTENCIA', 'ESTOQUE'])) {
            <a routerLink="/membros" routerLinkActive="active">Membros</a>
          }
          @if (podeVer(['ADMIN'])) {
            <a routerLink="/usuarios" routerLinkActive="active">Usuários</a>
            <a routerLink="/auditoria" routerLinkActive="active">Auditoria</a>
          }
          <button type="button" class="nav-button" (click)="logout()">Sair</button>
        </nav>
      </aside>

      <main class="content">
        <router-outlet />
      </main>
    </div>
  `,
})
export class LayoutComponent {
  constructor(private readonly authService: AuthService) {}

  podeVer(perfis: string[]): boolean {
    return this.authService.hasAnyPerfil(perfis);
  }

  logout(): void {
    this.authService.logout();
  }
}
