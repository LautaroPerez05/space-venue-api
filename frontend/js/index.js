// ====================================================
//  Helpers compartidos (usados por todos los .js)
// ====================================================
function escapeHtml(str) {
    return (str || "").replace(/[&<>"']/g,
        c => ({ "&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;" }[c]));
}

function numOrNull(v) {
    return (v === "" || v == null) ? null : Number(v);
}

function alertBox(msg, type = "error") {
    const el = document.getElementById("alert");
    if (el) el.innerHTML = msg ? `<div class="alert ${type}">${msg}</div>` : "";
}

function loading(containerId) {
    const el = document.getElementById(containerId);
    if (el) el.innerHTML = `<div class="loading"><div class="spinner"></div></div>`;
}

// ====================================================
//  Navbar dinámica (importada en todas las páginas)
// ====================================================
function renderNav() {
    const nav = document.getElementById("nav-links");
    if (!nav) return;
    if (Auth.isLogged()) {
        const isAdmin = Auth.isAdmin();
        nav.innerHTML = `
            <a href="index.html">Home</a>
            <a href="reservations.html">Mis reservas</a>
            <a href="my-spaces.html">Mis espacios</a>
            <a href="notifications.html">Notificaciones</a>
            ${isAdmin ? `<a href="admin-spaces.html">Admin Espacios</a>` : ''}
            ${isAdmin ? `<a href="admin-users.html">Admin Usuarios</a>` : ''}
            <button class="btn-outline" onclick="doLogout()">Salir</button>`;
    } else {
        nav.innerHTML = `
            <a href="login.html" class="btn-outline">Ingresar</a>
            <a href="login.html" class="btn-solid">Registrarse</a>`;
    }
}

async function doLogout() {
    try { await API.logout(); } catch (e) { /* token ya vencido, no importa */ }
    Auth.clear();
    location.href = "index.html";
}

// ====================================================
//  Render de tarjeta de espacio
// ====================================================
function spaceCard(s) {
    const price = s.basePrice != null ? Number(s.basePrice).toLocaleString("es-AR") : "-";
    const loc = s.location
        ? `${Number(s.location.latitude).toFixed(4)}, ${Number(s.location.longitude).toFixed(4)}`
        : "Ubicación a confirmar";
    const desc = (s.description || "").slice(0, 90);
    return `
        <a class="card" href="space.html?id=${s.idSpace}">
            <div class="thumb">🏛️</div>
            <div class="body">
                <h3>${escapeHtml(s.nameSpace || "Espacio")}</h3>
                <div class="loc">📍 ${escapeHtml(loc)}</div>
                <div class="desc">${escapeHtml(desc)}${s.description && s.description.length > 90 ? "…" : ""}</div>
                <div class="footer">
                    <span class="badge yellow">Disponible</span>
                    <span class="price">$${price}<small>/evento</small></span>
                </div>
            </div>
        </a>`;
}

function renderSpaces(list) {
    const cont = document.getElementById("spaces");
    if (!cont) return;
    if (!list || list.length === 0) {
        cont.innerHTML = `
            <div class="empty-state" style="grid-column:1/-1">
                <span class="icon">🔍</span>
                No se encontraron espacios con esos criterios.
            </div>`;
        return;
    }
    cont.innerHTML = list.map(spaceCard).join("");
}

// ====================================================
//  Carga inicial y búsqueda
// ====================================================
async function loadSpaces() {
    loading("spaces");
    try {
        const list = await API.listActiveSpaces();
        renderSpaces(list);
    } catch (e) {
        const cont = document.getElementById("spaces");
        if (cont) cont.innerHTML = `
            <div class="empty-state" style="grid-column:1/-1">
                <span class="icon">⚠️</span>
                No se pudieron cargar los espacios. ¿Está el servidor corriendo?
            </div>`;
    }
}

async function searchSpaces() {
    const filter = {
        idConsumerOwner: null,
        idLocation:      null,
        nameSpace:       document.getElementById("f-name")?.value.trim() || null,
        minPrice:        numOrNull(document.getElementById("f-min")?.value),
        maxPrice:        numOrNull(document.getElementById("f-max")?.value),
        lat: null, lng: null, radious: null
    };
    loading("spaces");
    try {
        const list = await API.filterSpaces(filter);
        renderSpaces(list);
    } catch (e) {
        alertBox("Error al filtrar los espacios.");
    }
}

// ====================================================
//  Inicialización (solo se ejecuta en index.html)
// ====================================================
renderNav();
if (document.getElementById("spaces")) loadSpaces();
