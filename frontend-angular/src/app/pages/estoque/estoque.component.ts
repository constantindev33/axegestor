import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';

import { ApiService } from '../../core/api.service';
import { Material } from '../../core/models';
import { friendlyHttpError } from '../../shared/http-error';
import { PageFeedbackComponent } from '../../shared/page-feedback.component';

@Component({
  selector: 'app-estoque',
  imports: [FormsModule, PageFeedbackComponent],
  template: `
    <header class="page-header">
      <div><p class="eyebrow">Almoxarifado</p><h1>Estoque</h1></div>
      <button class="secondary-button" type="button" (click)="carregar()">Atualizar</button>
    </header>

    <app-page-feedback
      [loading]="carregando"
      loadingText="Carregando estoque..."
      [success]="mensagemSucesso"
      [error]="mensagemErro"
    />

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
      <div class="actions-row"><button type="button" (click)="salvar()" [disabled]="salvando">{{ salvando ? 'Salvando...' : 'Salvar material' }}</button></div>
    </section>

    <section class="card search-card">
      <h2>Filtrar estoque</h2>
      <div class="filter-grid">
        <label>Buscar<input [(ngModel)]="filtroBusca" (ngModelChange)="paginaAtual = 1" placeholder="Nome ou local" /></label>
        <label>Categoria
          <select [(ngModel)]="filtroCategoria" (ngModelChange)="paginaAtual = 1">
            <option value="">Todas</option>
            @for (categoria of categorias; track categoria) {
              <option [value]="categoria">{{ categoria }}</option>
            }
          </select>
        </label>
        <label>Situação
          <select [(ngModel)]="filtroSituacao" (ngModelChange)="paginaAtual = 1">
            <option value="">Todas</option>
            <option value="BAIXO">Estoque baixo</option>
            <option value="OK">Estoque ok</option>
          </select>
        </label>
      </div>
    </section>

    <section class="cards list-cards">
      <div class="list-toolbar">
        <h2>Materiais</h2>
        <span class="muted-inline">{{ materiaisFiltrados.length }} resultado(s)</span>
      </div>
      @for (material of materiaisPaginados; track material.id) {
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

    <nav class="pagination-bar" aria-label="Paginação de estoque">
      <button type="button" class="secondary-button" (click)="paginaAnterior()" [disabled]="paginaAtual === 1">Anterior</button>
      <span>Página {{ paginaAtual }} de {{ totalPaginas }}</span>
      <button type="button" class="secondary-button" (click)="proximaPagina()" [disabled]="paginaAtual === totalPaginas">Próxima</button>
    </nav>
  `,
})
export class EstoqueComponent implements OnInit {
  materiais: Material[] = [];
  categorias = ['VELA', 'ERVA', 'BEBIDA', 'DEFUMACAO', 'PEMBA', 'FUNDANGA', 'BANHO', 'COMIDA_RITUALISTICA', 'LIMPEZA', 'COZINHA', 'PAPELARIA', 'ROUPA', 'FERRAMENTA', 'OUTROS'];
  form: Material = this.novoMaterial();
  filtroBusca = '';
  filtroCategoria = '';
  filtroSituacao = '';
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
    this.carregando = true;

    this.apiService.get<Material[]>('/almoxarifado/materiais')
      .pipe(finalize(() => (this.carregando = false)))
      .subscribe({
        next: (dados) => {
          this.materiais = dados;
          this.paginaAtual = 1;
        },
        error: (error) => (this.mensagemErro = friendlyHttpError(error, 'Não consegui carregar o estoque.')),
      });
  }

  salvar(): void {
    this.limparMensagens();
    this.salvando = true;

    this.apiService.post<Material>('/almoxarifado/materiais', this.form)
      .pipe(finalize(() => (this.salvando = false)))
      .subscribe({
        next: () => {
          this.form = this.novoMaterial();
          this.mensagemSucesso = 'Material salvo com sucesso.';
          this.carregar(true);
        },
        error: (error) => (this.mensagemErro = friendlyHttpError(error, 'Não consegui salvar o material.')),
      });
  }

  get materiaisFiltrados(): Material[] {
    const busca = this.normalizar(this.filtroBusca);

    return this.materiais.filter((material) => {
      const texto = this.normalizar(`${material.nome} ${material.localArmazenamento || ''}`);
      const estoqueBaixo = material.quantidadeAtual <= material.quantidadeMinima;
      const bateBusca = !busca || texto.includes(busca);
      const bateCategoria = !this.filtroCategoria || material.categoria === this.filtroCategoria;
      const bateSituacao = !this.filtroSituacao || (this.filtroSituacao === 'BAIXO' ? estoqueBaixo : !estoqueBaixo);

      return bateBusca && bateCategoria && bateSituacao;
    });
  }

  get materiaisPaginados(): Material[] {
    const inicio = (this.paginaAtual - 1) * this.itensPorPagina;
    return this.materiaisFiltrados.slice(inicio, inicio + this.itensPorPagina);
  }

  get totalPaginas(): number {
    return Math.max(Math.ceil(this.materiaisFiltrados.length / this.itensPorPagina), 1);
  }

  paginaAnterior(): void {
    this.paginaAtual = Math.max(this.paginaAtual - 1, 1);
  }

  proximaPagina(): void {
    this.paginaAtual = Math.min(this.paginaAtual + 1, this.totalPaginas);
  }

  private normalizar(valor: string): string {
    return valor.toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '');
  }

  private limparMensagens(): void {
    this.mensagemErro = '';
    this.mensagemSucesso = '';
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
