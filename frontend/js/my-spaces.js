if (!Auth.isLogged()) location.href = "login.html";

let mySpaces = [];
let currentSrvSpaceId = null;

// =====================================================
//  Carga y render de los espacios del dueño
// =====================================================
async function loadMySpaces() {
    loading("my-spaces");
    try {
        mySpaces = await API.myOwnedSpaces();
        renderMySpaces();
    } catch (e) {
        document.getElementById("my-spaces").innerHTML =
            `<div class="empty-state" style="grid-column:1/-1">
                <span class="icon">⚠️</span>
                No se pudieron cargar tus espacios.
             </div>`;
    }
}

function renderMySpaces() {
    const cont = document.getElementById("my-spaces");
    if (!mySpaces || mySpaces.length === 0) {
        cont.innerHTML = `
            <div class="empty-state" style="grid-column:1/-1">
                <span class="icon">🏛️</span>
                Todavía no publicaste ningún espacio.<br>
                <button class="btn mt" onclick="openSpaceModal()">Publicar mi primer espacio</button>
            </div>`;
        return;
    }
    cont.innerHTML = mySpaces.map(s => `
        <div class="card">
            <div class="thumb">🏛️</div>
            <div class="body">
                <h3>${escapeHtml(s.nameSpace || "Espacio")}</h3>
                <p class="desc">${escapeHtml((s.description || "").slice(0, 80))}${(s.description||"").length > 80 ? "…" : ""}</p>
                <div class="footer">
                    <span class="badge ${s.isActive ? 'green' : 'gray'}">${s.isActive ? 'Activo' : 'Inactivo'}</span>
                    <span class="price">$${Number(s.basePrice || 0).toLocaleString("es-AR")}</span>
                </div>
                <div class="flex flex-wrap mt">
                    <button class="btn small secondary" onclick="editSpace(${s.idSpace})">✏️ Editar</button>
                    <button class="btn small"           onclick="openSrvModal(${s.idSpace})">🛎 Servicios</button>
                    <button class="btn small secondary" onclick="openResModal(${s.idSpace})">📋 Reservas</button>
                    <button class="btn small danger"    onclick="deleteSpace(${s.idSpace})">🗑 Eliminar</button>
                </div>
            </div>
        </div>`).join("");
}

// =====================================================
//  Modal Crear / Editar espacio
// =====================================================
function openSpaceModal() {
    document.getElementById("space-modal-title").textContent = "Nuevo espacio";
    document.getElementById("s-id").value     = "";
    document.getElementById("s-name").value   = "";
    document.getElementById("s-desc").value   = "";
    document.getElementById("s-price").value  = "";
    document.getElementById("s-lat").value    = "";
    document.getElementById("s-lng").value    = "";
    document.getElementById("s-buffer").value = 2;
    document.getElementById("s-policy").value = "FLEXIBLE";
    document.getElementById("space-modal").classList.remove("hidden");
}

function closeSpaceModal() {
    document.getElementById("space-modal").classList.add("hidden");
}

function editSpace(id) {
    const s = mySpaces.find(x => x.idSpace === id);
    if (!s) return;
    document.getElementById("space-modal-title").textContent = "Editar espacio";
    document.getElementById("s-id").value     = s.idSpace;
    document.getElementById("s-name").value   = s.nameSpace   || "";
    document.getElementById("s-desc").value   = s.description || "";
    document.getElementById("s-price").value  = s.basePrice   || "";
    document.getElementById("s-lat").value    = s.location?.latitude  || "";
    document.getElementById("s-lng").value    = s.location?.longitude || "";
    document.getElementById("s-buffer").value = s.bufferTime  || 2;
    // intenta mapear la política
    const pol = s.cancellationPolicies?.policyType || s.cancellationPolicies || "FLEXIBLE";
    document.getElementById("s-policy").value = pol;
    document.getElementById("space-modal").classList.remove("hidden");
}

