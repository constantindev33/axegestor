import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize, forkJoin } from 'rxjs';

import { ApiService } from '../../core/api.service';
import { LancamentoFinanceiro, Membro, ResumoMensalidade } from '../../core/models';
import { formatCurrency, formatDate } from '../../shared/formatters';
import { friendlyHttpError } from '../../shared/http-error';
import { PageFeedbackComponent } from '../../shared/page-feedback.component';

@Component({
  selector: 'app-financeiro',
  imports: [FormsModule, PageFeedbackComponent],
  template: `
    <header class="page-header">
      <div><p class="eyebrow">Controle financeiro</p><h1>Financeiro</h1></div>
      <button class="secondary-button" type="button" (click)="carregar()">Atualizar</button>
    </header>

    <app-page-feedback
      [loading]="carregando"
      loadingText="Carregando financeiro..."
      [success]="mensagemSucesso"
      [error]="mensagemErro"
    />

    <section class="card form-card">
      <h2>Novo lançamento</h2>
      <div class="form-grid">
        <label>Descrição<input [(ngModel)]="form.descricao" /></label>
        <label>Membro
          <select [(ngModel)]="membroId">
            <option [ngValue]="null">Sem membro vinculado</option>
            @for (membro of membros; track membro.id) {
              <option [ngValue]="membro.id">{{ membro.nome }}</option>
            }
          </select>
        </label>
        <label>Responsável<input [(ngModel)]="form.responsavel" /></label>
        <label>Valor<input type="number" [(ngModel)]="form.valor" /></label>
        <label>Data do lançamento<input type="date" [(ngModel)]="form.dataLancamento" /></label>
        <label>Vencimento<input type="date" [(ngModel)]="form.dataVencimento" /></label>
        <label>Tipo<select [(ngModel)]="form.tipo"><option value="RECEITA">Receita</option><option value="DESPESA">Despesa</option></select></label>
        <label>Categoria
          <select [(ngModel)]="form.categoria">
            @for (categoria of categorias; track categoria) {
              <option [value]="categoria">{{ categoria }}</option>
            }
          </select>
        </label>
        <label>Status<select [(ngModel)]="form.status"><option value="PENDENTE">Pendente</option><option value="PAGO">Pago</option><option value="ATRASADO">Atrasado</option><option value="CANCELADO">Cancelado</option></select></label>
      </div>
      <div class="actions-row"><button type="button" (click)="salvar()" [disabled]="salvando">{{ salvando ? 'Salvando...' : 'Salvar lançamento' }}</button></div>
    </section>

    <section class="card search-card">
      <h2>Mensalidades dos membros</h2>
      <div class="search-row">
        <input type="month" [(ngModel)]="mesMensalidade" />
        <button type="button" (click)="carregarMensalidades()" [disabled]="carregando">Buscar mês</button>
        <button type="button" class="secondary-button" (click)="exportarMensalidades()">Exportar mensalidades</button>
      </div>
      <div class="cards list-cards">
        @for (item of mensalidades; track item.membroId) {
          <article class="card">
            <span class="status-badge" [class.status-ok]="item.statusMensalidade === 'EM_DIA'" [class.status-danger]="item.statusMensalidade === 'ATRASADA'" [class.status-warn]="item.statusMensalidade !== 'EM_DIA' && item.statusMensalidade !== 'ATRASADA'">
              {{ statusMensalidade(item.statusMensalidade) }}
            </span>
            <h3>{{ item.nome }}</h3>
            <div class="info-list">
              <div><dt>Status do membro</dt><dd>{{ item.statusMembro }}</dd></div>
              <div><dt>Valor</dt><dd>{{ formatCurrency(item.valor) }}</dd></div>
              <div><dt>Vencimento</dt><dd>{{ formatDate(item.dataVencimento) }}</dd></div>
              <div><dt>Pagamento</dt><dd>{{ formatDate(item.dataPagamento) }}</dd></div>
            </div>
            @if (item.lancamentoId && item.statusPagamento !== 'PAGO') {
              <div class="actions-row"><button type="button" (click)="marcarComoPago(item.lancamentoId)">Marcar como pago</button></div>
            }
          </article>
        } @empty {
          <div class="empty-state">Nenhum membro cadastrado.</div>
        }
      </div>
    </section>

    <section class="card search-card">
      <div class="section-title">
        <h2>Filtrar lançamentos</h2>
        <button type="button" class="secondary-button" (click)="exportarLancamentos()">Exportar financeiro</button>
      </div>
      <div class="filter-grid">
        <label>Buscar<input [(ngModel)]="filtroBusca" (ngModelChange)="paginaAtual = 1" placeholder="Descrição ou responsável" /></label>
        <label>Tipo
          <select [(ngModel)]="filtroTipo" (ngModelChange)="paginaAtual = 1">
            <option value="">Todos</option>
            <option value="RECEITA">Receita</option>
            <option value="DESPESA">Despesa</option>
          </select>
        </label>
        <label>Status
          <select [(ngModel)]="filtroStatus" (ngModelChange)="paginaAtual = 1">
            <option value="">Todos</option>
            <option value="PENDENTE">Pendente</option>
            <option value="PAGO">Pago</option>
            <option value="ATRASADO">Atrasado</option>
            <option value="CANCELADO">Cancelado</option>
          </select>
        </label>
        <label>Categoria
          <select [(ngModel)]="filtroCategoria" (ngModelChange)="paginaAtual = 1">
            <option value="">Todas</option>
            @for (categoria of categorias; track categoria) {
              <option [value]="categoria">{{ categoria }}</option>
            }
          </select>
        </label>
      </div>
    </section>

    <section class="cards list-cards">
      <div class="list-toolbar">
        <h2>Lançamentos</h2>
        <span class="muted-inline">{{ lancamentosFiltrados.length }} lançamento(s)</span>
      </div>
      @for (item of lancamentosPaginados; track item.id) {
        <article class="card">
          <span class="status-badge">{{ item.status }}</span>
          <h3>{{ item.descricao }}</h3>
          <p>{{ formatCurrency(item.valor) }}</p>
          <div class="info-list">
            <div><dt>Tipo</dt><dd>{{ item.tipo }}</dd></div>
            <div><dt>Categoria</dt><dd>{{ item.categoria }}</dd></div>
            <div><dt>Data</dt><dd>{{ formatDate(item.dataLancamento) }}</dd></div>
            <div><dt>Vencimento</dt><dd>{{ formatDate(item.dataVencimento) }}</dd></div>
          </div>
        </article>
      } @empty {
        <div class="empty-state">Nenhum lançamento encontrado.</div>
      }
    </section>

    <nav class="pagination-bar" aria-label="Paginação de lançamentos">
      <button type="button" class="secondary-button" (click)="paginaAnterior()" [disabled]="paginaAtual === 1">Anterior</button>
      <span>Página {{ paginaAtual }} de {{ totalPaginas }}</span>
      <button type="button" class="secondary-button" (click)="proximaPagina()" [disabled]="paginaAtual === totalPaginas">Próxima</button>
    </nav>
  `,
})
export class FinanceiroComponent implements OnInit {
  lancamentos: LancamentoFinanceiro[] = [];
  membros: Membro[] = [];
  mensalidades: ResumoMensalidade[] = [];
  membroId: number | null = null;
  mesMensalidade = new Date().toISOString().slice(0, 7);
  categorias = ['MENSALIDADE', 'DOACAO', 'MATERIAL_RITUALISTICO', 'ALUGUEL', 'AGUA', 'LUZ', 'INTERNET', 'LIMPEZA', 'MANUTENCAO', 'EVENTO', 'CURSO', 'OUTROS'];
  form: LancamentoFinanceiro = this.novoLancamento();
  filtroBusca = '';
  filtroTipo = '';
  filtroStatus = '';
  filtroCategoria = '';
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

