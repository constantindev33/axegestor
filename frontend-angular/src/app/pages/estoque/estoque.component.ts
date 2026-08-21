import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';

import { ApiService } from '../../core/api.service';
import { Material, MovimentacaoEstoque } from '../../core/models';
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
      <div class="section-title">
        <h2>Novo material</h2>
        <span class="muted-inline">Cadastro base</span>
      </div>
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
      <label>Observações<textarea [(ngModel)]="form.observacoes" placeholder="Observações úteis para o almoxarifado"></textarea></label>
      <div class="actions-row"><button type="button" (click)="salvar()" [disabled]="salvando">{{ salvando ? 'Salvando...' : 'Salvar material' }}</button></div>
    </section>

    <section class="card form-card">
      <div class="section-title">
        <h2>Entrada e saída de material</h2>
        <span class="muted-inline">Movimentação semanal</span>
      </div>

      <div class="form-grid">
        <label>Material
          <select [(ngModel)]="movimentacao.materialId">
            <option [ngValue]="null">Selecione o material</option>
            @for (material of materiais; track material.id) {
              <option [ngValue]="material.id">{{ material.nome }} - {{ material.quantidadeAtual }} {{ material.unidadeMedida }}</option>
            }
          </select>
        </label>
        <label>Tipo
          <select [(ngModel)]="movimentacao.tipo">
            <option value="ENTRADA">Entrada</option>
            <option value="SAIDA">Saída</option>
          </select>
        </label>
        <label>Quantidade<input type="number" min="1" [(ngModel)]="movimentacao.quantidade" /></label>
        <label>Responsável<input [(ngModel)]="movimentacao.responsavel" placeholder="Quem movimentou" /></label>
        <label>Motivo<input [(ngModel)]="movimentacao.motivo" placeholder="Compra, uso no trabalho, reposição..." /></label>
      </div>
      <label>Observações<textarea [(ngModel)]="movimentacao.observacoes" placeholder="Detalhes da movimentação"></textarea></label>

      <div class="actions-row">
        <button type="button" (click)="registrarMovimentacao()" [disabled]="salvando">{{ salvando ? 'Registrando...' : 'Registrar movimentação' }}</button>
      </div>
    </section>

    <section class="card search-card">
      <div class="section-title">
        <h2>Filtrar estoque</h2>
        <button type="button" class="secondary-button" (click)="exportarEstoque()">Exportar CSV</button>
      </div>
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

    <section class="alerts-panel">
      @if (materiaisEstoqueBaixo.length > 0) {
        <div class="alert-card warn">{{ materiaisEstoqueBaixo.length }} material(is) abaixo ou no mínimo.</div>
      } @else {
        <div class="alert-card ok">Nenhum material em estoque baixo.</div>
      }
      <div class="alert-card ok">{{ materiais.length }} material(is) cadastrado(s).</div>
    </section>

    <section class="card list-cards">
      <div class="list-toolbar">
        <h2>Planilha do almoxarifado</h2>
        <span class="muted-inline">{{ materiaisFiltrados.length }} resultado(s)</span>
      </div>

      <div class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th>Material</th>
              <th>Categoria</th>
              <th>Atual</th>
              <th>Mínimo</th>
              <th>Unidade</th>
              <th>Local</th>
              <th>Situação</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            @for (material of materiaisPaginados; track material.id) {
              <tr>
                <td><strong>{{ material.nome }}</strong></td>
                <td>{{ material.categoria }}</td>
                <td>{{ material.quantidadeAtual }}</td>
                <td>{{ material.quantidadeMinima }}</td>
                <td>{{ material.unidadeMedida }}</td>
                <td>{{ material.localArmazenamento || '-' }}</td>
                <td>
                  <span class="status-badge" [class.status-danger]="material.quantidadeAtual <= material.quantidadeMinima">
                    {{ material.quantidadeAtual <= material.quantidadeMinima ? 'Estoque baixo' : 'Ok' }}
                  </span>
                </td>
                <td>
                  @if (material.id) {
                    <button class="secondary-button" type="button" (click)="selecionarMaterial(material)">Movimentar</button>
                  }
                </td>
              </tr>
            } @empty {
              <tr><td colspan="8">Nenhum material encontrado.</td></tr>
            }
          </tbody>
        </table>
      </div>
    </section>

    <section class="card list-cards">
      <div class="section-title">
        <h2>Histórico do material</h2>
        <span class="muted-inline">{{ movimentacoes.length }} movimentação(ões)</span>
      </div>

      @if (!materialHistoricoId) {
        <div class="empty-state">Selecione um material na tabela para ver o histórico.</div>
      } @else {
        <div class="table-wrapper">
          <table class="data-table compact-table">
            <thead><tr><th>Data</th><th>Tipo</th><th>Qtd.</th><th>Responsável</th><th>Motivo</th></tr></thead>
            <tbody>
              @for (item of movimentacoes; track item.id) {
                <tr>
                  <td>{{ item.dataMovimentacao || '-' }}</td>
                  <td>{{ item.tipo }}</td>
                  <td>{{ item.quantidade }}</td>
                  <td>{{ item.responsavel }}</td>
                  <td>{{ item.motivo || '-' }}</td>
                </tr>
              } @empty {
                <tr><td colspan="5">Nenhuma movimentação registrada para este material.</td></tr>
              }
            </tbody>
          </table>
        </div>
      }
    </section>

    <section class="cards list-cards">
      @for (material of materiaisEstoqueBaixo; track material.id) {
        <article class="card">
          <span class="status-badge status-danger">Estoque baixo</span>
          <h3>{{ material.nome }}</h3>
          <p>{{ material.quantidadeAtual }} {{ material.unidadeMedida }}</p>
          <div class="info-list">
            <div><dt>Mínimo</dt><dd>{{ material.quantidadeMinima }}</dd></div>
            <div><dt>Local</dt><dd>{{ material.localArmazenamento || '-' }}</dd></div>
          </div>
        </article>
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
  movimentacoes: MovimentacaoEstoque[] = [];
  categorias = ['VELA', 'ERVA', 'BEBIDA', 'DEFUMACAO', 'PEMBA', 'FUNDANGA', 'BANHO', 'COMIDA_RITUALISTICA', 'LIMPEZA', 'COZINHA', 'PAPELARIA', 'ROUPA', 'FERRAMENTA', 'OUTROS'];
  form: Material = this.novoMaterial();
  movimentacao = this.novaMovimentacao();
  materialHistoricoId: number | null = null;
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

  registrarMovimentacao(): void {
    this.limparMensagens();

    if (!this.movimentacao.materialId) {
      this.mensagemErro = 'Selecione o material movimentado.';
      return;
    }

    if (!this.movimentacao.quantidade || this.movimentacao.quantidade < 1) {
      this.mensagemErro = 'Informe uma quantidade maior que zero.';
      return;
    }

    if (!this.movimentacao.responsavel?.trim()) {
      this.mensagemErro = 'Informe quem é responsável pela movimentação.';
      return;
    }

    const payload = {
      material: { id: this.movimentacao.materialId },
      tipo: this.movimentacao.tipo,
      quantidade: this.movimentacao.quantidade,
      responsavel: this.movimentacao.responsavel,
      motivo: this.movimentacao.motivo,
      observacoes: this.movimentacao.observacoes,
    };

    this.salvando = true;
    this.apiService.post<MovimentacaoEstoque>('/almoxarifado/movimentacoes', payload)
      .pipe(finalize(() => (this.salvando = false)))
      .subscribe({
        next: () => {
          const materialId = this.movimentacao.materialId;
          this.movimentacao = this.novaMovimentacao();
          this.mensagemSucesso = 'Movimentação registrada com sucesso.';
          this.carregar(true);
          if (materialId) {
            this.carregarHistorico(materialId);
          }
        },
        error: (error) => (this.mensagemErro = friendlyHttpError(error, 'Não consegui registrar a movimentação.')),
      });
  }

  selecionarMaterial(material: Material): void {
    this.movimentacao.materialId = material.id || null;
    this.carregarHistorico(material.id || null);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  carregarHistorico(materialId: number | null): void {
    this.materialHistoricoId = materialId;
    this.movimentacoes = [];

    if (!materialId) {
      return;
    }

    this.apiService.get<MovimentacaoEstoque[]>(`/almoxarifado/movimentacoes/material/${materialId}`)
      .subscribe({
        next: (dados) => (this.movimentacoes = dados),
        error: (error) => (this.mensagemErro = friendlyHttpError(error, 'Não consegui carregar o histórico do material.')),
      });
  }

  exportarEstoque(): void {
    this.limparMensagens();

    if (!this.materiaisFiltrados.length) {
      this.mensagemErro = 'Não há materiais para exportar.';
      return;
    }

    const linhas = this.materiaisFiltrados.map((material) => ({
      nome: material.nome,
      categoria: material.categoria,
      quantidadeAtual: material.quantidadeAtual,
      quantidadeMinima: material.quantidadeMinima,
      unidadeMedida: material.unidadeMedida,
      localArmazenamento: material.localArmazenamento || '',
      situacao: material.quantidadeAtual <= material.quantidadeMinima ? 'Estoque baixo' : 'Ok',
      observacoes: material.observacoes || '',
    }));

    this.baixarCsv('estoque-almoxarifado.csv', linhas);
    this.mensagemSucesso = 'Planilha de estoque exportada com sucesso.';
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

  get materiaisEstoqueBaixo(): Material[] {
    return this.materiais.filter((material) => material.quantidadeAtual <= material.quantidadeMinima);
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
      observacoes: '',
    };
  }

  private novaMovimentacao(): {
    materialId: number | null;
    tipo: string;
    quantidade: number;
    responsavel: string;
    motivo: string;
    observacoes: string;
  } {
    return {
      materialId: null,
      tipo: 'ENTRADA',
      quantidade: 1,
      responsavel: '',
      motivo: '',
      observacoes: '',
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
}
