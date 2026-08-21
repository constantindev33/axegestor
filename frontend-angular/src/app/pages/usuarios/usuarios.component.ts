import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';

import { ApiService } from '../../core/api.service';
import { Usuario } from '../../core/models';
import { friendlyHttpError } from '../../shared/http-error';
import { PageFeedbackComponent } from '../../shared/page-feedback.component';

@Component({
  selector: 'app-usuarios',
  imports: [FormsModule, PageFeedbackComponent],
  template: `
    <header class="page-header">
      <div>
        <p class="eyebrow">Acesso</p>
        <h1>Usuários</h1>
      </div>
      <button class="secondary-button" type="button" (click)="carregar()">Atualizar</button>
    </header>

    <app-page-feedback
      [loading]="carregando"
      loadingText="Carregando usuários..."
      [success]="mensagemSucesso"
      [error]="mensagemErro"
    />

    <section class="card form-card">
      <h2>{{ usuarioEditandoId ? 'Editar usuário' : 'Novo usuário' }}</h2>
      <div class="form-grid">
        <label>Nome<input [(ngModel)]="form.nome" /></label>
        <label>E-mail<input type="email" [(ngModel)]="form.email" /></label>
        <label>Senha<input type="password" [(ngModel)]="form.senha" [placeholder]="usuarioEditandoId ? 'Deixe em branco para manter' : ''" /></label>
        <label>Perfil
          <select [(ngModel)]="form.perfil">
            @for (perfil of perfis; track perfil.valor) {
              <option [value]="perfil.valor">{{ perfil.label }}</option>
            }
          </select>
        </label>
        <label>Status
          <select [(ngModel)]="form.ativo">
            <option [ngValue]="true">Ativo</option>
            <option [ngValue]="false">Inativo</option>
          </select>
        </label>
      </div>

      <div class="actions-row">
        <button type="button" (click)="salvar()" [disabled]="salvando">{{ salvando ? 'Salvando...' : 'Salvar usuário' }}</button>
        <button type="button" class="secondary-button" (click)="limparFormulario()">Limpar</button>
      </div>
    </section>

    <section class="cards list-cards">
      @for (usuario of usuarios; track usuario.id) {
        <article class="card">
          <span class="status-badge" [class.status-ok]="usuario.ativo" [class.status-danger]="!usuario.ativo">
            {{ usuario.ativo ? 'Ativo' : 'Inativo' }}
          </span>
          <h3>{{ usuario.nome }}</h3>
          <div class="info-list">
            <div><dt>E-mail</dt><dd>{{ usuario.email }}</dd></div>
            <div><dt>Perfil</dt><dd>{{ formatarPerfil(usuario.perfil) }}</dd></div>
          </div>
          <div class="actions-row">
            <button class="secondary-button" type="button" (click)="preencherFormulario(usuario)">Editar</button>
            @if (usuario.id && usuario.ativo) {
              <button class="secondary-button" type="button" (click)="inativar(usuario.id)">Inativar</button>
            }
          </div>
        </article>
      } @empty {
        <div class="empty-state">Nenhum usuário encontrado.</div>
      }
    </section>
  `,
})
export class UsuariosComponent implements OnInit {
  usuarios: Usuario[] = [];
  usuarioEditandoId: number | null = null;
  form: Usuario = this.novoUsuario();
  carregando = false;
  salvando = false;
  mensagemErro = '';
  mensagemSucesso = '';
  perfis = [
    { valor: 'ADMIN', label: 'Administrador' },
    { valor: 'FINANCEIRO', label: 'Financeiro' },
    { valor: 'ESTOQUE', label: 'Estoque' },
    { valor: 'ASSISTENCIA', label: 'Assistência' },
    { valor: 'MEMBRO', label: 'Membro' },
  ];

  constructor(private readonly apiService: ApiService) {}

  ngOnInit(): void {
    this.carregar();
  }

  carregar(preservarMensagem = false): void {
    if (!preservarMensagem) {
      this.limparMensagens();
    }

    this.carregando = true;
    this.apiService.get<Usuario[]>('/usuarios')
      .pipe(finalize(() => (this.carregando = false)))
      .subscribe({
        next: (dados) => (this.usuarios = dados),
        error: (error) => (this.mensagemErro = friendlyHttpError(error, 'Não consegui carregar os usuários.')),
      });
  }

  salvar(): void {
    this.limparMensagens();

    if (!this.form.nome || !this.form.email || (!this.usuarioEditandoId && !this.form.senha)) {
      this.mensagemErro = 'Preencha nome, e-mail e senha.';
      return;
    }

    const payload = { ...this.form };

    if (this.usuarioEditandoId && !payload.senha) {
      delete payload.senha;
    }

    const requisicao = this.usuarioEditandoId
      ? this.apiService.put<Usuario>(`/usuarios/${this.usuarioEditandoId}`, payload)
      : this.apiService.post<Usuario>('/usuarios', payload);

    this.salvando = true;
    requisicao.pipe(finalize(() => (this.salvando = false))).subscribe({
      next: () => {
        this.limparFormulario();
        this.mensagemSucesso = 'Usuário salvo com sucesso.';
        this.carregar(true);
      },
      error: (error) => (this.mensagemErro = friendlyHttpError(error, 'Não consegui salvar o usuário.')),
    });
  }

  preencherFormulario(usuario: Usuario): void {
    this.usuarioEditandoId = usuario.id || null;
    this.form = { ...usuario, senha: '' };
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  inativar(id: number): void {
    if (!confirm('Deseja inativar este usuário? Ele não conseguirá mais entrar no sistema.')) {
      return;
    }

    this.limparMensagens();
    this.salvando = true;
    this.apiService.delete<void>(`/usuarios/${id}`)
      .pipe(finalize(() => (this.salvando = false)))
      .subscribe({
        next: () => {
          this.mensagemSucesso = 'Usuário inativado com sucesso.';
          this.carregar(true);
        },
        error: (error) => (this.mensagemErro = friendlyHttpError(error, 'Não consegui inativar o usuário.')),
      });
  }

  limparFormulario(): void {
    this.usuarioEditandoId = null;
    this.form = this.novoUsuario();
  }

  private limparMensagens(): void {
    this.mensagemErro = '';
    this.mensagemSucesso = '';
  }

  formatarPerfil(perfil: string): string {
    return {
      ADMIN: 'Administrador',
      FINANCEIRO: 'Financeiro',
      ESTOQUE: 'Estoque',
      ASSISTENCIA: 'Assistência',
      MEMBRO: 'Membro',
    }[perfil] || perfil;
  }

  private novoUsuario(): Usuario {
    return {
      nome: '',
      email: '',
      senha: '',
      perfil: 'ASSISTENCIA',
      ativo: true,
    };
  }
}
