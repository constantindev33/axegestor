import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';

import { ApiService } from '../../core/api.service';
import { Auditoria } from '../../core/models';
import { friendlyHttpError } from '../../shared/http-error';
import { PageFeedbackComponent } from '../../shared/page-feedback.component';

@Component({
  selector: 'app-auditoria',
  imports: [FormsModule, PageFeedbackComponent],
  template: `
    <header class="page-header">
      <div>
        <p class="eyebrow">Segurança</p>
        <h1>Auditoria</h1>
      </div>
      <button class="secondary-button" type="button" (click)="carregar()">Atualizar</button>
    </header>

    <app-page-feedback
      [loading]="carregando"
      loadingText="Carregando auditoria..."
      [error]="mensagemErro"
    />

    <section class="card search-card">
      <h2>Filtrar registros</h2>
      <div class="filter-grid">
        <label>Buscar<input [(ngModel)]="filtroBusca" (ngModelChange)="paginaAtual = 1" placeholder="Usuário, ação ou descrição" /></label>
        <label>Módulo
          <select [(ngModel)]="moduloSelecionado" (ngModelChange)="carregar()">
            <option value="">Todos os módulos</option>
            @for (modulo of modulos; track modulo.valor) {
              <option [value]="modulo.valor">{{ modulo.label }}</option>
            }
          </select>
        </label>
      </div>
    </section>

    <section class="card">
      <div class="section-title">
        <h2>Últimas ações</h2>
        <span class="muted-inline">{{ auditoriasFiltradas.length }} registro(s)</span>
      </div>

      <div class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th>Data</th>
              <th>Usuário</th>
              <th>Módulo</th>
              <th>Ação</th>
              <th>Descrição</th>
            </tr>
          </thead>
          <tbody>
            @for (auditoria of auditoriasPaginadas; track auditoria.id) {
              <tr>
                <td>{{ formatarDataHora(auditoria.criadoEm) }}</td>
                <td>{{ auditoria.usuarioNome || auditoria.usuarioEmail || '-' }}</td>
                <td><span class="status-badge">{{ formatarModulo(auditoria.modulo) }}</span></td>
                <td>{{ formatarAcao(auditoria.acao) }}</td>
                <td>{{ auditoria.descricao }}</td>
              </tr>
            } @empty {
              <tr>
                <td colspan="5">Nenhum registro de auditoria encontrado.</td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    </section>

    <nav class="pagination-bar" aria-label="Paginação de auditoria">
      <button type="button" class="secondary-button" (click)="paginaAnterior()" [disabled]="paginaAtual === 1">Anterior</button>
      <span>Página {{ paginaAtual }} de {{ totalPaginas }}</span>
      <button type="button" class="secondary-button" (click)="proximaPagina()" [disabled]="paginaAtual === totalPaginas">Próxima</button>
    </nav>
  `,
})
export class AuditoriaComponent implements OnInit {
  auditorias: Auditoria[] = [];
  filtroBusca = '';
  moduloSelecionado = '';
  paginaAtual = 1;
  itensPorPagina = 10;
  carregando = false;
  mensagemErro = '';
  modulos = [
    { valor: 'USUARIOS', label: 'Usuários' },
    { valor: 'MEMBROS', label: 'Membros' },
    { valor: 'FINANCEIRO', label: 'Financeiro' },
    { valor: 'ESTOQUE', label: 'Estoque' },
    { valor: 'ASSISTENCIAS', label: 'Assistências' },
  ];

  constructor(private readonly apiService: ApiService) {}

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.mensagemErro = '';
    const query = this.moduloSelecionado ? `?modulo=${this.moduloSelecionado}` : '';
    this.carregando = true;
    this.apiService.get<Auditoria[]>(`/auditorias${query}`)
      .pipe(finalize(() => (this.carregando = false)))
      .subscribe({
        next: (dados) => {
          this.auditorias = dados;
          this.paginaAtual = 1;
        },
        error: (error) => (this.mensagemErro = friendlyHttpError(error, 'Não consegui carregar a auditoria.')),
      });
  }

  get auditoriasFiltradas(): Auditoria[] {
    const busca = this.normalizar(this.filtroBusca);

    if (!busca) {
      return this.auditorias;
    }

    return this.auditorias.filter((auditoria) => this.normalizar([
      auditoria.usuarioNome || '',
      auditoria.usuarioEmail || '',
      auditoria.modulo,
      auditoria.acao,
      auditoria.descricao,
    ].join(' ')).includes(busca));
  }

  get auditoriasPaginadas(): Auditoria[] {
    const inicio = (this.paginaAtual - 1) * this.itensPorPagina;
    return this.auditoriasFiltradas.slice(inicio, inicio + this.itensPorPagina);
  }

  get totalPaginas(): number {
    return Math.max(Math.ceil(this.auditoriasFiltradas.length / this.itensPorPagina), 1);
  }

  paginaAnterior(): void {
    this.paginaAtual = Math.max(this.paginaAtual - 1, 1);
  }

  proximaPagina(): void {
    this.paginaAtual = Math.min(this.paginaAtual + 1, this.totalPaginas);
  }

  formatarDataHora(valor: string): string {
    if (!valor) {
      return '-';
    }

    return new Date(valor).toLocaleString('pt-BR');
  }

  formatarModulo(modulo: string): string {
    return this.modulos.find((item) => item.valor === modulo)?.label || modulo;
  }

  formatarAcao(acao: string): string {
    return {
      CADASTRAR: 'Cadastro',
      ATUALIZAR: 'Atualização',
      DELETAR: 'Exclusão',
      INATIVAR: 'Inativação',
      PAGAR: 'Pagamento',
      MOVIMENTAR: 'Movimentação',
      FINALIZAR: 'Finalização',
      ATUALIZAR_SESSAO: 'Atualização de sessão',
      REALIZAR_SESSAO: 'Sessão realizada',
    }[acao] || acao;
  }

  private normalizar(valor: string): string {
    return valor.toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '');
  }
}
