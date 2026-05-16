import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { ApiService } from '../../core/api.service';
import { LancamentoFinanceiro, RelatorioFinanceiroMensal, ResumoMensalidade } from '../../core/models';
import { formatCurrency, formatDate } from '../../shared/formatters';

@Component({
  selector: 'app-relatorios',
  imports: [FormsModule],
  template: `
    <header class="page-header">
      <div>
        <p class="eyebrow">Prestação de contas</p>
        <h1>Relatórios</h1>
      </div>
      <button class="secondary-button" type="button" (click)="carregar()" [disabled]="carregandoRelatorio">Atualizar</button>
    </header>

    <section class="card search-card">
      <h2>Período do relatório</h2>
      <div class="search-row">
        <input type="month" [(ngModel)]="mesReferencia" />
        <button type="button" (click)="carregar()" [disabled]="carregandoRelatorio">{{ carregandoRelatorio ? 'Carregando...' : 'Gerar relatório' }}</button>
        <button type="button" class="secondary-button" (click)="exportarResumo()" [disabled]="botoesDesabilitados">CSV resumo</button>
        <button type="button" class="secondary-button" (click)="exportarLancamentos()" [disabled]="botoesDesabilitados">CSV lançamentos</button>
        <button type="button" class="secondary-button" (click)="exportarMensalidades()" [disabled]="botoesDesabilitados">CSV mensalidades</button>
        <button type="button" class="secondary-button" (click)="baixarExcel()" [disabled]="botoesDesabilitados">Excel .xlsx</button>
        <button type="button" class="secondary-button" (click)="baixarPdf()" [disabled]="botoesDesabilitados">PDF prestação</button>
      </div>
      @if (!relatorio && !mensagemErro && !carregandoRelatorio) {
        <p class="muted">Clique em Gerar relatório antes de baixar os arquivos.</p>
      }
      @if (carregandoArquivo) {
        <p class="muted">Preparando arquivo para download...</p>
      }
      @if (mensagemErro) {
        <p class="error-message">{{ mensagemErro }}</p>
      }
    </section>

    @if (relatorio) {
      <section class="cards dashboard-metrics">
        <article class="card metric-card">
          <span>Receitas</span>
          <strong>{{ formatCurrency(relatorio.receitas) }}</strong>
          <small>{{ relatorio.totalLancamentos }} lançamento(s) no mês</small>
        </article>
        <article class="card metric-card">
          <span>Despesas</span>
          <strong>{{ formatCurrency(relatorio.despesas) }}</strong>
          <small>{{ relatorio.totalPagos }} pago(s), {{ relatorio.totalPendentes }} pendente(s)</small>
        </article>
        <article class="card metric-card">
          <span>Saldo</span>
          <strong>{{ formatCurrency(relatorio.saldo) }}</strong>
          <small>Receitas menos despesas</small>
        </article>
        <article class="card metric-card">
          <span>Mensalidades atrasadas</span>
          <strong>{{ relatorio.mensalidadesAtrasadas }}</strong>
          <small>{{ relatorio.mensalidadesEmDia }} em dia | {{ relatorio.mensalidadesSemCadastro }} sem cadastro</small>
        </article>
      </section>

      <section class="dashboard-grid">
        <article class="card">
          <div class="section-title"><h2>Por categoria</h2><span class="muted-inline">{{ relatorio.porCategoria.length }} categoria(s)</span></div>
          <div class="table-wrapper">
            <table class="data-table compact-table">
              <thead><tr><th>Categoria</th><th>Qtd.</th><th>Total</th></tr></thead>
              <tbody>
                @for (item of relatorio.porCategoria; track item.chave) {
                  <tr><td>{{ formatarChave(item.chave) }}</td><td>{{ item.quantidade }}</td><td>{{ formatCurrency(item.total) }}</td></tr>
                } @empty {
                  <tr><td colspan="3">Sem lançamentos no período.</td></tr>
                }
              </tbody>
            </table>
          </div>
        </article>

        <article class="card">
          <div class="section-title"><h2>Por status</h2><span class="muted-inline">{{ relatorio.porStatus.length }} status</span></div>
          <div class="table-wrapper">
            <table class="data-table compact-table">
              <thead><tr><th>Status</th><th>Qtd.</th><th>Total</th></tr></thead>
              <tbody>
                @for (item of relatorio.porStatus; track item.chave) {
                  <tr><td>{{ formatarChave(item.chave) }}</td><td>{{ item.quantidade }}</td><td>{{ formatCurrency(item.total) }}</td></tr>
                } @empty {
                  <tr><td colspan="3">Sem lançamentos no período.</td></tr>
                }
              </tbody>
            </table>
          </div>
        </article>
      </section>

      <section class="card list-cards">
        <div class="section-title">
          <h2>Mensalidades críticas</h2>
          <span class="muted-inline">{{ mensalidadesCriticas.length }} membro(s)</span>
        </div>
        <div class="table-wrapper">
          <table class="data-table">
            <thead><tr><th>Membro</th><th>Status</th><th>Valor</th><th>Vencimento</th><th>Pagamento</th></tr></thead>
            <tbody>
              @for (item of mensalidadesCriticas; track item.membroId) {
                <tr>
                  <td>{{ item.nome }}</td>
                  <td>{{ statusMensalidade(item.statusMensalidade) }}</td>
                  <td>{{ formatCurrency(item.valor) }}</td>
                  <td>{{ formatDate(item.dataVencimento) }}</td>
                  <td>{{ formatDate(item.dataPagamento) }}</td>
                </tr>
              } @empty {
                <tr><td colspan="5">Nenhuma mensalidade crítica neste mês.</td></tr>
              }
            </tbody>
          </table>
        </div>
      </section>
    } @else {
      <div class="empty-state">Gere um relatório para visualizar os dados.</div>
    }
  `,
})
export class RelatoriosComponent implements OnInit {
  mesReferencia = new Date().toISOString().slice(0, 7);
  relatorio: RelatorioFinanceiroMensal | null = null;
  mensagemErro = '';
  carregandoRelatorio = false;
  carregandoArquivo = false;

