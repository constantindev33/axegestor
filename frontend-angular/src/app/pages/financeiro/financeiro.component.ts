import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { ApiService } from '../../core/api.service';
import { LancamentoFinanceiro, Membro, ResumoMensalidade } from '../../core/models';
import { formatCurrency, formatDate } from '../../shared/formatters';

@Component({
  selector: 'app-financeiro',
  imports: [FormsModule],
  template: `
    <header class="page-header">
      <div><p class="eyebrow">Controle financeiro</p><h1>Financeiro</h1></div>
      <button class="secondary-button" type="button" (click)="carregar()">Atualizar</button>
    </header>

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
      <div class="actions-row"><button type="button" (click)="salvar()">Salvar lançamento</button></div>
    </section>

    <section class="card search-card">
      <h2>Mensalidades dos membros</h2>
      <div class="search-row">
        <input type="month" [(ngModel)]="mesMensalidade" />
        <button type="button" (click)="carregarMensalidades()">Buscar mês</button>
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

    <section class="cards list-cards">
      @for (item of lancamentos; track item.id) {
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

  constructor(private readonly apiService: ApiService) {}

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.apiService.get<Membro[]>('/membros').subscribe((dados) => (this.membros = dados));
    this.apiService.get<LancamentoFinanceiro[]>('/financeiro').subscribe((dados) => (this.lancamentos = dados));
    this.carregarMensalidades();
  }

  carregarMensalidades(): void {
    const [ano, mes] = this.mesMensalidade.split('-');
    this.apiService.get<ResumoMensalidade[]>(`/financeiro/mensalidades/membros?ano=${ano}&mes=${Number(mes)}`).subscribe((dados) => (this.mensalidades = dados));
  }

  salvar(): void {
    const payload = {
      ...this.form,
      membro: this.membroId ? { id: this.membroId } : null,
      dataPagamento: this.form.status === 'PAGO' ? new Date().toISOString().split('T')[0] : null,
    };

    this.apiService.post<LancamentoFinanceiro>('/financeiro', payload).subscribe(() => {
      this.form = this.novoLancamento();
      this.membroId = null;
      this.carregar();
    });
  }

  marcarComoPago(id: number): void {
    this.apiService.put<LancamentoFinanceiro>(`/financeiro/${id}/pagar`).subscribe(() => this.carregar());
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
}
