export interface LoginResponse {
  token: string;
}

export interface Usuario {
  id?: number;
  nome: string;
  email: string;
  senha?: string;
  perfil: string;
  ativo: boolean;
}

export interface Auditoria {
  id?: number;
  criadoEm: string;
  usuarioEmail?: string;
  usuarioNome?: string;
  modulo: string;
  acao: string;
  entidade: string;
  entidadeId?: number;
  descricao: string;
}

export interface ResumoPorChave {
  chave: string;
  quantidade: number;
  total: number;
}

export interface RelatorioFinanceiroMensal {
  ano: number;
  mes: number;
  receitas: number;
  despesas: number;
  saldo: number;
  totalLancamentos: number;
  totalPagos: number;
  totalPendentes: number;
  totalAtrasados: number;
  mensalidadesEmDia: number;
  mensalidadesAtrasadas: number;
  mensalidadesSemCadastro: number;
  porCategoria: ResumoPorChave[];
  porStatus: ResumoPorChave[];
  mensalidades: ResumoMensalidade[];
  lancamentos: LancamentoFinanceiro[];
}

export interface Membro {
  id?: number;
  nome: string;
  telefone?: string;
  email?: string;
  dataEntrada?: string | null;
  funcao: string;
  status: string;
}

export interface Assistencia {
  id?: number;
  nome: string;
  entidadeConsulta?: string;
  dataConsulta?: string;
  cidade?: string;
  whatsapp?: string;
  observacoes?: string;
  status: string;
  tratamentos?: string[];
  sessoes?: SessaoTratamento[];
}

export interface SessaoTratamento {
  id?: number;
  numeroSessao: number;
  dataSessao: string;
  realizada: boolean;
  observacoes?: string;
}

export interface LancamentoFinanceiro {
  id?: number;
  descricao: string;
  responsavel?: string;
  valor: number;
  dataLancamento: string;
  dataVencimento?: string | null;
  dataPagamento?: string | null;
  tipo: string;
  categoria: string;
  status: string;
  membro?: { id: number } | null;
}

export interface ResumoMensalidade {
  membroId: number;
  nome: string;
  telefone?: string;
  email?: string;
  statusMembro: string;
  statusMensalidade: string;
  lancamentoId?: number;
  valor?: number;
  dataVencimento?: string;
  dataPagamento?: string;
  statusPagamento?: string;
}

export interface Material {
  id?: number;
  nome: string;
  categoria: string;
  quantidadeAtual: number;
  quantidadeMinima: number;
  unidadeMedida: string;
  localArmazenamento?: string;
}
