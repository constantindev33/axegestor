import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';

import { ApiService } from '../../core/api.service';
import { Assistencia } from '../../core/models';
import { formatDate } from '../../shared/formatters';
import { friendlyHttpError } from '../../shared/http-error';
import { PageFeedbackComponent } from '../../shared/page-feedback.component';

@Component({
  selector: 'app-atendimento',
  imports: [FormsModule, PageFeedbackComponent],
  template: `
    <header class="page-header">
      <div>
        <p class="eyebrow">Piloto de quarta-feira</p>
        <h1>Atendimento</h1>
      </div>
      <button class="secondary-button" type="button" (click)="carregar()">Atualizar</button>
    </header>

    <app-page-feedback
      [loading]="carregando"
      loadingText="Carregando atendimentos..."
      [success]="mensagemSucesso"
      [error]="mensagemErro"
    />

    <section class="cards dashboard-metrics">
      <article class="card metric-card">
        <span>Atendimentos hoje</span>
        <strong>{{ atendimentosDoDia.length }}</strong>
        <small>{{ dataAtendimentoFormatada }}</small>
      </article>
      <article class="card metric-card">
        <span>Em andamento</span>
        <strong>{{ totalEmAndamento }}</strong>
        <small>Tratamentos ainda abertos</small>
      </article>
      <article class="card metric-card">
        <span>Finalizados</span>
        <strong>{{ totalFinalizados }}</strong>
        <small>Tratamentos concluídos no dia</small>
      </article>
      <article class="card metric-card">
        <span>Cancelados</span>
        <strong>{{ totalCancelados }}</strong>
        <small>Registros cancelados</small>
      </article>
    </section>

    <section class="card form-card">
      <div class="section-title">
        <h2>Cadastro rápido</h2>
        <span class="muted-inline">Recepção</span>
      </div>

      <div class="form-grid">
        <label>Nome completo<input [(ngModel)]="form.nome" placeholder="Nome de quem será atendido" /></label>
        <label>WhatsApp<input [(ngModel)]="form.whatsapp" placeholder="(00) 00000-0000" /></label>
        <label>Cidade<input [(ngModel)]="form.cidade" placeholder="Cidade" /></label>
        <label>Entidade da consulta<input [(ngModel)]="form.entidadeConsulta" placeholder="Opcional" /></label>
      </div>

      <label>Observações rápidas<textarea [(ngModel)]="form.observacoes" placeholder="Queixa principal, orientação inicial ou observação da recepção"></textarea></label>

      <h3>Tratamentos indicados</h3>
      <div class="tratamentos">
        @for (tratamento of tratamentosDisponiveis; track tratamento.valor) {
          <label>
            <input type="checkbox" [checked]="tratamentoSelecionado(tratamento.valor)" (change)="alternarTratamento(tratamento.valor, $event)" />
            {{ tratamento.label }}
          </label>
        }
      </div>

      <div class="actions-row">
        <button type="button" (click)="salvarAtendimento()" [disabled]="salvando">{{ salvando ? 'Salvando...' : 'Salvar atendimento de hoje' }}</button>
        <button type="button" class="secondary-button" (click)="limparFormulario()">Limpar</button>
      </div>
    </section>

    <section class="card search-card">
      <div class="section-title">
        <h2>Fila e busca do dia</h2>
        <button type="button" class="secondary-button" (click)="exportarAtendimentosDoDia()">Exportar relatório do dia</button>
      </div>

      <div class="filter-grid">
        <label>Data do atendimento<input type="date" [(ngModel)]="dataAtendimento" (ngModelChange)="paginaAtual = 1" /></label>
        <label>Buscar<input [(ngModel)]="busca" (ngModelChange)="paginaAtual = 1" placeholder="Nome, WhatsApp, cidade ou entidade" /></label>
        <label>Status
          <select [(ngModel)]="statusSelecionado" (ngModelChange)="paginaAtual = 1">
            <option value="">Todos</option>
            <option value="EM_ANDAMENTO">Em andamento</option>
            <option value="FINALIZADO">Finalizado</option>
            <option value="CANCELADO">Cancelado</option>
          </select>
        </label>
      </div>
    </section>

    <section class="cards list-cards">
      <div class="list-toolbar">
        <h2>Lista de atendimento</h2>
        <span class="muted-inline">{{ atendimentosFiltrados.length }} registro(s)</span>
      </div>

      @for (assistencia of atendimentosPaginados; track assistencia.id) {
        <article class="card assistance-card">
          <div class="card-header">
            <div>
              <span class="status-badge" [class.status-ok]="assistencia.status === 'FINALIZADO'" [class.status-danger]="assistencia.status === 'CANCELADO'">
                {{ formatarStatus(assistencia.status) }}
              </span>
              <h3>{{ assistencia.nome }}</h3>
            </div>
          </div>

          <dl class="info-list">
            <div><dt>WhatsApp</dt><dd>{{ assistencia.whatsapp || '-' }}</dd></div>
            <div><dt>Cidade</dt><dd>{{ assistencia.cidade || '-' }}</dd></div>
            <div><dt>Entidade</dt><dd>{{ assistencia.entidadeConsulta || '-' }}</dd></div>
            <div><dt>Tratamentos</dt><dd>{{ assistencia.tratamentos?.join(', ') || '-' }}</dd></div>
            <div><dt>Observações</dt><dd>{{ assistencia.observacoes || '-' }}</dd></div>
          </dl>

          <div class="actions-row">
            <button class="secondary-button" type="button" (click)="editar(assistencia)">Editar</button>
            @if (assistencia.id && assistencia.status !== 'FINALIZADO') {
              <button type="button" (click)="finalizar(assistencia.id)">Finalizar</button>
            }
          </div>
        </article>
      } @empty {
        <div class="empty-state">Nenhum atendimento encontrado para esta data.</div>
      }
    </section>

    <nav class="pagination-bar" aria-label="Paginação de atendimentos">
      <button type="button" class="secondary-button" (click)="paginaAnterior()" [disabled]="paginaAtual === 1">Anterior</button>
      <span>Página {{ paginaAtual }} de {{ totalPaginas }}</span>
      <button type="button" class="secondary-button" (click)="proximaPagina()" [disabled]="paginaAtual === totalPaginas">Próxima</button>
    </nav>
  `,
})
export class AtendimentoComponent implements OnInit {
  assistencias: Assistencia[] = [];
  form: Assistencia = this.novoAtendimento();
  assistenciaEditandoId: number | null = null;
  busca = '';
  statusSelecionado = '';
  dataAtendimento = new Date().toISOString().split('T')[0];
  paginaAtual = 1;
  itensPorPagina = 8;
  carregando = false;
  salvando = false;
  mensagemErro = '';
  mensagemSucesso = '';
  tratamentosDisponiveis = [
    { valor: 'CANALIZACAO', label: 'Canalização' },
    { valor: 'FUNDANGA', label: 'Fundanga' },
    { valor: 'MAGIA_DIVINA', label: 'Magia Divina' },
    { valor: 'DRACOMETRIA', label: 'Dracometria' },
    { valor: 'REIKI', label: 'Reiki' },
    { valor: 'BANHO_AGUA_NO_ORI', label: 'Banho Água no Ori' },
    { valor: 'BANHO_DE_DEBURU', label: 'Banho de Deburu' },
    { valor: 'PRATO_OFERENDA', label: 'Prato Oferenda' },
    { valor: 'ORO', label: 'Orô' },
    { valor: 'BARRA_DE_ACCESS', label: 'Barra de Access' },
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
    this.apiService.get<Assistencia[]>('/assistencias')
      .pipe(finalize(() => (this.carregando = false)))
      .subscribe({
        next: (dados) => {
          this.assistencias = dados;
          this.paginaAtual = 1;
        },
        error: (error) => (this.mensagemErro = friendlyHttpError(error, 'Não consegui carregar os atendimentos.')),
      });
  }