    const [ano, mes] = this.mesMensalidade.split('-');
    this.carregando = true;

    forkJoin({
      membros: this.apiService.get<Membro[]>('/membros'),
      lancamentos: this.apiService.get<LancamentoFinanceiro[]>('/financeiro'),
      mensalidades: this.apiService.get<ResumoMensalidade[]>(`/financeiro/mensalidades/membros?ano=${ano}&mes=${Number(mes)}`),
    })
      .pipe(finalize(() => (this.carregando = false)))
      .subscribe({
        next: ({ membros, lancamentos, mensalidades }) => {
          this.membros = membros;
          this.lancamentos = lancamentos;
          this.mensalidades = mensalidades;
          this.paginaAtual = 1;
        },
        error: (error) => (this.mensagemErro = friendlyHttpError(error, 'Não consegui carregar o financeiro.')),
      });
  }

  carregarMensalidades(preservarMensagem = false): void {
    if (!preservarMensagem) {
      this.limparMensagens();
    }

    const [ano, mes] = this.mesMensalidade.split('-');
    this.carregando = true;

    this.apiService.get<ResumoMensalidade[]>(`/financeiro/mensalidades/membros?ano=${ano}&mes=${Number(mes)}`)
      .pipe(finalize(() => (this.carregando = false)))
      .subscribe({
        next: (dados) => (this.mensalidades = dados),
        error: (error) => (this.mensagemErro = friendlyHttpError(error, 'Não consegui carregar as mensalidades.')),
      });
  }

  salvar(): void {
    this.limparMensagens();
    const payload = {
      ...this.form,
      membro: this.membroId ? { id: this.membroId } : null,
      dataPagamento: this.form.status === 'PAGO' ? new Date().toISOString().split('T')[0] : null,
    };

    this.salvando = true;
    this.apiService.post<LancamentoFinanceiro>('/financeiro', payload)
      .pipe(finalize(() => (this.salvando = false)))
      .subscribe({
        next: () => {
          this.form = this.novoLancamento();
          this.membroId = null;
          this.mensagemSucesso = 'Lançamento salvo com sucesso.';
          this.carregar(true);
        },
        error: (error) => (this.mensagemErro = friendlyHttpError(error, 'Não consegui salvar o lançamento.')),
      });
  }

  marcarComoPago(id: number): void {
    if (!confirm('Confirmar pagamento desta mensalidade?')) {
      return;
    }

    this.limparMensagens();
    this.salvando = true;
    this.apiService.put<LancamentoFinanceiro>(`/financeiro/${id}/pagar`)
      .pipe(finalize(() => (this.salvando = false)))
      .subscribe({
        next: () => {
          this.mensagemSucesso = 'Pagamento confirmado com sucesso.';
          this.carregar(true);
        },
        error: (error) => (this.mensagemErro = friendlyHttpError(error, 'Não consegui confirmar o pagamento.')),
      });
  }

  exportarMensalidades(): void {
    const linhas = this.mensalidades.map((item) => ({
      membro: item.nome,
      telefone: item.telefone || '',
      email: item.email || '',
      statusMembro: item.statusMembro,
      statusMensalidade: this.statusMensalidade(item.statusMensalidade),
      valor: item.valor || 0,
      vencimento: item.dataVencimento || '',
      pagamento: item.dataPagamento || '',
    }));

    this.baixarCsv(`mensalidades-${this.mesMensalidade}.csv`, linhas);
  }

  exportarLancamentos(): void {
    const linhas = this.lancamentosFiltrados.map((item) => ({
      descricao: item.descricao,
      responsavel: item.responsavel || '',
      valor: item.valor || 0,
      dataLancamento: item.dataLancamento,
      vencimento: item.dataVencimento || '',
      pagamento: item.dataPagamento || '',
      tipo: item.tipo,
      categoria: item.categoria,
      status: item.status,
    }));

    this.baixarCsv('financeiro.csv', linhas);
  }

  get lancamentosFiltrados(): LancamentoFinanceiro[] {
    const busca = this.normalizar(this.filtroBusca);

    return this.lancamentos.filter((item) => {
      const texto = this.normalizar(`${item.descricao} ${item.responsavel || ''}`);
      const bateBusca = !busca || texto.includes(busca);
      const bateTipo = !this.filtroTipo || item.tipo === this.filtroTipo;
      const bateStatus = !this.filtroStatus || item.status === this.filtroStatus;
      const bateCategoria = !this.filtroCategoria || item.categoria === this.filtroCategoria;

      return bateBusca && bateTipo && bateStatus && bateCategoria;
    });
  }

  get lancamentosPaginados(): LancamentoFinanceiro[] {
    const inicio = (this.paginaAtual - 1) * this.itensPorPagina;
    return this.lancamentosFiltrados.slice(inicio, inicio + this.itensPorPagina);
  }

  get totalPaginas(): number {
    return Math.max(Math.ceil(this.lancamentosFiltrados.length / this.itensPorPagina), 1);
  }

  paginaAnterior(): void {
    this.paginaAtual = Math.max(this.paginaAtual - 1, 1);
  }

  proximaPagina(): void {
    this.paginaAtual = Math.min(this.paginaAtual + 1, this.totalPaginas);
  }

  statusMensalidade(status: string): string {
    return {
      EM_DIA: 'Em dia',
      ATRASADA: 'Atrasada',
      PENDENTE: 'Pendente',
      SEM_MENSALIDADE: 'Sem mensalidade',
      CANCELADO: 'Cancelada',
    }[status] || status;
  }

  formatCurrency = formatCurrency;
  formatDate = formatDate;

  private novoLancamento(): LancamentoFinanceiro {
    return {
      descricao: '',
      responsavel: '',
      valor: 0,
      dataLancamento: new Date().toISOString().split('T')[0],
      dataVencimento: null,
      tipo: 'RECEITA',
      categoria: 'MENSALIDADE',
      status: 'PENDENTE',
      membro: null,
    };
  }

  private baixarCsv(nomeArquivo: string, linhas: Record<string, string | number>[]): void {
    if (!linhas.length) {
      this.mensagemErro = 'Não há dados para exportar.';
      return;
    }

    const colunas = Object.keys(linhas[0]);
    const conteudo = [
      colunas.join(';'),
      ...linhas.map((linha) => colunas.map((coluna) => this.formatarCelulaCsv(linha[coluna])).join(';')),
    ].join('\n');

    const blob = new Blob([conteudo], { type: 'text/csv;charset=utf-8;' });
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
