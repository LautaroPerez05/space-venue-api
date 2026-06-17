// =============================================================
//  Configuración central de la API y manejo del token JWT
// =============================================================
const API_BASE = "/api";   // proxy reverso de Nginx → Spring Boot

const TOKEN_KEY = "sv_token";
const ROLE_KEY  = "sv_role";

const Auth = {
    getToken: () => localStorage.getItem(TOKEN_KEY),
    setToken: (t) => localStorage.setItem(TOKEN_KEY, t),
    clear:    () => { localStorage.removeItem(TOKEN_KEY); localStorage.removeItem(ROLE_KEY); },
    isLogged: () => !!localStorage.getItem(TOKEN_KEY),
    getRole:  () => localStorage.getItem(ROLE_KEY) || "",
    setRole:  (r) => localStorage.setItem(ROLE_KEY, r),
    isAdmin:  () => (localStorage.getItem(ROLE_KEY) || "").includes("ADMIN"),
    // Decodifica el payload del JWT para leer el rol
    parseRoleFromJwt: (token) => {
        try {
            const raw = token.startsWith("Bearer ") ? token.substring(7) : token;
            const payload = JSON.parse(atob(raw.split(".")[1]));
            return payload.rol || payload.role || payload.authorities || "ROLE_CLIENT";
        } catch (e) { return "ROLE_CLIENT"; }
    }
};

// Wrapper genérico de fetch que agrega el header Authorization
async function apiFetch(path, { method = "GET", body = null, auth = false } = {}) {
    const headers = { "Content-Type": "application/json" };
    if (auth && Auth.getToken()) {
        const t = Auth.getToken();
        headers["Authorization"] = t.startsWith("Bearer ") ? t : `Bearer ${t}`;
    }

    const res = await fetch(`${API_BASE}${path}`, {
        method,
        headers,
        body: body !== null ? JSON.stringify(body) : null
    });

    if (res.status === 401 || res.status === 403) {
        if (auth) {
            Auth.clear();
            if (!location.pathname.endsWith("login.html")) {
                location.href = "login.html";
            }
        }
        throw new Error("No autorizado");
    }

    const text = await res.text();
    const data = text ? safeJson(text) : null;

    if (!res.ok) {
        const msg = (data && (data.error || data.message)) || text || `Error ${res.status}`;
        throw new Error(msg);
    }
    return data;
}

function safeJson(text) {
    try { return JSON.parse(text); } catch (e) { return text; }
}

