const spaceId = Number(new URLSearchParams(location.search).get("id"));
let currentSpace   = null;
let spaceServices  = [];

// ---- Utilidad segura para llamadas que pueden fallar ----
async function safe(fn, fallback) {
    try { return await fn(); } catch (e) { return fallback; }
}

// ---- Puntaje promedio ----
function avgScore(comments) {
    if (!comments || comments.length === 0) return null;
    const sum = comments.reduce((a, c) => a + (c.score || 0), 0);
    return (sum / comments.length).toFixed(1);
}

// ---- Renderizado principal ----
async function loadDetail() {
    if (!spaceId) { alertBox("Espacio no encontrado."); return; }
    loading("detail");
    try {
        currentSpace  = await API.getSpace(spaceId);
        const images  = await safe(() => API.imagesBySpace(spaceId), []);
        const comments= await safe(() => API.commentsBySpace(spaceId), []);

        if (Auth.isLogged()) {
            spaceServices = await safe(() => API.servicesOfSpace(spaceId), currentSpace.services || []);
        } else {
            spaceServices = currentSpace.services || [];
        }

        renderDetail(currentSpace, images, comments);
    } catch (e) {
        document.getElementById("detail").innerHTML =
            `<div class="empty-state"><span class="icon">⚠️</span>No se pudo cargar el espacio.</div>`;
    }
}

function renderDetail(s, images, comments) {
    const price = s.basePrice != null ? Number(s.basePrice).toLocaleString("es-AR") : "-";
    const score = avgScore(comments);

    // Galería
    let galleryHtml = "";
    if (images && images.length) {
        galleryHtml = images.slice(0,6).map(img =>
            `<img src="${escapeHtml(img.urlImage || "")}" alt="${escapeHtml(img.fileName || "")}"
                  onerror="this.outerHTML='<div class=ph>🖼️</div>'">`
        ).join("");
    } else {
        galleryHtml = `<div class="ph">🖼️</div><div class="ph">🖼️</div><div class="ph">🖼️</div>`;
    }

    // Servicios
    let servicesHtml = "";
    if (spaceServices && spaceServices.length) {
        servicesHtml = spaceServices
            .filter(srv => srv.isActive !== false)
            .map(srv => `
                <div class="service-row">
                    <input type="checkbox" class="srv-chk" id="srv-${srv.id}"
                           value="${srv.id}" data-price="${srv.price}"
                           data-desc="${escapeHtml(srv.description)}">
                    <label for="srv-${srv.id}">${escapeHtml(srv.description)}</label>
                    <span class="badge gray">$${Number(srv.price).toLocaleString("es-AR")}</span>
                </div>`).join("");
    } else {
        servicesHtml = `<p class="muted">Este espacio no ofrece servicios adicionales.</p>`;
    }

    // Comentarios
    let commentsHtml = "";
    if (comments && comments.length) {
        commentsHtml = comments.map(c => {
            const nombreParaMostrar = c.username || "Usuario Anónimo";
            const stars  = c.score || 0;
            return `
            <div class="comment">
                <div class="flex between">
                    <strong>${escapeHtml(nombreParaMostrar)}</strong>
                    <span class="stars">${"★".repeat(stars)}${"☆".repeat(Math.max(0, 5-stars))}</span>
                </div>
                <p>${escapeHtml(c.description || "")}</p>
            </div>`;
        }).join("");
    } else {
        commentsHtml = `<p class="muted">Todavía no hay comentarios para este espacio.</p>`;
    }

    const loc = s.location
        ? `${s.location.latitude}, ${s.location.longitude}`
        : "Ubicación a confirmar";

    document.getElementById("detail").innerHTML = `
        <div class="detail-header">
            <div class="flex between flex-wrap mb">
                <h1>${escapeHtml(s.nameSpace || "Espacio")}</h1>
                ${score ? `<span class="badge green" style="font-size:1rem">⭐ ${score}</span>` : ""}
            </div>
            <p class="muted mb">📍 ${escapeHtml(loc)}</p>
            <div class="gallery">${galleryHtml}</div>
            <p>${escapeHtml(s.description || "")}</p>
        </div>

        <div class="detail-grid">
            <div>
                ${spaceServices && spaceServices.length ? `
                    <h2 class="section-title">Servicios adicionales</h2>
                    <p class="muted mb">Seleccioná los servicios que querés sumar a tu evento.</p>
                    ${servicesHtml}` : ""}

                <h2 class="section-title" style="margin-top:28px">Opiniones</h2>
                ${commentsHtml}

                ${Auth.isLogged() ? `
                <div class="mt" style="background:var(--card);border:1px solid var(--border);border-radius:var(--radius);padding:16px">
                    <h3 class="mb">Dejá tu comentario</h3>
                    <div class="form-group">
                        <label>Descripción</label>
                        <textarea id="c-desc" rows="3" placeholder="¿Cómo fue tu experiencia?"></textarea>
                    </div>
                    <div class="form-group">
                        <label>Puntuación (1–5)</label>
                        <select id="c-score">
                            <option value="5">⭐⭐⭐⭐⭐ Excelente</option>
                            <option value="4">⭐⭐⭐⭐ Muy bueno</option>
                            <option value="3">⭐⭐⭐ Bueno</option>
                            <option value="2">⭐⭐ Regular</option>
                            <option value="1">⭐ Malo</option>
                        </select>
                    </div>
                    <button class="btn small success" onclick="submitComment()">Enviar comentario</button>
                </div>` : ""}
            </div>

            <aside>
                <div class="booking-box">
                    <div class="price">$${price} <small>por hora</small></div>
                    ${renderBookingForm()}
                </div>
            </aside>
        </div>`;

    bindServiceTotals();

    // Actualizar UI de Google OAuth2 si el módulo está cargado
    try { if (typeof GoogleOAuth2 !== 'undefined') GoogleOAuth2.updateUI('google-calendar-container'); } catch (e) { /* no-op */ }
}