async function saveSpace() {
    const id   = document.getElementById("s-id").value;
    const name = document.getElementById("s-name").value.trim();
    const desc = document.getElementById("s-desc").value.trim();
    const price= numOrNull(document.getElementById("s-price").value);

    if (!name || !desc || !price) {
        alertBox("Completá al menos nombre, descripción y precio.");
        return;
    }

    const dto = {
        idSpace:              id ? Number(id) : null,
        idConsumerOwner:      null,        // El backend obtiene el ID del JWT
        location: {
            latitude:  numOrNull(document.getElementById("s-lat").value),
            longitude: numOrNull(document.getElementById("s-lng").value)
        },
        cancellationPolicies: document.getElementById("s-policy").value,
        googleCalendarId:     null,
        nameSpace:            name,
        description:          desc,
        basePrice:            price,
        publicationDate:      null,
        bufferTime:           Number(document.getElementById("s-buffer").value) || 2,
        active:               true,
        services:             null
    };

    const btn = document.querySelector("#space-modal .btn.success");
    btn.disabled = true; btn.textContent = "Guardando…";

    try {
        if (id) {
            await API.updateOwnedSpace(Number(id), dto);
            alertBox("Espacio actualizado correctamente.", "success");
        } else {
            await API.createOwnedSpace(dto);
            alertBox("Espacio publicado con éxito.", "success");
        }
        closeSpaceModal();
        await loadMySpaces();
    } catch (e) {
        alertBox(e.message || "No se pudo guardar el espacio.");
    } finally {
        btn.disabled = false; btn.textContent = "Guardar espacio";
    }
}

async function deleteSpace(id) {
    if (!confirm("¿Eliminar este espacio? Esta acción no se puede deshacer.")) return;
    try {
        await API.deleteOwnedSpace(id);
        alertBox("Espacio eliminado.", "success");
        await loadMySpaces();
    } catch (e) {
        alertBox(e.message || "No se pudo eliminar el espacio.");
    }
}

// =====================================================
//  Modal Servicios del espacio
// =====================================================
async function openSrvModal(idSpace) {
    currentSrvSpaceId = idSpace;
    document.getElementById("srv-desc").value  = "";
    document.getElementById("srv-price").value = "";
    document.getElementById("srv-modal").classList.remove("hidden");
    await renderServices();
}

function closeSrvModal() {
    document.getElementById("srv-modal").classList.add("hidden");
    currentSrvSpaceId = null;
}

async function renderServices() {
    const cont = document.getElementById("srv-list");
    cont.innerHTML = `<div class="loading"><div class="spinner"></div></div>`;
    try {
        const services = await API.servicesOfSpace(currentSrvSpaceId);
        if (!services || services.length === 0) {
            cont.innerHTML = `<p class="muted">Este espacio no tiene servicios todavía.</p>`;
            return;
        }
        cont.innerHTML = services.map(s => `
            <div class="service-row">
                <div style="flex:1">
                    <strong>${escapeHtml(s.description)}</strong>
                    <span class="badge gray" style="margin-left:8px">$${Number(s.price).toLocaleString("es-AR")}</span>
                    ${s.isActive === false ? '<span class="badge red" style="margin-left:4px">Inactivo</span>' : ''}
                </div>
                <button class="btn small danger" onclick="removeService(${s.id})">Quitar</button>
            </div>`).join("");
    } catch (e) {
        cont.innerHTML = `<p class="muted">No se pudieron cargar los servicios.</p>`;
    }
}

async function addService() {
    const desc  = document.getElementById("srv-desc").value.trim();
    const price = numOrNull(document.getElementById("srv-price").value);
    if (!desc || !price) { alertBox("Completá descripción y precio del servicio."); return; }

    const dto = { id: null, description: desc, price, isActive: true, idSpace: currentSrvSpaceId };
    const btn = document.querySelector("#srv-modal .btn.success");
    btn.disabled = true; btn.textContent = "Agregando…";

    try {
        await API.createService(dto);
        document.getElementById("srv-desc").value  = "";
        document.getElementById("srv-price").value = "";
        await renderServices();
    } catch (e) {
        alertBox(e.message || "No se pudo agregar el servicio.");
    } finally {
        btn.disabled = false; btn.textContent = "+ Agregar servicio";
    }
}

