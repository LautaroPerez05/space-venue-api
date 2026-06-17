// =============================================================
//  ADMIN USERS - Gestión administrativa de usuarios
// =============================================================

let allUsers = [];
let editingUserId = null;

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

    table.innerHTML = users.map(u => `
        <tr>
            <td>${u.id || u.idConsumer || '-'}</td>
            <td>${u.firstname || u.firstName || '-'} ${u.lastname || u.lastName || ''}</td>
            <td>${u.email || '-'}</td>
            <td>${u.phone || '-'}</td>
            <td><span class="badge">${formatRole(u.rol || u.role || 'CLIENT')}</span></td>
            <td><span class="badge ${u.isActive !== false ? 'success' : 'danger'}">${u.isActive !== false ? 'Activo' : 'Inactivo'}</span></td>
            <td>
                <button class="btn small" onclick="toggleUserStatus(${u.id || u.idConsumer})">Cambiar Estado</button>
                <button class="btn small danger" onclick="deleteUser(${u.id || u.idConsumer})">Eliminar</button>
            </td>
        </tr>
    `).join("");
}

function filterUsers() {
    const search = document.getElementById("search-input").value.toLowerCase();
    const filtered = allUsers.filter(u => {
        const name = `${u.firstname || u.firstName || ''} ${u.lastname || u.lastName || ''}`.toLowerCase();
        const email = (u.email || '').toLowerCase();
        return name.includes(search) || email.includes(search);
    });
    renderUsersTable(filtered);
}

function showCreateModal() {
    editingUserId = null;
    document.getElementById("modal-title").textContent = "Crear Nuevo Usuario";
    document.getElementById("user-form").reset();
    document.getElementById("user-password").required = true;
    document.getElementById("user-modal").style.display = "flex";
}

function closeModal() {
    document.getElementById("user-modal").style.display = "none";
}

async function saveUser(event) {
    event.preventDefault();

    const dto = {
        firstname: document.getElementById("user-firstname").value,
        lastname: document.getElementById("user-lastname").value,
        email: document.getElementById("user-email").value,
        phone: document.getElementById("user-phone").value,
        username: document.getElementById("user-username").value,
        password: document.getElementById("user-password").value,
        rol: document.getElementById("user-role").value,
        isActive: document.getElementById("user-active").checked
    };

    try {
        if (editingUserId) {
            showAlert("La edición de usuarios se realiza desde el perfil del usuario", "info");
        } else {
            await API.createUser(dto);
            showAlert("Usuario creado correctamente", "success");
        }
        closeModal();
        loadAllUsers();
    } catch (error) {
        showAlert("Error guardando usuario: " + error.message, "error");
    }
}

async function toggleUserStatus(userId) {
    try {
        await API.toggleUserStatus(userId);
        showAlert("Estado del usuario actualizado", "success");
        loadAllUsers();
    } catch (error) {
        showAlert("Error actualizando estado: " + error.message, "error");
    }
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

window.onclick = function(event) {
    const modal = document.getElementById("user-modal");
    if (event.target === modal) {
        closeModal();
    }
}
