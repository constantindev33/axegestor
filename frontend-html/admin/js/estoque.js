exigirLogin();
document.getElementById("logout").addEventListener("click", logout);

async function carregarMateriais() {
  const lista = document.getElementById("listaMateriais");
  try {
    const materiais = await apiFetch("/almoxarifado/materiais");
    lista.innerHTML = materiais.length
      ? materiais.map(renderizarMaterial).join("")
      : '<div class="empty-state">Nenhum material encontrado.</div>';
  } catch (error) {
    lista.innerHTML = `<div class="empty-state">${error.message}</div>`;
  }
}

async function salvarMaterial() {
  const dados = {
    nome: document.getElementById("nome").value.trim(),
    categoria: document.getElementById("categoria").value,
    quantidadeAtual: Number(document.getElementById("quantidadeAtual").value),
    quantidadeMinima: Number(document.getElementById("quantidadeMinima").value),
    unidadeMedida: document.getElementById("unidadeMedida").value.trim(),
    localArmazenamento: document.getElementById("localArmazenamento").value.trim(),
  };

  try {
    await apiFetch("/almoxarifado/materiais", { method: "POST", body: JSON.stringify(dados) });
    document.querySelectorAll("input").forEach((input) => (input.value = ""));
    carregarMateriais();
  } catch (error) {
    alert(error.message);
  }
}

function renderizarMaterial(material) {
  const baixo = material.quantidadeAtual <= material.quantidadeMinima;
  return `
    <article class="card">
      <span class="status-badge">${baixo ? "Estoque baixo" : material.categoria}</span>
      <h3>${escapeHtml(material.nome)}</h3>
      <p>${material.quantidadeAtual} ${escapeHtml(material.unidadeMedida || "")}</p>
      <div class="info-list">
        <div><dt>Mínimo</dt><dd>${material.quantidadeMinima}</dd></div>
        <div><dt>Local</dt><dd>${escapeHtml(material.localArmazenamento || "-")}</dd></div>
      </div>
    </article>
  `;
}

function escapeHtml(value) {
  return String(value || "").replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll('"', "&quot;").replaceAll("'", "&#039;");
}

carregarMateriais();
