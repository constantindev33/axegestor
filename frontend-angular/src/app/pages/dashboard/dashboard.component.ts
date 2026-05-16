import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';

import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { Assistencia, LancamentoFinanceiro, Material, Membro, ResumoMensalidade } from '../../core/models';
import { formatCurrency, formatDate } from '../../shared/formatters';

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink],
  template: `
    <header class="page-header">
      <div>
        <p class="eyebrow">Visão geral</p>
        <h1>Dashboard</h1>
      </div>
      <button type="button" class="secondary-button" (click)="carregar()">Atualizar</button>
    </header>

    <section class="cards dashboard-metrics">
      <article class="card metric-card">
        <span>Saldo financeiro</span>
        <strong>{{ formatCurrency(saldo) }}</strong>
        <small>{{ formatCurrency(receitas) }} em receitas | {{ formatCurrency(despesas) }} em despesas</small>
      </article>
      <article class="card metric-card">
        <span>Mensalidades atrasadas</span>
        <strong>{{ mensalidadesAtrasadas }}</strong>
        <small>{{ mensalidades.length }} membros acompanhados</small>
      </article>
      <article class="card metric-card">
        <span>Assistências em andamento</span>
        <strong>{{ assistenciasAndamento }}</strong>
        <small>{{ assistencias.length }} assistências no total</small>
      </article>
      <article class="card metric-card">
        <span>Estoque baixo</span>
        <strong>{{ estoqueBaixo.length }}</strong>
        <small>Materiais abaixo do mínimo</small>
      </article>
      <article class="card metric-card">
        <span>Membros ativos</span>
        <strong>{{ membrosAtivos }}</strong>
        <small>{{ membros.length }} membros no total</small>
      </article>
    </section>

    <section class="alerts-panel">
      @if (mensalidadesAtrasadas > 0) {
        <div class="alert-card danger">{{ mensalidadesAtrasadas }} mensalidade(s) atrasada(s)</div>
      }
      @if (mensalidadesSemCadastro > 0) {
        <div class="alert-card warn">{{ mensalidadesSemCadastro }} membro(s) sem mensalidade no mês</div>
      }
      @if (estoqueBaixo.length > 0) {
        <div class="alert-card warn">{{ estoqueBaixo.length }} material(is) com estoque baixo</div>
      }
      @if (mensalidadesAtrasadas === 0 && mensalidadesSemCadastro === 0 && estoqueBaixo.length === 0) {
        <div class="alert-card ok">Tudo certo nos principais alertas do mês.</div>
      }
    </section>

    <section class="dashboard-grid charts-grid">
      <article class="card chart-card">
        <div class="section-title"><h2>Receitas x despesas</h2><span>Mes atual</span></div>
        <div class="bar-chart">
          <div class="bar-row">
            <span>Receitas</span>
            <div class="bar-track"><div class="bar-fill income" [style.width.%]="percentual(receitas, maiorValorFinanceiro)"></div></div>
            <strong>{{ formatCurrency(receitas) }}</strong>
          </div>
          <div class="bar-row">
            <span>Despesas</span>
            <div class="bar-track"><div class="bar-fill expense" [style.width.%]="percentual(despesas, maiorValorFinanceiro)"></div></div>
            <strong>{{ formatCurrency(despesas) }}</strong>
          </div>
        </div>
      </article>

      <article class="card chart-card">
        <div class="section-title"><h2>Mensalidades</h2><span>{{ mensalidades.length }} membros</span></div>
        <div class="donut-row">
          <div class="donut-chart" [style.background]="mensalidadesChart"></div>
          <div class="legend-list">
            <span><i class="legend-dot ok"></i>Em dia: {{ mensalidadesEmDia }}</span>
            <span><i class="legend-dot danger"></i>Atrasadas: {{ mensalidadesAtrasadas }}</span>
            <span><i class="legend-dot warn"></i>Sem mensalidade: {{ mensalidadesSemCadastro }}</span>
          </div>
        </div>
      </article>

      <article class="card chart-card">
        <div class="section-title"><h2>Assistencias por status</h2><span>{{ assistencias.length }} registros</span></div>
        <div class="bar-chart">
          @for (item of assistenciasPorStatus; track item.label) {
            <div class="bar-row">
              <span>{{ item.label }}</span>
              <div class="bar-track"><div class="bar-fill neutral" [style.width.%]="percentual(item.total, maiorStatusAssistencia)"></div></div>
              <strong>{{ item.total }}</strong>
            </div>
          }
        </div>
      </article>
    </section>

    <section class="dashboard-grid">
      <article class="card">
        <div class="section-title"><h2>Mensalidades críticas</h2><a routerLink="/financeiro">Ver financeiro</a></div>
        <div class="compact-list">
          @for (item of mensalidadesCriticas; track item.membroId) {
            <div class="compact-item">
              <div><strong>{{ item.nome }}</strong><span>{{ statusMensalidade(item.statusMensalidade) }} | {{ formatCurrency(item.valor) }}</span></div>
              <small>{{ formatDate(item.dataVencimento) }}</small>
            </div>
          } @empty {
            <div class="empty-state">Nenhuma mensalidade crítica.</div>
          }
        </div>
      </article>

      <article class="card">
        <div class="section-title"><h2>Assistências recentes</h2><a routerLink="/assistencias">Ver assistências</a></div>
        <div class="compact-list">
          @for (item of assistenciasRecentes; track item.id) {
            <div class="compact-item">
              <div><strong>{{ item.nome }}</strong><span>{{ statusAssistencia(item.status) }} | {{ item.cidade || '-' }}</span></div>
              <small>{{ formatDate(item.dataConsulta) }}</small>
            </div>
          } @empty {
            <div class="empty-state">Nenhuma assistência cadastrada.</div>
          }
        </div>
      </article>

      <article class="card">
        <div class="section-title"><h2>Estoque baixo</h2><a routerLink="/estoque">Ver estoque</a></div>
        <div class="compact-list">
          @for (item of estoqueBaixo; track item.id) {
            <div class="compact-item">
              <div><strong>{{ item.nome }}</strong><span>{{ item.quantidadeAtual }} {{ item.unidadeMedida }} disponíveis</span></div>
              <small>Mín. {{ item.quantidadeMinima }}</small>
            </div>
          } @empty {
            <div class="empty-state">Nenhum material em estoque baixo.</div>
          }
        </div>
      </article>
    </section>
  `,
})
export class DashboardComponent implements OnInit {
  assistencias: Assistencia[] = [];
  financeiro: LancamentoFinanceiro[] = [];
  estoqueBaixo: Material[] = [];
  membros: Membro[] = [];
  mensalidades: ResumoMensalidade[] = [];

