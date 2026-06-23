// =============================================================
//  NOTIFICACIONES - Gestión de notificaciones del sistema
// =============================================================

document.addEventListener("DOMContentLoaded", () => {
    renderNav();
    loadNotifications();
    updateUnreadCount();
});

async function loadNotifications() {
    try {
        const notifications = await API.listAllNotifications();
        renderNotifications(notifications);
    } catch (error) {
        showAlert("Error cargando notificaciones: " + error.message, "error");
    }
}

function renderNotifications(notifications) {
    const container = document.getElementById("notifications-list");
    
    if (!notifications || notifications.length === 0) {
        container.innerHTML = `<p class="empty-state">No hay notificaciones</p>`;
        return;
    }

    const unseenCount = notifications.filter(n => !n.seen && !n.isRead).length;
    if (unseenCount > 0) {
        document.getElementById("mark-all-btn").style.display = "inline-block";
    }

    container.innerHTML = notifications.map(n => `
        <div class="notification-item ${!n.seen && !n.isRead ? 'unseen' : ''}" onclick="markAsRead(${n.idNotification || n.id})">
            <div class="notification-header">
                <span class="notification-type">${getNotificationType(n.type)}</span>
                <span class="notification-time">${formatDate(n.createdAt)}</span>
            </div>
            <p class="notification-message">${n.message || n.description || 'Notificación'}</p>
            <small class="notification-detail">${n.detail || ''}</small>
        </div>
    `).join("");
}

async function markAsRead(notificationId) {
    try {
        await API.markNotificationAsRead(notificationId);
        loadNotifications();
        updateUnreadCount();
    } catch (error) {
        showAlert("Error marcando notificación: " + error.message, "error");
    }
}

async function markAllAsRead() {
    try {
        const notifications = await API.listAllNotifications();
        for (let n of notifications) {
            if (!n.seen && !n.isRead) {
                await API.markNotificationAsRead(n.id);
            }
        }
        loadNotifications();
        updateUnreadCount();
    } catch (error) {
        showAlert("Error: " + error.message, "error");
    }
}

async function updateUnreadCount() {
    try {
        const result = await API.getUnreadCount();
        const count = result?.count || result?.unreadCount || 0;
        const badge = document.getElementById("unread-count");
        if (count > 0) {
            badge.textContent = count;
            badge.style.display = "inline-block";
        } else {
            badge.style.display = "none";
        }
    } catch (error) {
        console.warn("No se pudo obtener conteo de notificaciones", error);
    }
}

function getNotificationType(type) {
    const types = {
        "RESERVATION": "📅 Reserva",
        "PAYMENT": "💳 Pago",
        "COMMENT": "💬 Comentario",
        "SPACE": "🏠 Espacio",
        "SYSTEM": "⚙️ Sistema",
        "ALERT": "⚠️ Alerta"
    };
    return types[type] || "📬 Notificación";
}

function formatDate(dateStr) {
    if (!dateStr) return "";
    const date = new Date(dateStr);
    return date.toLocaleDateString("es-AR", { 
        month: "short", 
        day: "numeric",
        hour: "2-digit",
        minute: "2-digit"
    });
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
    if (!Auth.isLogged()) {
        nav.innerHTML = `<a href="login.html">Ingresar</a>`;
        return;
    }

    const isAdmin = Auth.isAdmin();
    nav.innerHTML = `
        <a href="index.html">Home</a>
        <a href="my-spaces.html">Mis espacios</a>
        <a href="reservations.html">Reservas</a>
        <a href="notifications.html" class="active">Notificaciones</a>
        ${isAdmin ? `<a href="admin-spaces.html">Admin Espacios</a>` : ""}
        ${isAdmin ? `<a href="admin-users.html">Admin Usuarios</a>` : ""}
        <a onclick="logout()">Salir</a>
    `;
}

async function logout() {
    try {
        await API.logout();
        Auth.clear();
        location.href = "login.html";
    } catch (error) {
        Auth.clear();
        location.href = "login.html";
    }
}
