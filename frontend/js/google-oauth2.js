// =============================================================
//  Google Calendar OAuth2 Integration
// =============================================================

const GoogleOAuth2 = {
    /**
     * Obtiene la URL de autorización y redirige al usuario
     */
    authorize: async function() {
        try {
            const response = await API.googleOAuth2AuthUrl();
            if (response && response.authUrl) {
                window.open(response.authUrl, '_blank', 'width=600,height=600');
                // Poll de estado cada 2 segundos durante 5 minutos
                let attempts = 0;
                const maxAttempts = 150; // 5 minutos
                const pollInterval = setInterval(async () => {
                    attempts++;
                    const status = await GoogleOAuth2.checkStatus();
                    if (status && status.connected) {
                        clearInterval(pollInterval);
                        alertBox("🎉 ¡Google Calendar conectado exitosamente!", "success");
                        // Recargar la página o actualizar UI según sea necesario
                        setTimeout(() => location.reload(), 1500);
                    }
                    if (attempts >= maxAttempts) {
                        clearInterval(pollInterval);
                        alertBox("⏱️ Tiempo agotado. Si ya conectaste tu calendario, recarga la página.", "warning");
                    }
                }, 2000);
            } else {
                alertBox("No se pudo obtener la URL de autorización");
            }
        } catch (e) {
            alertBox("Error al obtener URL de autorización: " + (e.message || e));
        }
    },

    /**
     * Verifica el estado de autorización
     */
    checkStatus: async function() {
        try {
            return await API.googleOAuth2Status();
        } catch (e) {
            console.warn("Error checking OAuth status:", e);
            return null;
        }
    },

    /**
     * Desconecta el calendario de Google
     */
    disconnect: async function() {
        if (!confirm("¿Seguro que querés desconectar tu Google Calendar?")) return;
        try {
            await API.googleOAuth2Disconnect();
            alertBox("Google Calendar desconectado", "success");
            setTimeout(() => location.reload(), 1000);
        } catch (e) {
            alertBox("Error al desconectar: " + (e.message || e));
        }
    },

    /**
     * Actualiza la UI mostrand el estado de conexión
     */
    updateUI: async function(containerId) {
        const container = document.getElementById(containerId);
        if (!container) return;

        try {
            const status = await GoogleOAuth2.checkStatus();

            if (status && status.connected) {
                container.innerHTML = `
                    <div style="background: #e8f5e9; border: 1px solid #4caf50; border-radius: 8px; padding: 16px; margin: 16px 0;">
                        <p style="color: #2e7d32; font-weight: bold; margin: 0 0 8px 0;">
                            ✅ Google Calendar Conectado
                        </p>
                        <p style="color: #555; margin: 0 0 12px 0;">
                            ${status.googleEmail ? 'Correo: ' + status.googleEmail : ''}
                        </p>
                        <button class="btn small danger" onclick="GoogleOAuth2.disconnect()">
                            🔌 Desconectar Google Calendar
                        </button>
                    </div>`;
            } else {
                container.innerHTML = `
                    <div style="background: #fff3e0; border: 1px solid #ff9800; border-radius: 8px; padding: 16px; margin: 16px 0;">
                        <p style="color: #e65100; font-weight: bold; margin: 0 0 8px 0;">
                            🔗 Conectar Google Calendar
                        </p>
                        <p style="color: #555; margin: 0 0 12px 0;">
                            Conecta tu Google Calendar para sincronizar automáticamente tus reservas.
                        </p>
                        <button class="btn small success" onclick="GoogleOAuth2.authorize()">
                            📅 Conectar Google Calendar
                        </button>
                    </div>`;
            }
        } catch (e) {
            console.error("Error updating OAuth UI:", e);
            container.innerHTML = `<p class="muted">Error cargando estado de Google Calendar</p>`;
        }
    }
};

