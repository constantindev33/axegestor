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
          <a routerLink="/assistencias" routerLinkActive="active">Assistências</a>
          <a routerLink="/financeiro" routerLinkActive="active">Financeiro</a>
          <a routerLink="/estoque" routerLinkActive="active">Estoque</a>
          <a routerLink="/membros" routerLinkActive="active">Membros</a>
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

  logout(): void {
    this.authService.logout();
  }
}
