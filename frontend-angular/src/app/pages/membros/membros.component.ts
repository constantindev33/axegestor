import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { ApiService } from '../../core/api.service';
import { Membro } from '../../core/models';

@Component({
  selector: 'app-membros',
  imports: [FormsModule],
  template: `
    <header class="page-header">
      <div><p class="eyebrow">Casa</p><h1>Membros</h1></div>
      <button class="secondary-button" type="button" (click)="carregar()">Atualizar</button>
    </header>

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
      <div class="actions-row"><button type="button" (click)="salvar()">Salvar membro</button></div>
    </section>

    <section class="cards list-cards">
      @for (membro of membros; track membro.id) {
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
  `,
})
export class MembrosComponent implements OnInit {
  membros: Membro[] = [];
  form: Membro = this.novoMembro();

  constructor(private readonly apiService: ApiService) {}

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.apiService.get<Membro[]>('/membros').subscribe((dados) => (this.membros = dados));
  }

  salvar(): void {
    this.apiService.post<Membro>('/membros', this.form).subscribe(() => {
      this.form = this.novoMembro();
      this.carregar();
    });
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
