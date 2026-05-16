import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { ApiService } from '../../core/api.service';
import { Assistencia, SessaoTratamento } from '../../core/models';
import { formatDate } from '../../shared/formatters';

@Component({
  selector: 'app-assistencias',
  imports: [FormsModule],
  template: `
    <header class="page-header">
      <div>
        <p class="eyebrow">Tratamentos</p>
        <h1>Assistências</h1>
      </div>
      <button class="secondary-button" type="button" (click)="carregar()">Atualizar</button>
    </header>

    <section class="card form-card">
      <h2>{{ assistenciaEditandoId ? 'Editar assistência' : 'Nova assistência' }}</h2>

      <div class="form-grid">
        <label>Nome<input [(ngModel)]="form.nome" placeholder="Nome completo" /></label>
        <label>Entidade da consulta<input [(ngModel)]="form.entidadeConsulta" placeholder="Entidade" /></label>
        <label>Cidade<input [(ngModel)]="form.cidade" placeholder="Cidade" /></label>
        <label>WhatsApp<input [(ngModel)]="form.whatsapp" placeholder="(00) 00000-0000" /></label>
        <label>Status
          <select [(ngModel)]="form.status">
            <option value="EM_ANDAMENTO">Em andamento</option>
            <option value="FINALIZADO">Finalizado</option>
            <option value="CANCELADO">Cancelado</option>
          </select>
        </label>
      </div>

      <label>Observações<textarea [(ngModel)]="form.observacoes" placeholder="Observações importantes"></textarea></label>

      <h3>Tratamentos</h3>
      <div class="tratamentos">
        @for (tratamento of tratamentosDisponiveis; track tratamento.valor) {
          <label>
            <input type="checkbox" [checked]="tratamentoSelecionado(tratamento.valor)" (change)="alternarTratamento(tratamento.valor, $event)" />
            {{ tratamento.label }}
          </label>
        }
      </div>

      <h3>Sessões presenciais</h3>
      <div class="sessoes-form">
        @for (sessao of sessoesDatas; track $index) {
          <input type="date" [(ngModel)]="sessoesDatas[$index]" />
        }
      </div>

      <div class="actions-row">
        <button type="button" (click)="salvar()">Salvar assistência</button>
        <button type="button" class="secondary-button" (click)="limparFormulario()">Limpar</button>
      </div>
    </section>

    <section class="card search-card">
      <h2>Buscar assistência</h2>
      <div class="search-row">
        <input [(ngModel)]="buscaNome" placeholder="Buscar por nome" />
        <button type="button" (click)="buscarPorNome()">Buscar</button>
        <button type="button" class="secondary-button" (click)="carregar()">Mostrar todos</button>
      </div>
    </section>

    <section class="cards list-cards">
      @for (assistencia of assistencias; track assistencia.id) {
        <article class="card assistance-card">
          <div class="card-header">
            <div>
              <span class="status-badge">{{ formatarStatus(assistencia.status) }}</span>
              <h3>{{ assistencia.nome }}</h3>
            </div>
          </div>

          <dl class="info-list">
            <div><dt>Entidade</dt><dd>{{ assistencia.entidadeConsulta || '-' }}</dd></div>
            <div><dt>Cidade</dt><dd>{{ assistencia.cidade || '-' }}</dd></div>
            <div><dt>WhatsApp</dt><dd>{{ assistencia.whatsapp || '-' }}</dd></div>
            <div><dt>Tratamentos</dt><dd>{{ assistencia.tratamentos?.join(', ') || '-' }}</dd></div>
          </dl>

          <div class="sessions-list">
            <strong>Sessões</strong>
            @for (sessao of assistencia.sessoes || []; track sessao.id) {
              <p>
                Sessão {{ sessao.numeroSessao }} - {{ formatDate(sessao.dataSessao) }} -
                {{ sessao.realizada ? 'Realizada' : 'Pendente' }}
                @if (!sessao.realizada && assistencia.id && sessao.id) {
                  <button class="inline-button" type="button" (click)="realizarSessao(assistencia.id, sessao.id)">Marcar realizada</button>
                }
              </p>
            } @empty {
              <p>Nenhuma sessão cadastrada.</p>
            }
          </div>

          <div class="actions-row">
            <button class="secondary-button" type="button" (click)="preencherFormulario(assistencia)">Editar</button>
            @if (assistencia.id) {
              <button class="secondary-button" type="button" (click)="deletar(assistencia.id)">Deletar</button>
            }
            @if (assistencia.id && assistencia.status !== 'FINALIZADO') {
              <button type="button" (click)="finalizar(assistencia.id)">Finalizar</button>
            }
          </div>
        </article>
      } @empty {
        <div class="empty-state">Nenhuma assistência encontrada.</div>
      }
    </section>
  `,
})
export class AssistenciasComponent implements OnInit {
  assistencias: Assistencia[] = [];
  form: Assistencia = this.novaAssistencia();
  assistenciaEditandoId: number | null = null;
  buscaNome = '';
  sessoesDatas = Array(7).fill('');
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

