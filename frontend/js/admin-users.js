// =============================================================
//  ADMIN USERS - Gestión administrativa de usuarios
// =============================================================

let allUsers = [];

document.addEventListener("DOMContentLoaded", () => {
    if (!Auth.isAdmin()) {
        location.href = "index.html";
        return;
    }
    renderNav();
    loadAllUsers();
});

async function loadAllUsers() {
    try {
        allUsers = await API.listUsers();
        renderUsersTable(allUsers);
    } catch (error) {
        showAlert("Error cargando usuarios: " + error.message, "error");
    }
}

function renderUsersTable(users) {
    const table = document.getElementById("users-table");

    if (!users || users.length === 0) {
        table.innerHTML = `<tr><td colspan="7" class="text-center">No hay usuarios</td></tr>`;
        return;
    }

    table.innerHTML = users.map(u => {
        const creds = u.credentials || {};
        const rol = creds.rol || 'ROLE_CLIENT';
        const isActive = creds.isActive !== false;

        return `
        <tr>
            <td>${u.idConsumer || '-'}</td>
            <td>${u.firstname || '-'} ${u.lastname || ''}</td>
            <td>${u.email || '-'}</td>
            <td>${u.phone || '-'}</td>
            <td><span class="badge">${formatRole(rol)}</span></td>
            <td><span class="badge ${isActive ? 'success' : 'danger'}">${isActive ? 'Activo' : 'Inactivo'}</span></td>
            <td>
                <button class="btn small danger" onclick="deleteUser(${u.idConsumer})">Eliminar</button>
            </td>
        </tr>
        `;
    }).join("");
}

async function deleteUser(userId) {
    if (!confirm("¿Estás seguro que deseas eliminar este usuario? Esta acción no se puede deshacer.")) return;

    try {
        await API.deleteUser(userId);
        showAlert("Usuario eliminado correctamente", "success");
        loadAllUsers();
    } catch (error) {
        showAlert("Error eliminando usuario: " + error.message, "error");
    }
}

function formatRole(rol) {
    const roles = {
        "ROLE_ADMIN": "Administrador",
        "ROLE_CLIENT": "Cliente",
        "ADMIN": "Administrador",
        "CLIENT": "Cliente"
    };
    return roles[rol] || rol;
}

function showAlert(message, type = "info") {
    const alert = document.getElementById("alert");
    alert.className = `alert alert-${type}`;
    alert.textContent = message;
    alert.style.display = "block";
    setTimeout(() => alert.style.display = "none", 5000);
}

function renderNav() {
    const nav = document.getElementById("nav-links");
    nav.innerHTML = `
        <a href="index.html">Home</a>
        <a href="my-spaces.html">Mis espacios</a>
        <a href="reservations.html">Reservas</a>
        <a href="notifications.html">Notificaciones</a>
        <a href="admin-spaces.html">Admin Espacios</a>
        <a href="admin-users.html" class="active">Admin Usuarios</a>
        <a onclick="logout()">Salir</a>
    `;
}

async function logout() {
    try {
        await API.logout();
    } catch (e) {}
    Auth.clear();
    location.href = "login.html";
}