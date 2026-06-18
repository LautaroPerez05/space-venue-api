// =============================================================
//  ADMIN SPACES - Gestión administrativa de espacios
// =============================================================

let allSpaces = [];
let editingSpaceId = null;
let adminMap = null;
let adminMarker = null;

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
            <td>${s.idSpace || '-'}</td>
            <td>${s.nameSpace || '-'}</td>
            <td>${s.consumerOwner?.idConsumer || s.idConsumerOwner || '-'}</td>
            <td>$${(s.basePrice || 0).toLocaleString("es-AR")}</td>
            <td><span class="badge ${s.isActive ? 'success' : 'danger'}">${s.isActive ? 'Activo' : 'Inactivo'}</span></td>
            <td>
                <button class="btn small" onclick="editSpace(${s.idSpace})">Editar</button>
                <button class="btn small danger" onclick="deleteSpace(${s.idSpace})">Eliminar</button>
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
    initAdminMap();
}

async function editSpace(spaceId) {
    try {
        const space = await API.getSpace(spaceId);
        editingSpaceId = spaceId;
        document.getElementById("modal-title").textContent = "Editar Espacio";
        
        document.getElementById("space-name").value = space.nameSpace || "";
        document.getElementById("space-desc").value = space.description || "";
        document.getElementById("space-price").value = space.basePrice || 0;
        document.getElementById("space-lat").value = space.location?.latitude || 0;
        document.getElementById("space-lng").value = space.location?.longitude || 0;
        const pol = space.cancellationPolicies?.policyType || space.cancellationPolicies || "FLEXIBLE";
        document.getElementById("space-policy").value = pol;
        // Mostrar buffer en minutos en la UI (backend guarda horas)
        document.getElementById("space-buffer").value = (space.bufferTime != null) ? (Number(space.bufferTime) * 60) : 0;
        document.getElementById("space-active").checked = space.isActive !== false;
        
        document.getElementById("space-modal").style.display = "block";
        initAdminMap();
        // colocar marcador si la ubicación existe
        const lat = numOrNull(document.getElementById("space-lat").value);
        const lng = numOrNull(document.getElementById("space-lng").value);
        if (lat != null && lng != null) setAdminMarker(lat, lng, true);
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
        // El input está en minutos; convertir a horas para el backend
        bufferTime: (function(){ const mins = parseInt(document.getElementById("space-buffer").value); return (Number.isFinite(mins) && mins>0) ? (mins/60) : 0; })(),
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

// Inicializa mapa para admin (crear/editar espacio)
function initAdminMap() {
    // Si Leaflet no está cargado aún, reintentar dentro de 200ms
    if (typeof L === 'undefined') { setTimeout(initAdminMap, 200); return; }
    console.debug('initAdminMap: L present=', typeof L !== 'undefined', 'adminMap exists=', !!adminMap);
    const el = document.getElementById('admin-space-map');
    if (!el) return;
    if (!adminMap) {
        const DEFAULT = [-34.6037, -58.3816];
        adminMap = L.map('admin-space-map', { attributionControl: false }).setView(DEFAULT, 13);
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { maxZoom: 19 }).addTo(adminMap);
        adminMap.on('click', (e) => {
            setAdminMarker(e.latlng.lat, e.latlng.lng, false);
        });
    }
    setTimeout(() => { try { adminMap.invalidateSize(); console.debug('adminMap.invalidateSize called'); } catch (e) { console.error('admin invalidateSize', e); } }, 200);
}

function setAdminMarker(lat, lng, fly) {
    if (!adminMap) return;
    if (adminMarker) adminMarker.setLatLng([lat, lng]);
    else {
        adminMarker = L.marker([lat, lng], { draggable: true }).addTo(adminMap);
        adminMarker.on('dragend', (ev) => {
            const p = ev.target.getLatLng();
            document.getElementById('space-lat').value = p.lat;
            document.getElementById('space-lng').value = p.lng;
        });
    }
    document.getElementById('space-lat').value = lat;
    document.getElementById('space-lng').value = lng;
    if (fly) adminMap.flyTo([lat, lng], 15); else adminMap.setView([lat, lng], 15);
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
