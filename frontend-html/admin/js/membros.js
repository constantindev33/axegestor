exigirLogin();
document.getElementById("logout").addEventListener("click", logout);

async function carregarMembros() {
  const lista = document.getElementById("listaMembros");
  try {
    const membros = await apiFetch("/membros");
    lista.innerHTML = membros.length
      ? membros.map(renderizarMembro).join("")
      : '<div class="empty-state">Nenhum membro encontrado.</div>';
  } catch (error) {
    lista.innerHTML = `<div class="empty-state">${error.message}</div>`;
  }
}

async function salvarMembro() {
  const dados = {
    nome: document.getElementById("nome").value.trim(),
    telefone: document.getElementById("telefone").value.trim(),
    email: document.getElementById("email").value.trim(),
    dataEntrada: document.getElementById("dataEntrada").value || null,
    funcao: document.getElementById("funcao").value,
    status: document.getElementById("status").value,
  };

  try {
    await apiFetch("/membros", { method: "POST", body: JSON.stringify(dados) });
    document.querySelectorAll("input").forEach((input) => (input.value = ""));
    carregarMembros();
  } catch (error) {
    alert(error.message);
  }
}

function renderizarMembro(membro) {
  return `
    <article class="card">
      <span class="status-badge">${membro.status}</span>
      <h3>${escapeHtml(membro.nome)}</h3>
      <div class="info-list">
        <div><dt>Função</dt><dd>${membro.funcao}</dd></div>
        <div><dt>Telefone</dt><dd>${escapeHtml(membro.telefone || "-")}</dd></div>
        <div><dt>E-mail</dt><dd>${escapeHtml(membro.email || "-")}</dd></div>
      </div>
    </article>
  `;
}

function escapeHtml(value) {
  return String(value || "").replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll('"', "&quot;").replaceAll("'", "&#039;");
}

carregarMembros();
