if (!Auth.isLogged()) location.href = "login.html";

function fmtDate(iso) {
    if (!iso) return "—";
    return new Date(iso).toLocaleString("es-AR", { dateStyle: "short", timeStyle: "short" });
}

function statusPill(status) {
    const s = (status || "TENTATIVE").toString();
    return `<span class="status-${s}">${s}</span>`;
}

// ---- Carga y renderizado ----
async function loadReservations() {
    loading("reservations");
    try {
        const list = await API.myReservations();
        renderReservations(list);
    } catch (e) {
        document.getElementById("reservations").innerHTML =
            `<div class="empty-state"><span class="icon">⚠️</span>No se pudieron cargar tus reservas.</div>`;
    }
}

function renderReservations(list) {
    const cont = document.getElementById("reservations");
    if (!list || list.length === 0) {
        cont.innerHTML = `
            <div class="empty-state">
                <span class="icon">🗓️</span>
                Todavía no tenés reservas.
                <br><a href="index.html" class="btn mt" style="display:inline-block">Buscar espacios</a>
            </div>`;
        return;
    }

    const rows = list.map(r => {
        const status     = (r.status || "TENTATIVE").toString();
        const total      = r.finalPrice != null ? "$" + Number(r.finalPrice).toLocaleString("es-AR") : "—";
        const spaceName  = r.space?.nameSpace || `Espacio #${r.space?.idSpace ?? "—"}`;
        const spaceLink  = r.space?.idSpace
            ? `<a href="space.html?id=${r.space.idSpace}">${escapeHtml(spaceName)}</a>`
            : escapeHtml(spaceName);

        const reserver = r.consumer ? (r.consumer.firstname + ' ' + r.consumer.lastname) : (r.consumerName || r.consumerFullName || '—');

        let actions = [];
        if (status === "CONFIRMED") {
            actions.push(`<button class="btn small success" onclick="pay(${r.id})">💳 Pagar</button>`);
        }
        if (status === "TENTATIVE" || status === "CONFIRMED") {
            actions.push(`<button class="btn small danger"  onclick="cancelR(${r.id})">Cancelar</button>`);
        }

        return `
            <tr>
                <td>
                    <strong>${escapeHtml(r.title || "")}</strong><br>
                    <span class="muted">${escapeHtml(r.description || "")}</span><br>
                    <small class="muted">Reservado por: ${escapeHtml(reserver)}</small>
                </td>
                <td>${spaceLink}</td>
                <td>${fmtDate(r.fromDate)}</td>
                <td>${fmtDate(r.untilDate)}</td>
                <td>${statusPill(status)}</td>
                <td>${total}</td>
                <td><div class="flex flex-wrap">${actions.join("") || '<span class="muted">—</span>'}</div></td>
            </tr>`;
    }).join("");

    cont.innerHTML = `
        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>Evento</th>
                        <th>Espacio</th>
                        <th>Desde</th>
                        <th>Hasta</th>
                        <th>Estado</th>
                        <th>Total</th>
                        <th>Acciones</th>
                    </tr>
                </thead>
                <tbody>${rows}</tbody>
            </table>
        </div>`;
}

// ---- Acciones ----
async function pay(id) {
    try {
        const res = await API.checkout(id);
        if (res && res.initPoint) {
            window.open(res.initPoint, "_blank");
            alertBox("Redirigiendo a MercadoPago en una nueva pestaña…", "success");
        } else {
            alertBox("No se pudo obtener el link de pago.");
        }
    } catch (e) {
        alertBox(e.message || "Error al iniciar el pago.");
    }
}

async function cancelR(id) {
    if (!confirm("¿Seguro que querés cancelar esta reserva?")) return;
    try {
        await API.cancelReservation(id);
        alertBox("Reserva cancelada.", "success");
        await loadReservations();
    } catch (e) {
        alertBox(e.message || "No se pudo cancelar la reserva.");
    }
}

renderNav();
loadReservations();
