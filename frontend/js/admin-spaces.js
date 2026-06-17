// =============================================================
//  ADMIN SPACES - Gestión administrativa de espacios
// =============================================================

let allSpaces = [];
let editingSpaceId = null;

document.addEventListener("DOMContentLoaded", () => {
    if (!Auth.isAdmin()) {
        location.href = "index.html";
        return;
    }
    renderNav();
    loadAllSpaces();
});

async function loadAllSpaces() {
    try {
        allSpaces = await API.listAllSpaces();
        renderSpacesTable(allSpaces);
    } catch (error) {
        showAlert("Error cargando espacios: " + error.message, "error");
    }
}

function renderSpacesTable(spaces) {
    const table = document.getElementById("spaces-table");
    
    if (!spaces || spaces.length === 0) {
        table.innerHTML = `<tr><td colspan="6" class="text-center">No hay espacios</td></tr>`;
        return;
    }

    table.innerHTML = spaces.map(s => `
        <tr>
            <td>${s.id || s.idSpace || '-'}</td>
            <td>${s.nameSpace || s.name || '-'}</td>
            <td>${s.idConsumerOwner || '-'}</td>
            <td>$${(s.basePrice || 0).toLocaleString("es-AR")}</td>
            <td><span class="badge ${s.active ? 'success' : 'danger'}">${s.active ? 'Activo' : 'Inactivo'}</span></td>
            <td>
                <button class="btn small" onclick="editSpace(${s.id || s.idSpace})">Editar</button>
                <button class="btn small danger" onclick="deleteSpace(${s.id || s.idSpace})">Eliminar</button>
            </td>
        </tr>
    `).join("");
}

function filterSpaces() {
    const search = document.getElementById("search-input").value.toLowerCase();
    const filtered = allSpaces.filter(s => 
        (s.nameSpace || s.name || "").toLowerCase().includes(search)
    );
    renderSpacesTable(filtered);
}

function showCreateModal() {
    editingSpaceId = null;
    document.getElementById("modal-title").textContent = "Crear Nuevo Espacio";
    document.getElementById("space-form").reset();
    document.getElementById("space-modal").style.display = "flex";
}

async function editSpace(spaceId) {
    try {
        const space = await API.getSpace(spaceId);
        editingSpaceId = spaceId;
        document.getElementById("modal-title").textContent = "Editar Espacio";
        
        document.getElementById("space-name").value = space.nameSpace || space.name || "";
        document.getElementById("space-desc").value = space.description || "";
        document.getElementById("space-price").value = space.basePrice || 0;
        document.getElementById("space-lat").value = space.location?.latitude || 0;
        document.getElementById("space-lng").value = space.location?.longitude || 0;
        document.getElementById("space-policy").value = space.cancellationPolicies || "FLEXIBLE";
        document.getElementById("space-buffer").value = space.bufferTime || 0;
        document.getElementById("space-active").checked = space.active !== false;
        
        document.getElementById("space-modal").style.display = "block";
    } catch (error) {
        showAlert("Error cargando espacio: " + error.message, "error");
    }
}

async function saveSpace(event) {
    event.preventDefault();

    const dto = {
        nameSpace: document.getElementById("space-name").value,
        description: document.getElementById("space-desc").value,
        basePrice: parseFloat(document.getElementById("space-price").value),
        location: {
            latitude: parseFloat(document.getElementById("space-lat").value),
            longitude: parseFloat(document.getElementById("space-lng").value)
        },
        cancellationPolicies: document.getElementById("space-policy").value,
        bufferTime: parseInt(document.getElementById("space-buffer").value),
        active: document.getElementById("space-active").checked
    };

    try {
        if (editingSpaceId) {
            await API.updateSpace(editingSpaceId, dto);
            showAlert("Espacio actualizado correctamente", "success");
        } else {
            await API.createSpace(dto);
            showAlert("Espacio creado correctamente", "success");
        }
        closeModal();
        loadAllSpaces();
    } catch (error) {
        showAlert("Error guardando espacio: " + error.message, "error");
    }
}

async function deleteSpace(spaceId) {
    if (!confirm("¿Estás seguro que deseas eliminar este espacio?")) return;

    try {
        await API.deleteSpace(spaceId);
        showAlert("Espacio eliminado correctamente", "success");
        loadAllSpaces();
    } catch (error) {
        showAlert("Error eliminando espacio: " + error.message, "error");
    }
}

function closeModal() {
    document.getElementById("space-modal").style.display = "none";
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
        <a href="admin-spaces.html" class="active">Admin Espacios</a>
        <a href="admin-users.html">Admin Usuarios</a>
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
    const modal = document.getElementById("space-modal");
    if (event.target === modal) {
        closeModal();
    }
}
