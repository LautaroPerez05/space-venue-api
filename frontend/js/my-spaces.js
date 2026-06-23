if (!Auth.isLogged()) location.href = "login.html";

let mySpaces = [];
let currentSrvSpaceId = null;
// Leaflet map variables para seleccionar ubicación al crear/editar espacio
let spaceMap = null;
let spaceMarker = null;

// =====================================================
//  Carga y render de los espacios del dueño
// =====================================================
// Helper: formatea Date a yyyy-MM-dd'T'HH:mm:ss (hora local)
function formatLocalDateTime(d) {
    const two = (n) => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${two(d.getMonth()+1)}-${two(d.getDate())}T${two(d.getHours())}:${two(d.getMinutes())}:${two(d.getSeconds())}`;
}

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
    document.getElementById("s-buffer").value = 120; // minutos por defecto
    document.getElementById("s-policy").value = "FLEXIBLE";
    document.getElementById("s-images").value = "";
    document.getElementById("s-images-preview").innerHTML = "";
    document.getElementById("space-modal").classList.remove("hidden");
    // Inicializa o refresca el mapa cuando se abre el modal
    initSpaceMap();

    // Agregar listener para preview de imágenes (usar asignación para evitar múltiples listeners)
    const imagesInput = document.getElementById("s-images");
    if (imagesInput) imagesInput.onchange = previewImages;
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
    // Mostrar buffer en minutos en la UI (backend guarda horas)
    document.getElementById("s-buffer").value = (s.bufferTime != null) ? (Number(s.bufferTime) * 60) : 120;
    // intenta mapear la política
    const pol = s.cancellationPolicies?.policyType || s.cancellationPolicies || "FLEXIBLE";
    document.getElementById("s-policy").value = pol;
    document.getElementById("space-modal").classList.remove("hidden");
    // Inicializa el mapa y coloca el marcador en la ubicación existente (si la hay)
    initSpaceMap();
    const lat = numOrNull(document.getElementById("s-lat").value);
    const lng = numOrNull(document.getElementById("s-lng").value);
    if (lat != null && lng != null) {
        setSpaceMarker(lat, lng, true);
    }
}

async function saveSpace() {
    let id   = document.getElementById("s-id").value;
    const name = document.getElementById("s-name").value.trim();
    const desc = document.getElementById("s-desc").value.trim();
    const price= numOrNull(document.getElementById("s-price").value);

    if (!name || !desc || !price) {
        alertBox("Completá al menos nombre, descripción y precio.");
        return;
    }
    const lat = numOrNull(document.getElementById("s-lat").value);
    const lng = numOrNull(document.getElementById("s-lng").value);
    if (lat == null || lng == null) {
        alertBox("Seleccioná la ubicación del espacio en el mapa.");
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
        // El campo en la UI está en minutos; convertir a horas para el backend
        bufferTime: (function(){
            const mins = Number(document.getElementById("s-buffer").value);
            return (Number.isFinite(mins) && mins > 0) ? (mins / 60) : 2;
        })(),
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
            const res = await API.createOwnedSpace(dto);
            alertBox("Espacio publicado con éxito.", "success");
            // Si el backend devolvió el id, úsalo
            if (res) {
                if (typeof res === 'object' && (res.idSpace || res.id_space || res.id)) {
                    id = res.idSpace || res.id_space || res.id;
                }
            }

            // Si por alguna razón el backend no devuelve id, intentamos recuperar buscando por nombre/precio
            if (!id) {
                const owned = await API.myOwnedSpaces();
                const created = (owned || []).find(s => s.nameSpace === name && Number(s.basePrice) === Number(price));
                if (created) id = created.idSpace;
            }
        }

        // Cargar imágenes si existen
        const imagesInput = document.getElementById("s-images");
        if (id && imagesInput?.files && imagesInput.files.length > 0) {
            await uploadSpaceImages(Number(id), imagesInput.files);
            // Esperar un poco a que el backend procese las imágenes antes de recargar
            await new Promise(resolve => setTimeout(resolve, 500));
        }

        closeSpaceModal();
        await loadMySpaces();
    } catch (e) {
        alertBox(e.message || "No se pudo guardar el espacio.");
    } finally {
        btn.disabled = false; btn.textContent = "Guardar espacio";
    }
}

// Inicializa el mapa Leaflet dentro del modal si aún no existe
function initSpaceMap() {
    // Si Leaflet no está cargado aún, reintentar dentro de 200ms (evita condiciones de carrera)
    if (typeof L === 'undefined') { setTimeout(initSpaceMap, 200); return; }

    const mapContainer = document.getElementById('s-map');
    if (!mapContainer) return;

    console.debug('initSpaceMap: L present=', typeof L !== 'undefined', 'spaceMap exists=', !!spaceMap);
    // Crear mapa solo una vez
    if (!spaceMap) {
        // Coordenadas por defecto (centro de Buenos Aires)
        const DEFAULT = [-34.6037, -58.3816];
        spaceMap = L.map('s-map', { attributionControl: false }).setView(DEFAULT, 13);

        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 19,
        }).addTo(spaceMap);

        // Click en el mapa para crear/mover marcador
        spaceMap.on('click', function (e) {
            const lat = e.latlng.lat;
            const lng = e.latlng.lng;
            setSpaceMarker(lat, lng, false);
        });
    }

    // Forzar recalculo de tamaño cuando el modal queda visible
    setTimeout(() => { try { spaceMap.invalidateSize(); console.debug('spaceMap.invalidateSize called'); } catch (e) { console.error('invalidateSize error', e); } }, 200);
}

function setSpaceMarker(lat, lng, fly) {
    if (!spaceMap) return;
    if (spaceMarker) {
        spaceMarker.setLatLng([lat, lng]);
    } else {
        spaceMarker = L.marker([lat, lng], { draggable: true }).addTo(spaceMap);
        // Permitir arrastrar el marcador para ajustar la posición
        spaceMarker.on('dragend', function (ev) {
            const p = ev.target.getLatLng();
            document.getElementById('s-lat').value = p.lat;
            document.getElementById('s-lng').value = p.lng;
        });
    }

    // Actualiza los inputs ocultos
    document.getElementById('s-lat').value = lat;
    document.getElementById('s-lng').value = lng;

    if (fly) {
        spaceMap.flyTo([lat, lng], 15);
    } else {
        spaceMap.setView([lat, lng], 15);
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
        const spaceIdNum = Number(idSpace);
        const filtered = (all || []).filter(r => Number(r.space?.idSpace) === spaceIdNum || Number(r.idSpace) === spaceIdNum);

        if (!filtered.length) {
            cont.innerHTML = `<p class="muted">Este espacio no tiene reservas todavía.</p>`;
            return;
        }

        const rows = filtered.map(r => {
            const status = (r.status || "TENTATIVE").toString();
            const reserver = r.consumer ? (r.consumer.firstname + ' ' + r.consumer.lastname) : (r.consumerName || r.consumerFullName || '—');
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
                    <td><strong>${escapeHtml(r.title||"")}</strong><br><small class="muted">Por: ${escapeHtml(reserver)}</small></td>
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

// Función para previsualizar imágenes seleccionadas
function previewImages() {
    const input = document.getElementById("s-images");
    const preview = document.getElementById("s-images-preview");
    preview.innerHTML = "";

    if (input.files) {
        Array.from(input.files).forEach(file => {
            const reader = new FileReader();
            reader.onload = (e) => {
                const img = document.createElement("img");
                img.src = e.target.result;
                img.style.width = "80px";
                img.style.height = "80px";
                img.style.borderRadius = "4px";
                img.style.objectFit = "cover";
                preview.appendChild(img);
            };
            reader.readAsDataURL(file);
        });
    }
}

// Función para cargar imágenes del espacio
async function uploadSpaceImages(idSpace, files) {
    // Procesar archivos de forma secuencial y esperar a que cada upload termine
    for (const file of files) {
        await new Promise((resolve) => {
            const reader = new FileReader();
            reader.onload = async (e) => {
                const base64 = e.target.result;
                try {
                    const dto = {
                        idImage: null,
                        idSpace: idSpace,
                        fileName: file.name,
                        urlImage: base64,
                        dateSend: formatLocalDateTime(new Date())
                    };
                    await API.createImage(dto);
                    console.log("Imagen cargada:", file.name);
                } catch (err) {
                    console.error("Error al cargar imagen:", err);
                }
                resolve();
            };
            reader.onerror = (err) => {
                console.error("Error leyendo archivo:", err);
                resolve();
            };
            reader.readAsDataURL(file);
        });
    }
}

renderNav();
loadMySpaces();
