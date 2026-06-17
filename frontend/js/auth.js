function showTab(tab) {
    document.getElementById("tab-login").classList.toggle("active",    tab === "login");
    document.getElementById("tab-register").classList.toggle("active", tab === "register");
    document.getElementById("form-login").classList.toggle("hidden",    tab !== "login");
    document.getElementById("form-register").classList.toggle("hidden", tab !== "register");
    showAlert("");
}

function showAlert(msg, type = "error") {
    const el = document.getElementById("alert");
    el.innerHTML = msg ? `<div class="alert ${type}">${msg}</div>` : "";
}

// Si ya está logueado, lo mando al inicio
if (Auth.isLogged()) location.href = "index.html";

document.getElementById("form-login").addEventListener("submit", async (e) => {
    e.preventDefault();
    const btn = e.target.querySelector("button[type=submit]");
    btn.disabled = true;
    btn.textContent = "Ingresando...";
    try {
        const token = await API.login(
            document.getElementById("login-username").value.trim(),
            document.getElementById("login-password").value
        );
        Auth.setToken(token);                       // viene como "Bearer xxx"
        Auth.setRole(Auth.parseRoleFromJwt(token));
        location.href = "index.html";
    } catch (err) {
        showAlert("Credenciales inválidas. Verificá tu usuario y contraseña.");
        btn.disabled = false;
        btn.textContent = "Ingresar";
    }
});

document.getElementById("form-register").addEventListener("submit", async (e) => {
    e.preventDefault();
    const btn = e.target.querySelector("button[type=submit]");
    btn.disabled = true;
    btn.textContent = "Creando cuenta...";
    const dto = {
        username:  document.getElementById("reg-username").value.trim(),
        password:  document.getElementById("reg-password").value,
        firstname: document.getElementById("reg-firstname").value.trim(),
        lastname:  document.getElementById("reg-lastname").value.trim(),
        email:     document.getElementById("reg-email").value.trim(),
        phone:     document.getElementById("reg-phone").value.trim()
    };
    try {
        await API.register(dto);
        showAlert("¡Cuenta creada! Ya podés iniciar sesión.", "success");
        showTab("login");
    } catch (err) {
        showAlert(err.message || "No se pudo registrar el usuario.");
    } finally {
        btn.disabled = false;
        btn.textContent = "Crear cuenta";
    }
});
