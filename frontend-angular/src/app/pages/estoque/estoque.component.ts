import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { ApiService } from '../../core/api.service';
import { Material } from '../../core/models';

@Component({
  selector: 'app-estoque',
  imports: [FormsModule],
  template: `
    <header class="page-header">
      <div><p class="eyebrow">Almoxarifado</p><h1>Estoque</h1></div>
      <button class="secondary-button" type="button" (click)="carregar()">Atualizar</button>
    </header>

    <section class="card form-card">
      <h2>Novo material</h2>
      <div class="form-grid">
        <label>Nome<input [(ngModel)]="form.nome" /></label>
        <label>Categoria
          <select [(ngModel)]="form.categoria">
            @for (categoria of categorias; track categoria) {
              <option [value]="categoria">{{ categoria }}</option>
            }
          </select>
        </label>
        <label>Quantidade atual<input type="number" [(ngModel)]="form.quantidadeAtual" /></label>
        <label>Quantidade mínima<input type="number" [(ngModel)]="form.quantidadeMinima" /></label>
        <label>Unidade<input [(ngModel)]="form.unidadeMedida" /></label>
        <label>Local<input [(ngModel)]="form.localArmazenamento" /></label>
      </div>
      <div class="actions-row"><button type="button" (click)="salvar()">Salvar material</button></div>
    </section>

    <section class="cards list-cards">
      @for (material of materiais; track material.id) {
        <article class="card">
          <span class="status-badge" [class.status-danger]="material.quantidadeAtual <= material.quantidadeMinima">
            {{ material.quantidadeAtual <= material.quantidadeMinima ? 'Estoque baixo' : material.categoria }}
          </span>
          <h3>{{ material.nome }}</h3>
          <p>{{ material.quantidadeAtual }} {{ material.unidadeMedida }}</p>
          <div class="info-list">
            <div><dt>Mínimo</dt><dd>{{ material.quantidadeMinima }}</dd></div>
            <div><dt>Local</dt><dd>{{ material.localArmazenamento || '-' }}</dd></div>
          </div>
        </article>
      } @empty {
        <div class="empty-state">Nenhum material encontrado.</div>
      }
    </section>
  `,
})
export class EstoqueComponent implements OnInit {
  materiais: Material[] = [];
  categorias = ['VELA', 'ERVA', 'BEBIDA', 'DEFUMACAO', 'PEMBA', 'FUNDANGA', 'BANHO', 'COMIDA_RITUALISTICA', 'LIMPEZA', 'COZINHA', 'PAPELARIA', 'ROUPA', 'FERRAMENTA', 'OUTROS'];
  form: Material = this.novoMaterial();

  constructor(private readonly apiService: ApiService) {}

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.apiService.get<Material[]>('/almoxarifado/materiais').subscribe((dados) => (this.materiais = dados));
  }

  salvar(): void {
    this.apiService.post<Material>('/almoxarifado/materiais', this.form).subscribe(() => {
      this.form = this.novoMaterial();
      this.carregar();
    });
  }

  private novoMaterial(): Material {
    return {
      nome: '',
      categoria: 'VELA',
      quantidadeAtual: 0,
      quantidadeMinima: 1,
      unidadeMedida: 'un',
      localArmazenamento: '',
    };
  }
}