  constructor(
    private readonly apiService: ApiService,
    private readonly authService: AuthService,
  ) {}

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    const hoje = new Date();
    const ano = hoje.getFullYear();
    const mes = hoje.getMonth() + 1;

    if (this.podeVer(['ADMIN', 'ASSISTENCIA'])) {
      this.apiService.get<Assistencia[]>('/assistencias').subscribe((dados) => (this.assistencias = dados));
    }

    if (this.podeVer(['ADMIN', 'FINANCEIRO'])) {
      this.apiService.get<LancamentoFinanceiro[]>('/financeiro').subscribe((dados) => (this.financeiro = dados));
      this.apiService.get<ResumoMensalidade[]>(`/financeiro/mensalidades/membros?ano=${ano}&mes=${mes}`).subscribe((dados) => (this.mensalidades = dados));
    }

    if (this.podeVer(['ADMIN', 'ESTOQUE'])) {
      this.apiService.get<Material[]>('/almoxarifado/materiais/estoque-baixo').subscribe((dados) => (this.estoqueBaixo = dados));
    }

    if (this.podeVer(['ADMIN', 'FINANCEIRO', 'ASSISTENCIA', 'ESTOQUE'])) {
      this.apiService.get<Membro[]>('/membros').subscribe((dados) => (this.membros = dados));
    }
  }

  get receitas(): number {
    return this.financeiro.filter((item) => item.tipo === 'RECEITA').reduce((total, item) => total + Number(item.valor || 0), 0);
  }

  get despesas(): number {
    return this.financeiro.filter((item) => item.tipo === 'DESPESA').reduce((total, item) => total + Number(item.valor || 0), 0);
  }

  get saldo(): number {
    return this.receitas - this.despesas;
  }

  get mensalidadesAtrasadas(): number {
    return this.mensalidades.filter((item) => item.statusMensalidade === 'ATRASADA').length;
  }

  get mensalidadesEmDia(): number {
    return this.mensalidades.filter((item) => item.statusMensalidade === 'EM_DIA').length;
  }

  get mensalidadesSemCadastro(): number {
    return this.mensalidades.filter((item) => item.statusMensalidade === 'SEM_MENSALIDADE').length;
  }

  get assistenciasAndamento(): number {
    return this.assistencias.filter((item) => item.status === 'EM_ANDAMENTO').length;
  }

  get membrosAtivos(): number {
    return this.membros.filter((item) => item.status === 'ATIVO').length;
  }

  get mensalidadesCriticas(): ResumoMensalidade[] {
    return this.mensalidades.filter((item) => ['ATRASADA', 'PENDENTE', 'SEM_MENSALIDADE'].includes(item.statusMensalidade)).slice(0, 6);
  }

  get assistenciasRecentes(): Assistencia[] {
    return [...this.assistencias].sort((a, b) => String(b.dataConsulta || '').localeCompare(String(a.dataConsulta || ''))).slice(0, 6);
  }

  get maiorValorFinanceiro(): number {
    return Math.max(this.receitas, this.despesas, 1);
  }

  get assistenciasPorStatus(): { label: string; total: number }[] {
    return [
      { label: 'Em andamento', total: this.assistencias.filter((item) => item.status === 'EM_ANDAMENTO').length },
      { label: 'Finalizadas', total: this.assistencias.filter((item) => item.status === 'FINALIZADO').length },
      { label: 'Canceladas', total: this.assistencias.filter((item) => item.status === 'CANCELADO').length },
    ];
  }

  get maiorStatusAssistencia(): number {
    return Math.max(...this.assistenciasPorStatus.map((item) => item.total), 1);
  }

  get mensalidadesChart(): string {
    const total = Math.max(this.mensalidades.length, 1);
    const emDia = (this.mensalidadesEmDia / total) * 100;
    const atrasadas = (this.mensalidadesAtrasadas / total) * 100;
    const semCadastro = (this.mensalidadesSemCadastro / total) * 100;
    const fimEmDia = emDia;
    const fimAtrasadas = emDia + atrasadas;
    const fimSemCadastro = fimAtrasadas + semCadastro;

    return `conic-gradient(#22c55e 0 ${fimEmDia}%, #ef4444 ${fimEmDia}% ${fimAtrasadas}%, #eab308 ${fimAtrasadas}% ${fimSemCadastro}%, #64748b ${fimSemCadastro}% 100%)`;
  }

  percentual(valor: number, total: number): number {
    return Math.round((Number(valor || 0) / Math.max(total, 1)) * 100);
  }

  podeVer(perfis: string[]): boolean {
    return this.authService.hasAnyPerfil(perfis);
  }

  formatCurrency = formatCurrency;
  formatDate = formatDate;

  statusMensalidade(status: string): string {
    return {
      EM_DIA: 'Em dia',
      ATRASADA: 'Atrasada',
      PENDENTE: 'Pendente',
      SEM_MENSALIDADE: 'Sem mensalidade',
      CANCELADO: 'Cancelada',
    }[status] || status;
  }

  statusAssistencia(status: string): string {
    return {
      EM_ANDAMENTO: 'Em andamento',
      FINALIZADO: 'Finalizada',
      CANCELADO: 'Cancelada',
    }[status] || status;
  }
}
