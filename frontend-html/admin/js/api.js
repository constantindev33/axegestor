const API_URL = "http://localhost:8080";

function getToken() {
  return localStorage.getItem("token");
}

function exigirLogin() {
  if (!getToken()) {
    window.location.href = "login.html";
  }
}

function logout() {
  localStorage.removeItem("token");
  window.location.href = "login.html";
}

async function apiFetch(path, options = {}) {
  const token = getToken();

  const response = await fetch(`${API_URL}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(options.headers || {}),
    },
  });

  if (response.status === 401 || response.status === 403) {
    logout();
    throw new Error("Sessão expirada. Faça login novamente.");
  }

  if (!response.ok) {
    const error = await response.json().catch(() => null);
    throw new Error(error?.message || "Não foi possível concluir a operação.");
  }

  if (response.status === 204) {
    return null;
  }

  return response.json();
}
