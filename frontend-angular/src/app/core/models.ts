export interface LoginResponse {
  token: string;
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