// ---- Formulario de reserva ----
function renderBookingForm() {
    if (!Auth.isLogged()) {
        return `
            <p class="muted mb">Iniciá sesión para reservar este espacio.</p>
            <a href="login.html" class="btn" style="width:100%;display:block;text-align:center">
                Ingresar para reservar
            </a>`;
    }
    return `
        <div class="form-group"><label>Título del evento</label>
            <input id="r-title" placeholder="Cumpleaños de Sofía"></div>
        <div class="form-group"><label>Descripción</label>
            <input id="r-desc" placeholder="Celebración de 15 años"></div>
        <div class="form-group"><label>Desde</label>
            <input id="r-from" type="datetime-local"></div>
        <div class="form-group"><label>Hasta</label>
            <input id="r-until" type="datetime-local"></div>
        <div class="form-group"><label style="display:flex;align-items:center">
            <input id="r-save-calendar" type="checkbox" style="margin-right:8px"> Guardar en mi calendario
        </label></div>
        <div id="google-calendar-container"></div>
        <div class="flex between mb" style="border-top:1px solid var(--border);padding-top:12px;margin-top:4px">
            <span>Total estimado</span>
            <strong id="r-total">$${Number(currentSpace.basePrice||0).toLocaleString("es-AR")}</strong>
        </div>
        <button class="btn success" style="width:100%" onclick="doReserve()">
            Reservar ahora →
        </button>`;
}

function bindServiceTotals() {
    document.querySelectorAll(".srv-chk").forEach(chk =>
        chk.addEventListener("change", recalcTotal));
    
    // Agregar listeners para cambios en las fechas
    const fromInput = document.getElementById("r-from");
    const untilInput = document.getElementById("r-until");
    if (fromInput) fromInput.addEventListener("change", recalcTotal);
    if (untilInput) untilInput.addEventListener("change", recalcTotal);
}

function recalcTotal() {
    const base  = Number(currentSpace?.basePrice || 0);

    // Calcular horas entre fechas
    const fromInput = document.getElementById("r-from")?.value;
    const untilInput = document.getElementById("r-until")?.value;

    let horasReservation = 0;
    if (fromInput && untilInput) {
        const from = new Date(fromInput);
        const until = new Date(untilInput);
        const diffMs = until - from;
        horasReservation = diffMs / (1000 * 60 * 60); // convertir ms a horas
    }

    let extra = 0;
    document.querySelectorAll(".srv-chk:checked").forEach(c => extra += Number(c.dataset.price || 0));

    const precioTotal = (base * horasReservation) + extra;
    const el = document.getElementById("r-total");
    if (el) el.textContent = "$" + precioTotal.toLocaleString("es-AR");
}

// ---- Crear reserva ----
async function doReserve() {
    const title = document.getElementById("r-title")?.value.trim();
    const desc  = document.getElementById("r-desc")?.value.trim();
    let from  = document.getElementById("r-from")?.value;
    let until = document.getElementById("r-until")?.value;

    // Algunos navegadores/devices devuelven datetime-local sin segundos (YYYY-MM-DDTHH:mm)
    // El backend espera segundos. Normalizamos para añadir ":00" si faltan.
    function ensureSeconds(s) {
        if (!s) return s;
        // formato esperado mínimo: 16 chars "yyyy-MM-ddTHH:mm"
        if (s.length === 16) return s + ":00";
        return s;
    }
    from = ensureSeconds(from);
    until = ensureSeconds(until);

    if (!title || !desc || !from || !until) {
        alertBox("Completá todos los datos (título, descripción, fechas).");
        return;
    }
    if (new Date(from) >= new Date(until)) {
        alertBox("La fecha de inicio debe ser anterior a la fecha de fin.");
        return;
    }

    const selected = [...document.querySelectorAll(".srv-chk:checked")].map(c => Number(c.value));

    const dto = {
        id: null,
        title,
        description: desc,
        googleEventCode: null,
        fromDate:  from,
        untilDate: until,
        finalPrice: null,
        status: null,
        createdAt: null,
        isActive: true,
        saveToMyCalendar: !!(document.getElementById('r-save-calendar')?.checked),
        idConsumer: null,      // el backend lo resuelve por el JWT del usuario logueado
        idSpace: spaceId,
        idServicesSelec: selected
    };

    const btn = document.querySelector(".booking-box button");
    if (btn) { btn.disabled = true; btn.textContent = "Reservando..."; }

    try {
        await API.createReservation(dto);
        alertBox("¡Reserva creada con éxito! Redirigiendo a tus reservas…", "success");
        setTimeout(() => location.href = "reservations.html", 1500);
    } catch (e) {
        alertBox(e.message || "No se pudo crear la reserva.");
        if (btn) { btn.disabled = false; btn.textContent = "Reservar ahora →"; }
    }
}

// ---- Enviar comentario ----
async function submitComment() {
    const desc  = document.getElementById("c-desc")?.value.trim();
    const score = Number(document.getElementById("c-score")?.value);
    if (!desc) { alertBox("Escribí algo en tu comentario."); return; }

    const dto = { idConsumer: null, idSpace: spaceId, description: desc, score };
    try {
        await API.createComment(dto);
        alertBox("¡Comentario enviado!", "success");
        // Recarga el detalle para mostrar el nuevo comentario
        setTimeout(() => loadDetail(), 800);
    } catch (e) {
        alertBox(e.message || "No se pudo enviar el comentario. ¿Tenés una reserva en este espacio?");
    }
}

renderNav();
loadDetail();