  salvarAtendimento(): void {
    this.limparMensagens();

    if (!this.form.nome?.trim()) {
      this.mensagemErro = 'Informe o nome da pessoa atendida.';
      return;
    }

    this.salvando = true;
    const payload: Assistencia = {
      ...this.form,
      dataConsulta: this.dataAtendimento,
      status: this.form.status || 'EM_ANDAMENTO',
      tratamentos: this.form.tratamentos || [],
      sessoes: [],
    };

    const requisicao = this.assistenciaEditandoId
      ? this.apiService.put<Assistencia>(`/assistencias/${this.assistenciaEditandoId}`, payload)
      : this.apiService.post<Assistencia>('/assistencias', payload);

    requisicao.pipe(finalize(() => (this.salvando = false))).subscribe({
      next: () => {
        this.limparFormulario();
        this.mensagemSucesso = 'Atendimento salvo na lista do dia.';
        this.carregar(true);
      },
      error: (error) => (this.mensagemErro = friendlyHttpError(error, 'Não consegui salvar o atendimento.')),
    });
  }

  editar(assistencia: Assistencia): void {
    this.assistenciaEditandoId = assistencia.id || null;
    this.form = {
      ...assistencia,
      tratamentos: [...(assistencia.tratamentos || [])],
      sessoes: [...(assistencia.sessoes || [])],
    };
    this.dataAtendimento = assistencia.dataConsulta || this.dataAtendimento;
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  finalizar(id: number): void {
    if (!confirm('Deseja finalizar este atendimento?')) {
      return;
    }

    this.limparMensagens();
    this.salvando = true;
    this.apiService.put<Assistencia>(`/assistencias/${id}/finalizar`)
      .pipe(finalize(() => (this.salvando = false)))
      .subscribe({
        next: () => {
          this.mensagemSucesso = 'Atendimento finalizado.';
          this.carregar(true);
        },
        error: (error) => (this.mensagemErro = friendlyHttpError(error, 'Não consegui finalizar o atendimento.')),
      });
  }

  exportarAtendimentosDoDia(): void {
    this.limparMensagens();

    if (!this.atendimentosDoDia.length) {
      this.mensagemErro = 'Não há atendimentos para exportar nesta data.';
      return;
    }

    const linhas = this.atendimentosDoDia.map((item) => ({
      data: item.dataConsulta || '',
      nome: item.nome,
      whatsapp: item.whatsapp || '',
      cidade: item.cidade || '',
      entidade: item.entidadeConsulta || '',
      status: this.formatarStatus(item.status),
      tratamentos: (item.tratamentos || []).join(', '),
      observacoes: item.observacoes || '',
    }));

    this.baixarCsv(`atendimentos-${this.dataAtendimento}.csv`, linhas);
    this.mensagemSucesso = 'Relatório do dia gerado com sucesso.';
  }

  get atendimentosDoDia(): Assistencia[] {
    return this.assistencias
      .filter((assistencia) => assistencia.dataConsulta === this.dataAtendimento)
      .sort((a, b) => String(a.id || 0).localeCompare(String(b.id || 0), undefined, { numeric: true }));
  }

  get atendimentosFiltrados(): Assistencia[] {
    const termo = this.normalizar(this.busca);

    return this.atendimentosDoDia.filter((assistencia) => {
      const texto = this.normalizar([
        assistencia.nome,
        assistencia.whatsapp || '',
        assistencia.cidade || '',
        assistencia.entidadeConsulta || '',
        (assistencia.tratamentos || []).join(' '),
      ].join(' '));
      const bateBusca = !termo || texto.includes(termo);
      const bateStatus = !this.statusSelecionado || assistencia.status === this.statusSelecionado;

      return bateBusca && bateStatus;
    });
  }

  get atendimentosPaginados(): Assistencia[] {
    const inicio = (this.paginaAtual - 1) * this.itensPorPagina;
    return this.atendimentosFiltrados.slice(inicio, inicio + this.itensPorPagina);
  }

  get totalPaginas(): number {
    return Math.max(Math.ceil(this.atendimentosFiltrados.length / this.itensPorPagina), 1);
  }

  get totalEmAndamento(): number {
    return this.atendimentosDoDia.filter((item) => item.status === 'EM_ANDAMENTO').length;
  }

  get totalFinalizados(): number {
    return this.atendimentosDoDia.filter((item) => item.status === 'FINALIZADO').length;
  }

  get totalCancelados(): number {
    return this.atendimentosDoDia.filter((item) => item.status === 'CANCELADO').length;
  }

  get dataAtendimentoFormatada(): string {
    return formatDate(this.dataAtendimento);
  }

  paginaAnterior(): void {
    this.paginaAtual = Math.max(this.paginaAtual - 1, 1);
  }

  proximaPagina(): void {
    this.paginaAtual = Math.min(this.paginaAtual + 1, this.totalPaginas);
  }

  tratamentoSelecionado(valor: string): boolean {
    return this.form.tratamentos?.includes(valor) || false;
  }

  alternarTratamento(valor: string, event: Event): void {
    const marcado = (event.target as HTMLInputElement).checked;
    const tratamentos = new Set(this.form.tratamentos || []);

    if (marcado) {
      tratamentos.add(valor);
    } else {
      tratamentos.delete(valor);
    }

    this.form.tratamentos = Array.from(tratamentos);
  }

  limparFormulario(): void {
    this.assistenciaEditandoId = null;
    this.form = this.novoAtendimento();
  }

  formatarStatus(status: string): string {
    return {
      EM_ANDAMENTO: 'Em andamento',
      FINALIZADO: 'Finalizado',
      CANCELADO: 'Cancelado',
    }[status] || status;
  }

  private novoAtendimento(): Assistencia {
    return {
      nome: '',
      entidadeConsulta: '',
      dataConsulta: this.dataAtendimento,
      cidade: '',
      whatsapp: '',
      observacoes: '',
      status: 'EM_ANDAMENTO',
      tratamentos: [],
      sessoes: [],
    };
  }

  private baixarCsv(nomeArquivo: string, linhas: Record<string, string | number>[]): void {
    const colunas = Object.keys(linhas[0]);
    const conteudo = [
      colunas.join(';'),
      ...linhas.map((linha) => colunas.map((coluna) => this.formatarCelulaCsv(linha[coluna])).join(';')),
    ].join('\n');

    const blob = new Blob([`\uFEFF${conteudo}`], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = nomeArquivo;
    link.click();
    URL.revokeObjectURL(url);
  }

  private formatarCelulaCsv(valor: string | number): string {
    return `"${String(valor).replaceAll('"', '""')}"`;
  }

  private normalizar(valor: string): string {
    return valor.toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '');
  }

  private limparMensagens(): void {
    this.mensagemErro = '';
    this.mensagemSucesso = '';
  }
}
