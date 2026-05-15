exigirLogin();

document.getElementById("logout").addEventListener("click", logout);

let assistenciaEditandoId = null;

async function carregarAssistencias() {
  try {
    const assistencias = await apiFetch("/assistencias");
    renderizarAssistencias(assistencias);
  } catch (error) {
    alert(error.message);
  }
}

function renderizarAssistencias(assistencias) {
  const lista = document.getElementById("listaAssistencias");
  lista.innerHTML = "";

  if (!assistencias.length) {
    lista.innerHTML = '<div class="empty-state">Nenhuma assistência encontrada.</div>';
    return;
  }

  assistencias.forEach((assistencia) => {
    const card = document.createElement("article");
    card.className = "card assistance-card";

    card.innerHTML = `
      <div class="card-header">
        <div>
          <span class="status-badge">${formatarStatus(assistencia.status)}</span>
          <h3>${escapeHtml(assistencia.nome)}</h3>
        </div>
      </div>

      <dl class="info-list">
        <div><dt>Entidade</dt><dd>${escapeHtml(assistencia.entidadeConsulta || "-")}</dd></div>
        <div><dt>Cidade</dt><dd>${escapeHtml(assistencia.cidade || "-")}</dd></div>
        <div><dt>WhatsApp</dt><dd>${escapeHtml(assistencia.whatsapp || "-")}</dd></div>
        <div><dt>Tratamentos</dt><dd>${escapeHtml(assistencia.tratamentos?.join(", ") || "-")}</dd></div>
      </dl>

      <div class="sessions-list">
        <strong>Sessões</strong>
        ${renderizarSessoes(assistencia)}
      </div>

      <div class="actions-row">
        <button class="secondary-button" onclick="preencherFormulario(${assistencia.id})">Editar</button>
        <button class="secondary-button" onclick="deletarAssistencia(${assistencia.id})">Deletar</button>
        ${
          assistencia.status !== "FINALIZADO"
            ? `<button onclick="finalizarAssistencia(${assistencia.id})">Finalizar</button>`
            : ""
        }
      </div>
    `;

    lista.appendChild(card);
  });
}

function renderizarSessoes(assistencia) {
  if (!assistencia.sessoes?.length) {
    return "<p>Nenhuma sessão cadastrada.</p>";
  }

  return assistencia.sessoes
    .map(
      (sessao) => `
        <p>
          Sessão ${sessao.numeroSessao} - ${formatarData(sessao.dataSessao)} -
          ${sessao.realizada ? "Realizada" : "Pendente"}
          ${
            !sessao.realizada
              ? `<button class="inline-button" onclick="realizarSessao(${assistencia.id}, ${sessao.id})">Marcar realizada</button>`
              : ""
          }
        </p>
      `,
    )
    .join("");
}

async function cadastrarAssistencia() {
  const dados = montarDadosFormulario();

  try {
    if (assistenciaEditandoId) {
      await apiFetch(`/assistencias/${assistenciaEditandoId}`, {
        method: "PUT",
        body: JSON.stringify(dados),
      });

      assistenciaEditandoId = null;
    } else {
      await apiFetch("/assistencias", {
        method: "POST",
        body: JSON.stringify(dados),
      });
    }

    limparFormulario();
    carregarAssistencias();
  } catch (error) {
    alert(error.message);
  }
}

function montarDadosFormulario() {
  const tratamentos = Array.from(
    document.querySelectorAll(".tratamentos input:checked"),
  ).map((tratamento) => tratamento.value);

  const sessoes = [];

  for (let i = 1; i <= 7; i++) {
    const data = document.getElementById(`sessao${i}`).value;

    if (data) {
      sessoes.push({
        numeroSessao: i,
        dataSessao: data,
        realizada: false,
        observacoes: "",
      });
    }
  }

  return {
    nome: document.getElementById("nome").value.trim(),
    entidadeConsulta: document.getElementById("entidade").value.trim(),
    cidade: document.getElementById("cidade").value.trim(),
    whatsapp: document.getElementById("whatsapp").value.trim(),
    observacoes: document.getElementById("observacoes").value.trim(),
    dataConsulta: new Date().toISOString().split("T")[0],
    status: document.getElementById("status").value,
    tratamentos,
    sessoes,
  };
}

function limparFormulario() {
  assistenciaEditandoId = null;

  document.getElementById("nome").value = "";
  document.getElementById("entidade").value = "";
  document.getElementById("cidade").value = "";
  document.getElementById("whatsapp").value = "";
  document.getElementById("observacoes").value = "";
  document.getElementById("status").value = "EM_ANDAMENTO";

  document
    .querySelectorAll(".tratamentos input")
    .forEach((checkbox) => (checkbox.checked = false));

  for (let i = 1; i <= 7; i++) {
    document.getElementById(`sessao${i}`).value = "";
  }
}

async function preencherFormulario(id) {
  try {
    const assistencia = await apiFetch(`/assistencias/${id}`);

    assistenciaEditandoId = id;
    document.getElementById("nome").value = assistencia.nome || "";
    document.getElementById("entidade").value = assistencia.entidadeConsulta || "";
    document.getElementById("cidade").value = assistencia.cidade || "";
    document.getElementById("whatsapp").value = assistencia.whatsapp || "";
    document.getElementById("observacoes").value = assistencia.observacoes || "";
    document.getElementById("status").value = assistencia.status;

    document
      .querySelectorAll(".tratamentos input")
      .forEach((checkbox) => {
        checkbox.checked = assistencia.tratamentos?.includes(checkbox.value) || false;
      });

    window.scrollTo({ top: 0, behavior: "smooth" });
  } catch (error) {
    alert(error.message);
  }
}

async function deletarAssistencia(id) {
  if (!confirm("Deseja realmente deletar esta assistência?")) {
    return;
  }

  await apiFetch(`/assistencias/${id}`, { method: "DELETE" });
  carregarAssistencias();
}

async function finalizarAssistencia(id) {
  if (!confirm("Deseja finalizar este tratamento?")) {
    return;
  }

  await apiFetch(`/assistencias/${id}/finalizar`, { method: "PUT" });
  carregarAssistencias();
}

async function realizarSessao(idAssistencia, idSessao) {
  await apiFetch(`/assistencias/${idAssistencia}/sessoes/${idSessao}/realizar`, {
    method: "PUT",
  });

  carregarAssistencias();
}

async function buscarPorNome() {
  const nome = document.getElementById("buscaNome").value.trim();

  if (!nome) {
    carregarAssistencias();
    return;
  }

  const assistencias = await apiFetch(
    `/assistencias/buscar/nome?nome=${encodeURIComponent(nome)}`,
  );

  renderizarAssistencias(assistencias);
}

function formatarStatus(status) {
  const statusFormatados = {
    EM_ANDAMENTO: "Em andamento",
    FINALIZADO: "Finalizado",
    CANCELADO: "Cancelado",
  };

  return statusFormatados[status] || status;
}

function formatarData(data) {
  if (!data) {
    return "-";
  }

  return new Date(`${data}T00:00:00`).toLocaleDateString("pt-BR");
}

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

carregarAssistencias();