  carregar(): void {
    this.apiService.get<Assistencia[]>('/assistencias').subscribe((dados) => (this.assistencias = dados));
  }

  buscarPorNome(): void {
    const nome = this.buscaNome.trim();

    if (!nome) {
      this.carregar();
      return;
    }

    this.apiService.get<Assistencia[]>(`/assistencias/buscar/nome?nome=${encodeURIComponent(nome)}`).subscribe((dados) => (this.assistencias = dados));
  }

  salvar(): void {
    const payload: Assistencia = {
      ...this.form,
      dataConsulta: this.form.dataConsulta || new Date().toISOString().split('T')[0],
      tratamentos: this.form.tratamentos || [],
      sessoes: this.montarSessoes(),
    };

    const requisicao = this.assistenciaEditandoId
      ? this.apiService.put<Assistencia>(`/assistencias/${this.assistenciaEditandoId}`, payload)
      : this.apiService.post<Assistencia>('/assistencias', payload);

    requisicao.subscribe(() => {
      this.limparFormulario();
      this.carregar();
    });
  }

  preencherFormulario(assistencia: Assistencia): void {
    this.assistenciaEditandoId = assistencia.id || null;
    this.form = {
      ...assistencia,
      tratamentos: [...(assistencia.tratamentos || [])],
      sessoes: [...(assistencia.sessoes || [])],
    };

    this.sessoesDatas = Array(7).fill('');
    (assistencia.sessoes || []).forEach((sessao) => {
      if (sessao.numeroSessao >= 1 && sessao.numeroSessao <= 7) {
        this.sessoesDatas[sessao.numeroSessao - 1] = sessao.dataSessao;
      }
    });

    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  deletar(id: number): void {
    if (!confirm('Deseja realmente deletar esta assistência?')) {
      return;
    }

    this.apiService.delete<void>(`/assistencias/${id}`).subscribe(() => this.carregar());
  }

  finalizar(id: number): void {
    if (!confirm('Deseja finalizar este tratamento?')) {
      return;
    }

    this.apiService.put<Assistencia>(`/assistencias/${id}/finalizar`).subscribe(() => this.carregar());
  }

  realizarSessao(idAssistencia: number, idSessao: number): void {
    this.apiService.put<SessaoTratamento>(`/assistencias/${idAssistencia}/sessoes/${idSessao}/realizar`).subscribe(() => this.carregar());
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
    this.form = this.novaAssistencia();
    this.sessoesDatas = Array(7).fill('');
  }

  formatDate = formatDate;

  formatarStatus(status: string): string {
    return {
      EM_ANDAMENTO: 'Em andamento',
      FINALIZADO: 'Finalizado',
      CANCELADO: 'Cancelado',
    }[status] || status;
  }

  private montarSessoes(): SessaoTratamento[] {
    return this.sessoesDatas
      .map((data, index) => ({ data, index }))
      .filter((item) => !!item.data)
      .map((item) => ({
        numeroSessao: item.index + 1,
        dataSessao: item.data,
        realizada: false,
        observacoes: '',
      }));
  }

  private novaAssistencia(): Assistencia {
    return {
      nome: '',
      entidadeConsulta: '',
      cidade: '',
      whatsapp: '',
      observacoes: '',
      status: 'EM_ANDAMENTO',
      tratamentos: [],
      sessoes: [],
    };
  }
}
