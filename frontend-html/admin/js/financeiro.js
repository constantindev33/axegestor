exigirLogin();
document.getElementById("logout").addEventListener("click", logout);

async function inicializarFinanceiro() {
  const hoje = new Date();
  document.getElementById("dataLancamento").value = hoje.toISOString().split("T")[0];
  document.getElementById("mesMensalidade").value = hoje.toISOString().slice(0, 7);

  await carregarMembrosParaSelect();
  await carregarFinanceiro();
  await carregarMensalidades();
}

async function carregarMembrosParaSelect() {
  const membros = await apiFetch("/membros");
  const select = document.getElementById("membroId");

  select.innerHTML = '<option value="">Sem membro vinculado</option>';
  select.innerHTML += membros
    .map((membro) => `<option value="${membro.id}">${escapeHtml(membro.nome)}</option>`)
    .join("");
}

async function carregarFinanceiro() {
  const lista = document.getElementById("listaFinanceiro");
  try {
    const lancamentos = await apiFetch("/financeiro");
    lista.innerHTML = lancamentos.length
      ? lancamentos.map(renderizarLancamento).join("")
      : '<div class="empty-state">Nenhum lançamento encontrado.</div>';
  } catch (error) {
    lista.innerHTML = `<div class="empty-state">${error.message}</div>`;
  }
}

async function carregarMensalidades() {
  const lista = document.getElementById("listaMensalidades");
  const mesSelecionado = document.getElementById("mesMensalidade").value;
  const [ano, mes] = mesSelecionado.split("-");

  try {
    const mensalidades = await apiFetch(`/financeiro/mensalidades/membros?ano=${ano}&mes=${Number(mes)}`);
    lista.innerHTML = mensalidades.length
      ? mensalidades.map(renderizarMensalidade).join("")
      : '<div class="empty-state">Nenhum membro cadastrado.</div>';
  } catch (error) {
    lista.innerHTML = `<div class="empty-state">${error.message}</div>`;
  }
}

async function salvarLancamento() {
  const membroId = document.getElementById("membroId").value;
  const status = document.getElementById("status").value;

  const dados = {
    descricao: document.getElementById("descricao").value.trim(),
    responsavel: document.getElementById("responsavel").value.trim(),
    valor: Number(document.getElementById("valor").value),
    dataLancamento: document.getElementById("dataLancamento").value,
    dataVencimento: document.getElementById("dataVencimento").value || null,
    dataPagamento: status === "PAGO" ? new Date().toISOString().split("T")[0] : null,
    tipo: document.getElementById("tipo").value,
    categoria: document.getElementById("categoria").value,
    status,
    membro: membroId ? { id: Number(membroId) } : null,
  };

  try {
    await apiFetch("/financeiro", { method: "POST", body: JSON.stringify(dados) });
    limparFormularioFinanceiro();
    await carregarFinanceiro();
    await carregarMensalidades();
  } catch (error) {
    alert(error.message);
  }
}

function limparFormularioFinanceiro() {
  document.getElementById("descricao").value = "";
  document.getElementById("responsavel").value = "";
  document.getElementById("valor").value = "";
  document.getElementById("dataVencimento").value = "";
  document.getElementById("membroId").value = "";
  document.getElementById("tipo").value = "RECEITA";
  document.getElementById("categoria").value = "MENSALIDADE";
  document.getElementById("status").value = "PENDENTE";
}

function renderizarMensalidade(item) {
  const classe = item.statusMensalidade === "EM_DIA" ? "status-ok" : item.statusMensalidade === "ATRASADA" ? "status-danger" : "status-warn";

  return `
    <article class="card">
      <span class="status-badge ${classe}">${formatarStatusMensalidade(item.statusMensalidade)}</span>
      <h3>${escapeHtml(item.nome)}</h3>
      <div class="info-list">
        <div><dt>Status do membro</dt><dd>${escapeHtml(item.statusMembro)}</dd></div>
        <div><dt>Valor</dt><dd>${formatarMoeda(item.valor)}</dd></div>
        <div><dt>Vencimento</dt><dd>${formatarData(item.dataVencimento)}</dd></div>
        <div><dt>Pagamento</dt><dd>${formatarData(item.dataPagamento)}</dd></div>
      </div>
      ${item.lancamentoId && item.statusPagamento !== "PAGO" ? `<div class="actions-row"><button onclick="marcarComoPago(${item.lancamentoId})">Marcar como pago</button></div>` : ""}
    </article>
  `;
}

function renderizarLancamento(item) {
  return `
    <article class="card">
      <span class="status-badge">${item.status}</span>
      <h3>${escapeHtml(item.descricao)}</h3>
      <p>${formatarMoeda(item.valor)}</p>
      <div class="info-list">
        <div><dt>Tipo</dt><dd>${item.tipo}</dd></div>
        <div><dt>Categoria</dt><dd>${item.categoria}</dd></div>
        <div><dt>Data</dt><dd>${formatarData(item.dataLancamento)}</dd></div>
        <div><dt>Vencimento</dt><dd>${formatarData(item.dataVencimento)}</dd></div>
      </div>
      ${item.status !== "PAGO" ? `<div class="actions-row"><button onclick="marcarComoPago(${item.id})">Marcar como pago</button></div>` : ""}
    </article>
  `;
}

async function marcarComoPago(id) {
  await apiFetch(`/financeiro/${id}/pagar`, { method: "PUT" });
  await carregarFinanceiro();
  await carregarMensalidades();
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

inicializarFinanceiro();
