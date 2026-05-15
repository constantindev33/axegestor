exigirLogin();

document.getElementById("logout").addEventListener("click", logout);

async function carregarDashboard() {
  try {
    const hoje = new Date();
    const ano = hoje.getFullYear();
    const mes = hoje.getMonth() + 1;

    const [assistencias, financeiro, estoqueBaixo, membros, mensalidades] = await Promise.all([
      apiFetch("/assistencias"),
      apiFetch("/financeiro"),
      apiFetch("/almoxarifado/materiais/estoque-baixo"),
      apiFetch("/membros"),
      apiFetch(`/financeiro/mensalidades/membros?ano=${ano}&mes=${mes}`),
    ]);

    preencherMetricas({ assistencias, financeiro, estoqueBaixo, membros, mensalidades });
    renderizarAlertas({ estoqueBaixo, mensalidades });
    renderizarMensalidadesCriticas(mensalidades);
    renderizarAssistenciasRecentes(assistencias);
    renderizarEstoqueBaixo(estoqueBaixo);
  } catch (error) {
    document.getElementById("painelAlertas").innerHTML =
      `<div class="alert-card danger">Não foi possível carregar a dashboard: ${escapeHtml(error.message)}</div>`;
  }
}

function preencherMetricas({ assistencias, financeiro, estoqueBaixo, membros, mensalidades }) {
  const totalReceitas = somarPorTipo(financeiro, "RECEITA");
  const totalDespesas = somarPorTipo(financeiro, "DESPESA");
  const saldo = totalReceitas - totalDespesas;
  const assistenciasAndamento = assistencias.filter((item) => item.status === "EM_ANDAMENTO").length;
  const membrosAtivos = membros.filter((item) => item.status === "ATIVO").length;
  const atrasadas = mensalidades.filter((item) => item.statusMensalidade === "ATRASADA").length;

  document.getElementById("totalFinanceiro").innerText = formatarMoeda(saldo);
  document.getElementById("resumoFinanceiro").innerText = `${formatarMoeda(totalReceitas)} em receitas | ${formatarMoeda(totalDespesas)} em despesas`;
  document.getElementById("mensalidadesAtrasadas").innerText = atrasadas;
  document.getElementById("mensalidadesResumo").innerText = `${mensalidades.length} membros acompanhados`;
  document.getElementById("assistenciasAndamento").innerText = assistenciasAndamento;
  document.getElementById("totalAssistencias").innerText = `${assistencias.length} assistências no total`;
  document.getElementById("estoqueBaixo").innerText = estoqueBaixo.length;
  document.getElementById("membrosAtivos").innerText = membrosAtivos;
  document.getElementById("totalMembros").innerText = `${membros.length} membros no total`;
}

function renderizarAlertas({ estoqueBaixo, mensalidades }) {
  const alertas = [];
  const atrasadas = mensalidades.filter((item) => item.statusMensalidade === "ATRASADA");
  const semMensalidade = mensalidades.filter((item) => item.statusMensalidade === "SEM_MENSALIDADE");

  if (atrasadas.length) {
    alertas.push(`<div class="alert-card danger">${atrasadas.length} mensalidade(s) atrasada(s)</div>`);
  }

  if (semMensalidade.length) {
    alertas.push(`<div class="alert-card warn">${semMensalidade.length} membro(s) sem mensalidade no mês</div>`);
  }

  if (estoqueBaixo.length) {
    alertas.push(`<div class="alert-card warn">${estoqueBaixo.length} material(is) com estoque baixo</div>`);
  }

  document.getElementById("painelAlertas").innerHTML = alertas.length
    ? alertas.join("")
    : '<div class="alert-card ok">Tudo certo nos principais alertas do mês.</div>';
}

function renderizarMensalidadesCriticas(mensalidades) {
  const criticas = mensalidades
    .filter((item) => ["ATRASADA", "PENDENTE", "SEM_MENSALIDADE"].includes(item.statusMensalidade))
    .slice(0, 6);

  document.getElementById("listaMensalidadesCriticas").innerHTML = criticas.length
    ? criticas.map((item) => `
        <div class="compact-item">
          <div>
            <strong>${escapeHtml(item.nome)}</strong>
            <span>${formatarStatusMensalidade(item.statusMensalidade)} | ${formatarMoeda(item.valor)}</span>
          </div>
          <small>${formatarData(item.dataVencimento)}</small>
        </div>
      `).join("")
    : '<div class="empty-state">Nenhuma mensalidade crítica.</div>';
}

function renderizarAssistenciasRecentes(assistencias) {
  const recentes = [...assistencias]
    .sort((a, b) => String(b.dataConsulta || "").localeCompare(String(a.dataConsulta || "")))
    .slice(0, 6);

  document.getElementById("listaAssistenciasRecentes").innerHTML = recentes.length
    ? recentes.map((item) => `
        <div class="compact-item">
          <div>
            <strong>${escapeHtml(item.nome)}</strong>
            <span>${formatarStatusAssistencia(item.status)} | ${escapeHtml(item.cidade || "-")}</span>
          </div>
          <small>${formatarData(item.dataConsulta)}</small>
        </div>
      `).join("")
    : '<div class="empty-state">Nenhuma assistência cadastrada.</div>';
}

function renderizarEstoqueBaixo(materiais) {
  document.getElementById("listaEstoqueBaixo").innerHTML = materiais.length
    ? materiais.slice(0, 6).map((item) => `
        <div class="compact-item">
          <div>
            <strong>${escapeHtml(item.nome)}</strong>
            <span>${item.quantidadeAtual} ${escapeHtml(item.unidadeMedida || "")} disponíveis</span>
          </div>
          <small>Mín. ${item.quantidadeMinima}</small>
        </div>
      `).join("")
    : '<div class="empty-state">Nenhum material em estoque baixo.</div>';
}

function somarPorTipo(lancamentos, tipo) {
  return lancamentos
    .filter((item) => item.tipo === tipo)
    .reduce((total, item) => total + Number(item.valor || 0), 0);
}

function formatarStatusMensalidade(status) {
  const labels = {
    EM_DIA: "Em dia",
    ATRASADA: "Atrasada",
    PENDENTE: "Pendente",
    SEM_MENSALIDADE: "Sem mensalidade",
    CANCELADO: "Cancelada",
  };

  return labels[status] || status;
}

function formatarStatusAssistencia(status) {
  const labels = {
    EM_ANDAMENTO: "Em andamento",
    FINALIZADO: "Finalizada",
    CANCELADO: "Cancelada",
  };

  return labels[status] || status;
}

function formatarMoeda(valor) {
  if (valor === null || valor === undefined) {
    return "-";
  }

  return Number(valor).toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
}

function formatarData(data) {
  return data ? new Date(`${data}T00:00:00`).toLocaleDateString("pt-BR") : "-";
}

function escapeHtml(value) {
  return String(value || "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

carregarDashboard();
