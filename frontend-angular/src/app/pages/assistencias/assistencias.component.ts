import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { ApiService } from '../../core/api.service';
import { Assistencia } from '../../core/models';
import { formatDate } from '../../shared/formatters';

@Component({
  selector: 'app-assistencias',
  imports: [FormsModule],
  template: `
    <header class="page-header">
      <div><p class="eyebrow">Tratamentos</p><h1>Assistências</h1></div>
      <button class="secondary-button" type="button" (click)="carregar()">Atualizar</button>
    </header>

    <section class="card form-card">
      <h2>Nova assistência</h2>
      <div class="form-grid">
        <label>Nome<input [(ngModel)]="form.nome" /></label>
        <label>Entidade<input [(ngModel)]="form.entidadeConsulta" /></label>
        <label>Cidade<input [(ngModel)]="form.cidade" /></label>
        <label>WhatsApp<input [(ngModel)]="form.whatsapp" /></label>
        <label>Status<select [(ngModel)]="form.status"><option value="EM_ANDAMENTO">Em andamento</option><option value="FINALIZADO">Finalizado</option><option value="CANCELADO">Cancelado</option></select></label>
      </div>
      <label>Observações<textarea [(ngModel)]="form.observacoes"></textarea></label>
      <div class="actions-row"><button type="button" (click)="salvar()">Salvar assistência</button></div>
    </section>

    <section class="cards list-cards">
      @for (assistencia of assistencias; track assistencia.id) {
        <article class="card">
          <span class="status-badge">{{ assistencia.status }}</span>
          <h3>{{ assistencia.nome }}</h3>
          <div class="info-list">
            <div><dt>Entidade</dt><dd>{{ assistencia.entidadeConsulta || '-' }}</dd></div>
            <div><dt>Cidade</dt><dd>{{ assistencia.cidade || '-' }}</dd></div>
            <div><dt>Data</dt><dd>{{ formatDate(assistencia.dataConsulta) }}</dd></div>
            <div><dt>Sessões</dt><dd>{{ assistencia.sessoes?.length || 0 }}</dd></div>
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

  constructor(private readonly apiService: ApiService) {}

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.apiService.get<Assistencia[]>('/assistencias').subscribe((dados) => (this.assistencias = dados));
  }

  salvar(): void {
    this.form.dataConsulta = new Date().toISOString().split('T')[0];
    this.apiService.post<Assistencia>('/assistencias', this.form).subscribe(() => {
      this.form = this.novaAssistencia();
      this.carregar();
    });
  }

  formatDate = formatDate;

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
