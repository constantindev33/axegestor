const botaoLogin = document.getElementById("btn-login");
const emailInput = document.getElementById("email");
const senhaInput = document.getElementById("senha");
const erro = document.getElementById("erro");

botaoLogin.addEventListener("click", login);

senhaInput.addEventListener("keydown", (event) => {
  if (event.key === "Enter") {
    login();
  }
});

async function login() {
  const email = emailInput.value.trim();
  const senha = senhaInput.value;

  erro.innerText = "";

  if (!email || !senha) {
    erro.innerText = "Informe e-mail e senha.";
    return;
  }

  botaoLogin.disabled = true;
  botaoLogin.innerText = "Entrando...";

  try {
    const response = await fetch("http://localhost:8080/auth/login", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ email, senha }),
    });

    if (!response.ok) {
      throw new Error("Login inválido");
    }

    const data = await response.json();

    localStorage.setItem("token", data.token);
    window.location.href = "dashboard.html";
  } catch (error) {
    erro.innerText = "E-mail ou senha inválidos.";
  } finally {
    botaoLogin.disabled = false;
    botaoLogin.innerText = "Entrar";
  }
}