  constructor(private readonly apiService: ApiService) {}

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.mensagemErro = '';
    this.carregandoRelatorio = true;
    const [ano, mes] = this.mesReferencia.split('-');
    this.apiService.get<RelatorioFinanceiroMensal>(`/financeiro/relatorios/resumo-mensal?ano=${ano}&mes=${Number(mes)}`).subscribe({
      next: (dados) => {
        this.relatorio = dados;
        this.carregandoRelatorio = false;
      },
      error: () => {
        this.relatorio = null;
        this.carregandoRelatorio = false;
        this.mensagemErro = 'Não consegui carregar o relatório. Confirme se o backend está ligado e atualizado.';
      },
    });
  }

  get botoesDesabilitados(): boolean {
    return !this.relatorio || this.carregandoRelatorio || this.carregandoArquivo;
  }

  get mensalidadesCriticas(): ResumoMensalidade[] {
    return (this.relatorio?.mensalidades || []).filter((item) => ['ATRASADA', 'PENDENTE', 'SEM_MENSALIDADE'].includes(item.statusMensalidade));
  }

  exportarResumo(): void {
    if (!this.relatorio) {
      return;
    }

    const resumo = [
      { indicador: 'Receitas', valor: this.relatorio.receitas },
      { indicador: 'Despesas', valor: this.relatorio.despesas },
      { indicador: 'Saldo', valor: this.relatorio.saldo },
      { indicador: 'Total de lançamentos', valor: this.relatorio.totalLancamentos },
      { indicador: 'Pagos', valor: this.relatorio.totalPagos },
      { indicador: 'Pendentes', valor: this.relatorio.totalPendentes },
      { indicador: 'Atrasados', valor: this.relatorio.totalAtrasados },
      { indicador: 'Mensalidades em dia', valor: this.relatorio.mensalidadesEmDia },
      { indicador: 'Mensalidades atrasadas', valor: this.relatorio.mensalidadesAtrasadas },
      { indicador: 'Mensalidades sem cadastro', valor: this.relatorio.mensalidadesSemCadastro },
    ];

    this.baixarCsv(`relatorio-resumo-${this.mesReferencia}.csv`, resumo);
  }

  exportarLancamentos(): void {
    this.baixarCsv(`relatorio-lancamentos-${this.mesReferencia}.csv`, (this.relatorio?.lancamentos || []).map((item: LancamentoFinanceiro) => ({
      descricao: item.descricao,
      responsavel: item.responsavel || '',
      valor: item.valor || 0,
      dataLancamento: item.dataLancamento,
      vencimento: item.dataVencimento || '',
      pagamento: item.dataPagamento || '',
      tipo: item.tipo,
      categoria: item.categoria,
      status: item.status,
    })));
  }

  exportarMensalidades(): void {
    this.baixarCsv(`relatorio-mensalidades-${this.mesReferencia}.csv`, (this.relatorio?.mensalidades || []).map((item: ResumoMensalidade) => ({
      membro: item.nome,
      telefone: item.telefone || '',
      email: item.email || '',
      statusMembro: item.statusMembro,
      statusMensalidade: this.statusMensalidade(item.statusMensalidade),
      valor: item.valor || 0,
      vencimento: item.dataVencimento || '',
      pagamento: item.dataPagamento || '',
    })));
  }

  baixarExcel(): void {
    this.mensagemErro = '';
    this.carregandoArquivo = true;
    const [ano, mes] = this.mesReferencia.split('-');
    this.apiService.getBlob(`/financeiro/relatorios/resumo-mensal.xlsx?ano=${ano}&mes=${Number(mes)}`)
      .subscribe({
        next: (arquivo) => {
          this.baixarBlob(arquivo, `relatorio-axegestor-${this.mesReferencia}.xlsx`);
          this.carregandoArquivo = false;
        },
        error: () => {
          this.carregandoArquivo = false;
          this.mensagemErro = 'Não consegui baixar o Excel. Reinicie o backend para carregar os novos endpoints de relatório.';
        },
      });
  }

  baixarPdf(): void {
    this.mensagemErro = '';
    this.carregandoArquivo = true;
    const [ano, mes] = this.mesReferencia.split('-');
    this.apiService.getBlob(`/financeiro/relatorios/resumo-mensal.pdf?ano=${ano}&mes=${Number(mes)}`)
      .subscribe({
        next: (arquivo) => {
          this.baixarBlob(arquivo, `prestacao-contas-axegestor-${this.mesReferencia}.pdf`);
          this.carregandoArquivo = false;
        },
        error: () => {
          this.carregandoArquivo = false;
          this.mensagemErro = 'Não consegui baixar o PDF. Reinicie o backend para carregar os novos endpoints de relatório.';
        },
      });
  }

  formatarChave(valor: string): string {
    return valor.toLowerCase().replaceAll('_', ' ').replace(/(^|\s)\S/g, (letra) => letra.toUpperCase());
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

  private baixarCsv(nomeArquivo: string, linhas: Record<string, string | number>[]): void {
    if (!linhas.length) {
      alert('Não há dados para exportar.');
      return;
    }

    const colunas = Object.keys(linhas[0]);
    const conteudo = [
      colunas.join(';'),
      ...linhas.map((linha) => colunas.map((coluna) => this.formatarCelulaCsv(linha[coluna])).join(';')),
    ].join('\n');

    const blob = new Blob([`\uFEFF${conteudo}`], { type: 'text/csv;charset=utf-8;' });
    this.baixarBlob(blob, nomeArquivo);
  }

  private baixarBlob(arquivo: Blob, nomeArquivo: string): void {
    const url = URL.createObjectURL(arquivo);
    const link = document.createElement('a');
    link.href = url;
    link.download = nomeArquivo;
    link.click();
    URL.revokeObjectURL(url);
  }

  private formatarCelulaCsv(valor: string | number): string {
    return `"${String(valor).replaceAll('"', '""')}"`;
  }
}
