// =============================================================
//  DEBUGGING - Script para validar todos los componentes
// =============================================================

console.log("=== VALIDACIÓN DE COMPONENTES ===");

// 1. Verificar que API está cargada
if (typeof API !== 'undefined') {
    console.log("✅ API cargada correctamente");
    console.log("   Métodos de espacios:", typeof API.createOwnedSpace, typeof API.listAllSpaces);
    console.log("   Métodos de notificaciones:", typeof API.listAllNotifications);
} else {
    console.error("❌ API NO está cargada");
}

// 2. Verificar Auth
if (typeof Auth !== 'undefined') {
    console.log("✅ Auth cargada correctamente");
    console.log("   isLogged:", Auth.isLogged());
    console.log("   isAdmin:", Auth.isAdmin());
    console.log("   role:", Auth.getRole());
} else {
    console.error("❌ Auth NO está cargada");
}

// 3. Verificar funciones helper
if (typeof renderNav === 'function') {
    console.log("✅ renderNav existe");
} else {
    console.error("❌ renderNav NO existe");
}

// 4. Verificar modales
const modals = ['space-modal', 'srv-modal', 'res-modal', 'admin-spaces', 'user-modal'];
modals.forEach(m => {
    const el = document.getElementById(m);
    if (el) {
        console.log(`✅ Modal '${m}' existe`);
    } else {
        console.log(`⚠️  Modal '${m}' NO encontrado (normal si no está en esta página)`);
    }
});

// 5. Test API call
async function testApiCall() {
    try {
        if (Auth.isLogged()) {
            const spaces = await API.listActiveSpaces();
            console.log("✅ API call exitoso, espacios recibidos:", spaces.length);
        } else {
            console.log("⚠️  No autenticado, skipping API test");
        }
    } catch (error) {
        console.error("❌ Error en API call:", error.message);
    }
}

testApiCall();

// 6. Verificar navbar
setTimeout(() => {
    const nav = document.getElementById("nav-links");
    if (nav) {
        console.log("✅ Navbar existe y contiene:", nav.innerHTML.substring(0, 50) + "...");
    } else {
        console.error("❌ Navbar NO existe");
    }
}, 100);

console.log("=== FIN VALIDACIÓN ===");
