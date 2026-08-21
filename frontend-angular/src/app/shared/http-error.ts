import { HttpErrorResponse } from '@angular/common/http';

export function friendlyHttpError(error: unknown, fallback = 'Erro interno inesperado.'): string {
  if (!(error instanceof HttpErrorResponse)) {
    return fallback;
  }

  if (error.status === 0) {
    return 'Não consegui conectar com o backend. Verifique se a API está ligada.';
  }

  if (error.status === 400 && error.error?.message) {
    return error.error.message;
  }

  if (error.status === 401) {
    return 'Sua sessão expirou. Entre novamente.';
  }

  if (error.status === 403) {
    return 'Seu perfil não tem permissão para acessar esta ação.';
  }

  if (error.status === 404) {
    return 'Registro não encontrado.';
  }

  if (error.status >= 500) {
    return 'Erro no servidor. Tente novamente em alguns instantes.';
  }

  return fallback;
}
