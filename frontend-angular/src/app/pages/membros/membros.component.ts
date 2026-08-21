import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';

import { ApiService } from '../../core/api.service';
import { Membro } from '../../core/models';
import { friendlyHttpError } from '../../shared/http-error';
import { PageFeedbackComponent } from '../../shared/page-feedback.component';

@Component({
  selector: 'app-membros',
  imports: [FormsModule, PageFeedbackComponent],
  template: `
    <header class="page-header">
      <div><p class="eyebrow">Casa</p><h1>Membros</h1></div>
      <button class="secondary-button" type="button" (click)="carregar()">Atualizar</button>
    </header>

    <app-page-feedback
      [loading]="carregando"
      loadingText="Carregando membros..."
      [success]="mensagemSucesso"
      [error]="mensagemErro"
    />

    <section class="card form-card">
      <h2>Novo membro</h2>
      <div class="form-grid">
        <label>Nome<input [(ngModel)]="form.nome" /></label>
        <label>Telefone<input [(ngModel)]="form.telefone" /></label>
        <label>E-mail<input type="email" [(ngModel)]="form.email" /></label>
        <label>Data de entrada<input type="date" [(ngModel)]="form.dataEntrada" /></label>
        <label>Função
          <select [(ngModel)]="form.funcao">
            <option value="MEDIUM">Médium</option><option value="CAMBONE">Cambone</option><option value="DIRIGENTE">Dirigente</option>
            <option value="FINANCEIRO">Financeiro</option><option value="ALMOXARIFADO">Almoxarifado</option><option value="ESTUDANTE">Estudante</option><option value="VISITANTE">Visitante</option>
          </select>
        </label>
        <label>Status
          <select [(ngModel)]="form.status">
            <option value="ATIVO">Ativo</option><option value="AFASTADO">Afastado</option><option value="VISITANTE">Visitante</option><option value="DESLIGADO">Desligado</option>
          </select>
        </label>
      </div>
      <div class="actions-row"><button type="button" (click)="salvar()" [disabled]="salvando">{{ salvando ? 'Salvando...' : 'Salvar membro' }}</button></div>
    </section>

    <section class="card search-card">
      <h2>Filtrar membros</h2>
      <div class="filter-grid">
        <label>Buscar<input [(ngModel)]="filtroBusca" (ngModelChange)="paginaAtual = 1" placeholder="Nome, telefone ou e-mail" /></label>
        <label>Função
          <select [(ngModel)]="filtroFuncao" (ngModelChange)="paginaAtual = 1">
            <option value="">Todas</option>
            <option value="MEDIUM">Médium</option>
            <option value="CAMBONE">Cambone</option>
            <option value="DIRIGENTE">Dirigente</option>
            <option value="FINANCEIRO">Financeiro</option>
            <option value="ALMOXARIFADO">Almoxarifado</option>
            <option value="ESTUDANTE">Estudante</option>
            <option value="VISITANTE">Visitante</option>
          </select>
        </label>
        <label>Status
          <select [(ngModel)]="filtroStatus" (ngModelChange)="paginaAtual = 1">
            <option value="">Todos</option>
            <option value="ATIVO">Ativo</option>
            <option value="AFASTADO">Afastado</option>
            <option value="VISITANTE">Visitante</option>
            <option value="DESLIGADO">Desligado</option>
          </select>
        </label>
      </div>
    </section>

    <section class="cards list-cards">
      <div class="list-toolbar">
        <h2>Lista de membros</h2>
        <span class="muted-inline">{{ membrosFiltrados.length }} resultado(s)</span>
      </div>
      @for (membro of membrosPaginados; track membro.id) {
        <article class="card">
          <span class="status-badge">{{ membro.status }}</span>
          <h3>{{ membro.nome }}</h3>
          <div class="info-list">
            <div><dt>Função</dt><dd>{{ membro.funcao }}</dd></div>
            <div><dt>Telefone</dt><dd>{{ membro.telefone || '-' }}</dd></div>
            <div><dt>E-mail</dt><dd>{{ membro.email || '-' }}</dd></div>
          </div>
        </article>
      } @empty {
        <div class="empty-state">Nenhum membro encontrado.</div>
      }
    </section>

    <nav class="pagination-bar" aria-label="Paginação de membros">
      <button type="button" class="secondary-button" (click)="paginaAnterior()" [disabled]="paginaAtual === 1">Anterior</button>
      <span>Página {{ paginaAtual }} de {{ totalPaginas }}</span>
      <button type="button" class="secondary-button" (click)="proximaPagina()" [disabled]="paginaAtual === totalPaginas">Próxima</button>
    </nav>
  `,
})
export class MembrosComponent implements OnInit {
  membros: Membro[] = [];
  form: Membro = this.novoMembro();
  filtroBusca = '';
  filtroFuncao = '';
  filtroStatus = '';
  paginaAtual = 1;
  itensPorPagina = 6;
  carregando = false;
  salvando = false;
  mensagemErro = '';
  mensagemSucesso = '';

  constructor(private readonly apiService: ApiService) {}

  ngOnInit(): void {
    this.carregar();
  }

  carregar(preservarMensagem = false): void {
    if (!preservarMensagem) {
      this.limparMensagens();
    }
    this.carregando = true;

    this.apiService.get<Membro[]>('/membros')
      .pipe(finalize(() => (this.carregando = false)))
      .subscribe({
        next: (dados) => {
          this.membros = dados;
          this.paginaAtual = 1;
        },
        error: (error) => (this.mensagemErro = friendlyHttpError(error, 'Não consegui carregar os membros.')),
      });
  }

  salvar(): void {
    this.limparMensagens();
    this.salvando = true;

    this.apiService.post<Membro>('/membros', this.form)
      .pipe(finalize(() => (this.salvando = false)))
      .subscribe({
        next: () => {
          this.form = this.novoMembro();
          this.mensagemSucesso = 'Membro salvo com sucesso.';
          this.carregar(true);
        },
        error: (error) => (this.mensagemErro = friendlyHttpError(error, 'Não consegui salvar o membro.')),
      });
  }

  get membrosFiltrados(): Membro[] {
    const busca = this.normalizar(this.filtroBusca);

    return this.membros.filter((membro) => {
      const texto = this.normalizar(`${membro.nome} ${membro.telefone || ''} ${membro.email || ''}`);
      const bateBusca = !busca || texto.includes(busca);
      const bateFuncao = !this.filtroFuncao || membro.funcao === this.filtroFuncao;
      const bateStatus = !this.filtroStatus || membro.status === this.filtroStatus;

      return bateBusca && bateFuncao && bateStatus;
    });
  }

  get membrosPaginados(): Membro[] {
    const inicio = (this.paginaAtual - 1) * this.itensPorPagina;
    return this.membrosFiltrados.slice(inicio, inicio + this.itensPorPagina);
  }

  get totalPaginas(): number {
    return Math.max(Math.ceil(this.membrosFiltrados.length / this.itensPorPagina), 1);
  }

  paginaAnterior(): void {
    this.paginaAtual = Math.max(this.paginaAtual - 1, 1);
  }

  proximaPagina(): void {
    this.paginaAtual = Math.min(this.paginaAtual + 1, this.totalPaginas);
  }

  private normalizar(valor: string): string {
    return valor.toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '');
  }

  private limparMensagens(): void {
    this.mensagemErro = '';
    this.mensagemSucesso = '';
  }

  private novoMembro(): Membro {
    return {
      nome: '',
      telefone: '',
      email: '',
      dataEntrada: null,
      funcao: 'MEDIUM',
      status: 'ATIVO',
    };
  }
}
