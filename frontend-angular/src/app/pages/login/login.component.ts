import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { LoginResponse } from '../../core/models';
import { PageFeedbackComponent } from '../../shared/page-feedback.component';

@Component({
  selector: 'app-login',
  imports: [FormsModule, PageFeedbackComponent],
  template: `
    <main class="login-shell">
      <section class="login-box">
        <p class="eyebrow">Administração</p>
        <h1>AxeGestor</h1>
        <p class="muted">Acesse o painel de gestão com seu e-mail e senha.</p>

        <label>
          E-mail
          <input type="email" [(ngModel)]="email" autocomplete="email" />
        </label>

        <label>
          Senha
          <input type="password" [(ngModel)]="senha" autocomplete="current-password" (keydown.enter)="login()" />
        </label>

        <button class="login-button" type="button" (click)="login()" [disabled]="carregando">
          {{ carregando ? 'Entrando...' : 'Entrar' }}
        </button>

        <app-page-feedback
          [loading]="carregando"
          loadingText="Validando acesso..."
          [error]="erro"
        />
      </section>
    </main>
  `,
})
export class LoginComponent {
  email = '';
  senha = '';
  erro = '';
  carregando = false;

  constructor(
    private readonly apiService: ApiService,
    private readonly authService: AuthService,
    private readonly router: Router,
  ) {}

  login(): void {
    this.erro = '';

    if (!this.email || !this.senha) {
      this.erro = 'Informe e-mail e senha.';
      return;
    }

    this.carregando = true;
    this.apiService.post<LoginResponse>('/auth/login', {
      email: this.email,
      senha: this.senha,
    }).subscribe({
      next: (response) => {
        this.authService.saveToken(response.token);
        this.router.navigate(['/dashboard']);
      },
      error: () => {
        this.erro = 'E-mail ou senha inválidos.';
        this.carregando = false;
      },
    });
  }
}