async function removeService(id) {
    if (!confirm("¿Quitar este servicio?")) return;
    try {
        await API.deleteService(id, currentSrvSpaceId);
        await renderServices();
    } catch (e) {
        alertBox(e.message || "No se pudo quitar el servicio.");
    }
}

// =====================================================
//  Modal Reservas recibidas (como dueño del espacio)
// =====================================================
async function openResModal(idSpace) {
    document.getElementById("res-modal").classList.remove("hidden");
    const cont = document.getElementById("res-list");
    cont.innerHTML = `<div class="loading"><div class="spinner"></div></div>`;

    try {
        // Traemos TODAS las reservas y filtramos por espacio en el cliente
        // (el backend no tiene endpoint GET /reservations?spaceId= en los controllers visibles)
        const all = await API.listAllReservations();
        const filtered = (all || []).filter(r => r.space?.idSpace === idSpace || r.idSpace === idSpace);

        if (!filtered.length) {
            cont.innerHTML = `<p class="muted">Este espacio no tiene reservas todavía.</p>`;
            return;
        }

        const rows = filtered.map(r => {
            const status = (r.status || "TENTATIVE").toString();
            let actions = "";
            if (status === "TENTATIVE") {
                actions = `
                    <button class="btn small success" onclick="ownerConfirm(${r.id})">✔ Confirmar</button>
                    <button class="btn small danger"  onclick="ownerReject(${r.id})">✘ Rechazar</button>`;
            } else if (status === "CONFIRMED") {
                actions = `<button class="btn small success" onclick="ownerComplete(${r.id})">✔ Completar</button>`;
            }
            return `
                <tr>
                    <td><strong>${escapeHtml(r.title||"")}</strong></td>
                    <td>${r.fromDate ? new Date(r.fromDate).toLocaleDateString("es-AR") : "—"}</td>
                    <td>${r.untilDate ? new Date(r.untilDate).toLocaleDateString("es-AR") : "—"}</td>
                    <td><span class="status-${status}">${status}</span></td>
                    <td><div class="flex">${actions || '<span class="muted">—</span>'}</div></td>
                </tr>`;
        }).join("");

        cont.innerHTML = `
            <div class="table-wrap">
                <table>
                    <thead><tr><th>Evento</th><th>Desde</th><th>Hasta</th><th>Estado</th><th>Acción</th></tr></thead>
                    <tbody>${rows}</tbody>
                </table>
            </div>`;
    } catch (e) {
        cont.innerHTML = `<p class="muted">No se pudieron cargar las reservas.</p>`;
    }
}

function closeResModal() {
    document.getElementById("res-modal").classList.add("hidden");
}

async function ownerConfirm(id) {
    try {
        await API.confirmReservation(id);
        alertBox("Reserva confirmada.", "success");
        // Refresca la tabla dentro del modal buscando el spaceId actual
        const spaceId = mySpaces.find(s =>
            document.getElementById("res-modal").querySelector("table") !== null
        )?.idSpace;
        // Reabrimos el modal del mismo espacio
        document.getElementById("res-list").innerHTML = `<div class="loading"><div class="spinner"></div></div>`;
        await openResModal(spaceId || 0);
    } catch (e) { alertBox(e.message || "Error al confirmar."); }
}

async function ownerReject(id) {
    if (!confirm("¿Rechazar esta reserva?")) return;
    try {
        await API.rejectReservation(id);
        alertBox("Reserva rechazada.", "success");
    } catch (e) { alertBox(e.message || "Error al rechazar."); }
}

async function ownerComplete(id) {
    try {
        await API.completeReservation(id);
        alertBox("Reserva marcada como completada.", "success");
    } catch (e) { alertBox(e.message || "Error al completar."); }
}

renderNav();
loadMySpaces();