// =============================================================
//  ENDPOINTS  — mapeo 1:1 con los controllers del backend
// =============================================================
const API = {
    // ---------- AUTH ----------
    login:    (username, password) => apiFetch("/auth/login",    { method: "POST", body: { username, password } }),
    register: (dto)                => apiFetch("/auth/register",  { method: "POST", body: dto }),
    logout:   ()                   => apiFetch("/auth/logout",    { method: "POST", auth: true }),

    // ---------- USUARIOS / CONSUMER ----------
    getUserById:    (id)      => apiFetch(`/usuarios/${id}`,              { auth: true }),
    listUsers:      ()        => apiFetch("/usuarios",                    { auth: true }),
    filterUsers:    (filter)  => apiFetch("/usuarios/byfields",           { method: "POST", body: filter, auth: true }),
    updateProfile:  (dto)     => apiFetch("/usuario",                     { method: "PUT",    body: dto, auth: true }),
    deleteAccount:  ()        => apiFetch("/usuario",                     { method: "DELETE", auth: true }),
    createUser:     (dto)     => apiFetch("/usuarios",                    { method: "POST", body: dto }),
    toggleUserStatus: (id)    => apiFetch(`/usuarios/${id}/status`,       { method: "PUT", auth: true }),
    deleteUser:     (id)      => apiFetch(`/usuarios/${id}`,              { method: "DELETE", auth: true }),

    // ---------- SPACES ----------
    listActiveSpaces:      ()        => apiFetch("/spaces"),
    getSpace:              (id)      => apiFetch(`/spaces/${id}`),
    filterSpaces:          (filter)  => apiFetch("/spaces/byfields",              { method: "POST", body: filter }),
    myOwnedSpaces:         ()        => apiFetch("/spaces/ownedspaces",           { auth: true }),
    filterMySpaces:        (filter)  => apiFetch("/spaces/ownedspaces/byfields",  { method: "POST", body: filter, auth: true }),
    createOwnedSpace:      (dto)     => apiFetch("/spaces/ownedspace",            { method: "POST", body: dto, auth: true }),
    updateOwnedSpace:      (id, dto) => apiFetch(`/spaces/ownedspace/${id}`,      { method: "PUT",  body: dto, auth: true }),
    deleteOwnedSpace:      (id)      => apiFetch(`/spaces/ownedspace/${id}`,      { method: "DELETE", auth: true }),
    listAllSpaces:         ()        => apiFetch("/spaces/showinactives",         { auth: true }),
    createSpace:           (dto)     => apiFetch("/spaces",                       { method: "POST", body: dto, auth: true }),
    updateSpace:           (id, dto) => apiFetch(`/spaces/${id}`,                 { method: "PUT",  body: dto, auth: true }),
    deleteSpace:           (id)      => apiFetch(`/spaces/${id}`,                 { method: "DELETE", auth: true }),

    // ---------- IMÁGENES ----------
    imagesBySpace: (id)      => apiFetch(`/spaceimages/byspaceid/${id}`),
    createImage:   (dto)     => apiFetch("/spaceimages",       { method: "POST",   body: dto, auth: true }),
    updateImage:   (id, dto) => apiFetch(`/spaceimages/${id}`, { method: "PUT",    body: dto, auth: true }),
    deleteImage:   (id)      => apiFetch(`/spaceimages/${id}`, { method: "DELETE", auth: true }),

    // ---------- SERVICIOS DEL ESPACIO (SpaceServiceItemController) ----------
    servicesOfSpace: (idSpace)     => apiFetch(`/services/space/${idSpace}`, { auth: true }),
    createService:   (dto)         => apiFetch("/services/insert",           { method: "POST",   body: dto, auth: true }),
    updateService:   (id, dto)     => apiFetch(`/services/update/${id}`,     { method: "PUT",    body: dto, auth: true }),
    deleteService:   (id, idSpace) => apiFetch(`/services/delete/${id}`,     { method: "DELETE", body: { idSpace }, auth: true }),

    // ---------- RESERVAS ----------
    listAllReservations: ()    => apiFetch("/reservations",               { auth: true }),
    myReservations:      ()    => apiFetch("/reservations/me",            { auth: true }),
    getReservation:      (id)  => apiFetch(`/reservations/${id}`,         { auth: true }),
    createReservation:   (dto) => apiFetch("/reservations",               { method: "POST", body: dto, auth: true }),
    confirmReservation:  (id)  => apiFetch(`/reservations/confirm/${id}`, { method: "PUT",  auth: true }),
    rejectReservation:   (id)  => apiFetch(`/reservations/reject/${id}`,  { method: "PUT",  auth: true }),
    cancelReservation:   (id)  => apiFetch(`/reservations/cancel/${id}`,  { method: "PUT",  auth: true }),
    completeReservation: (id)  => apiFetch(`/reservations/complete/${id}`,{ method: "PUT",  auth: true }),
    checkout:            (id)  => apiFetch(`/reservations/${id}/checkout`,{ method: "POST", auth: true }),

    // ---------- SERVICIOS SELECCIONADOS (ServiceSelectedController) ----------
    servicesSelected: (idReservation)       => apiFetch(`/servicesselected/reservation/${idReservation}`, { auth: true }),
    selectServices:   (idReservation, list) => apiFetch(`/servicesselected/insert/list/${idReservation}`, { method: "POST", body: list, auth: true }),
    deselectService:  (id)                  => apiFetch(`/servicesselected/delete/${id}`,                 { method: "DELETE", auth: true }),

    // ---------- COMENTARIOS ----------
    commentsBySpace: (idSpace) => apiFetch(`/comments/byspaceid/${idSpace}`),
    createComment:   (dto)     => apiFetch("/comments",       { method: "POST",   body: dto, auth: true }),
    deleteComment:   (id)      => apiFetch(`/comments/${id}`, { method: "DELETE", auth: true }),

    // ---------- NOTIFICACIONES ----------
    listAllNotifications:    ()             => apiFetch("/notifications",                       { auth: true }),
    getNotification:         (id)           => apiFetch(`/notifications/${id}`,                 { auth: true }),
    getUnreadCount:          ()             => apiFetch("/notifications/unread-count",         { auth: true }),
    getUserNotifications:    (id)           => apiFetch(`/notifications/consumer/${id}`,        { auth: true }),
    getUnseenNotifications:  ()             => apiFetch("/notifications/consumer/onlyunseen",  { auth: true }),
    markNotificationAsRead:  (id)           => apiFetch(`/notifications/${id}`,                 { method: "POST", auth: true })
};
