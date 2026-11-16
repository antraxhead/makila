/* ==========================================================================
   MAKILA - Controlador de Interfaz, Analítica Estadística y Usuario Local
   Arquitectura Hexagonal - Adaptador Web Primario
   ========================================================================== */

const PRESET_USERS = [
    { nombre: 'José Martín Quiroz Castro', correo: 'j.quiroz@cucei.udg.mx', rol: 'Administrador' },
    { nombre: 'Dr. José Luis Bonilla Carranza', correo: 'jl.bonilla@cucei.udg.mx', rol: 'Supervisor' },
    { nombre: 'Investigador de Laboratorio', correo: 'invitado@cucei.udg.mx', rol: 'Investigador' }
];

const STORAGE_USER_KEY = 'makila_sim_user_v1';
const STORAGE_LOG_KEY = 'makila_session_log_v1';

const appState = {
    stats: null,
    alertas: [],
    reactivos: [],
    biomateriales: [],
    insumos: [],
    equipos: [],
    movimientos: [],
    abcItems: [],
    alertFilter: 'ALL',
    currentUser: null
};

document.addEventListener('DOMContentLoaded', () => {
    initSimulatedUser();
    initNavigation();
    initGlobalSearch();
    initChat();
    initModal();
    initUserModal();
    initCatalogModals();
    refreshAllData();
});

// ================= GESTIÓN DE USUARIO SIMULADO (LOCALSTORAGE) =================
function initSimulatedUser() {
    try {
        const saved = localStorage.getItem(STORAGE_USER_KEY);
        if (saved) {
            appState.currentUser = JSON.parse(saved);
        } else {
            appState.currentUser = { ...PRESET_USERS[0] };
            localStorage.setItem(STORAGE_USER_KEY, JSON.stringify(appState.currentUser));
            logSessionActivity('Inicio de sesión automático');
        }
    } catch (e) {
        appState.currentUser = { ...PRESET_USERS[0] };
    }
    updateUserUI();
}

function getInitials(fullName) {
    if (!fullName) return 'US';
    const parts = fullName.trim().split(/\s+/).filter(p => !p.endsWith('.'));
    if (parts.length >= 2) {
        return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return fullName.substring(0, 2).toUpperCase();
}

function getShortName(fullName) {
    if (!fullName) return 'Usuario';
    const parts = fullName.trim().split(/\s+/);
    if (parts.length === 1) return parts[0];
    if (parts[0].endsWith('.') && parts.length >= 3) {
        return `${parts[0]} ${parts[2]}`;
    }
    return `${parts[0][0]}. ${parts[parts.length > 2 ? 2 : 1]}`;
}

function setActiveSimulatedUser(user) {
    appState.currentUser = { ...user };
    try {
        localStorage.setItem(STORAGE_USER_KEY, JSON.stringify(appState.currentUser));
    } catch (e) { /* Ignorar si storage no disponible */ }
    logSessionActivity(`Sesión activa: ${user.nombre} (${user.rol})`);
    updateUserUI();
}

window.logoutSimulatedUser = function() {
    const guest = { nombre: 'Sesión Invitado', correo: 'invitado@cucei.udg.mx', rol: 'Investigador' };
    setActiveSimulatedUser(guest);
    logSessionActivity('Cierre de sesión (modo invitado)');
};

function updateUserUI() {
    const u = appState.currentUser || PRESET_USERS[0];
    const initials = getInitials(u.nombre);
    const shortName = getShortName(u.nombre);

    const elHeaderInit = document.getElementById('header-user-initials');
    const elHeaderName = document.getElementById('header-user-name');
    const elChatTag = document.getElementById('chat-user-tag');

    if (elHeaderInit) elHeaderInit.textContent = initials;
    if (elHeaderName) elHeaderName.textContent = shortName;
    if (elChatTag) elChatTag.textContent = `${shortName} (${u.rol})`;

    // Actualizar contenido del modal de usuario
    const elModalInit = document.getElementById('modal-user-initials');
    const elModalFull = document.getElementById('modal-user-fullname');
    const elModalEmail = document.getElementById('modal-user-email');
    const elModalRole = document.getElementById('modal-user-role');

    if (elModalInit) elModalInit.textContent = initials;
    if (elModalFull) elModalFull.textContent = u.nombre;
    if (elModalEmail) elModalEmail.textContent = u.correo;
    if (elModalRole) elModalRole.textContent = u.rol;

    const inName = document.getElementById('user-input-name');
    const inEmail = document.getElementById('user-input-email');
    const inRole = document.getElementById('user-input-role');
    if (inName) inName.value = u.nombre;
    if (inEmail) inEmail.value = u.correo;
    if (inRole) inRole.value = u.rol;

    renderUserPresets();
    renderSessionLog();
    applyRolePermissions(u.rol);
}

function applyRolePermissions(rol) {
    const isAdminOrSup = rol === 'Administrador' || rol === 'Supervisor';
    const exportBtn = document.getElementById('btn-export-csv');

    if (exportBtn) exportBtn.style.display = isAdminOrSup ? 'flex' : 'none';
}

function renderUserPresets() {
    const grid = document.getElementById('user-presets-grid');
    if (!grid) return;
    const currentEmail = appState.currentUser ? appState.currentUser.correo : '';

    grid.innerHTML = PRESET_USERS.map((p, idx) => {
        const isSel = p.correo === currentEmail ? 'selected' : '';
        return `
            <button type="button" class="user-preset-btn ${isSel}" onclick="selectPresetUser(${idx})">
                <strong style="font-size:0.8rem; color:var(--valencia);">${escapeHtml(p.nombre)}</strong>
                <span style="font-size:0.72rem; color:var(--venice-blue);">${escapeHtml(p.rol)}</span>
            </button>
        `;
    }).join('');
}

window.selectPresetUser = function(index) {
    const p = PRESET_USERS[index];
    if (p) setActiveSimulatedUser(p);
};

function logSessionActivity(accion) {
    try {
        const raw = localStorage.getItem(STORAGE_LOG_KEY);
        const list = raw ? JSON.parse(raw) : [];
        const hora = new Date().toLocaleTimeString('es-MX', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
        const usuario = appState.currentUser ? getShortName(appState.currentUser.nombre) : 'Sistema';
        list.unshift({ hora, usuario, accion });
        if (list.length > 15) list.length = 15;
        localStorage.setItem(STORAGE_LOG_KEY, JSON.stringify(list));
    } catch (e) { /* Ignorar */ }
    renderSessionLog();
}

function renderSessionLog() {
    const logEl = document.getElementById('user-session-log');
    if (!logEl) return;
    try {
        const raw = localStorage.getItem(STORAGE_LOG_KEY);
        const list = raw ? JSON.parse(raw) : [];
        if (list.length === 0) {
            logEl.innerHTML = `<span>Sin actividad registrada.</span>`;
            return;
        }
        logEl.innerHTML = list.map(item => `
            <div class="session-log-item">
                <span><strong>${escapeHtml(item.usuario)}:</strong> ${escapeHtml(item.accion)}</span>
                <code>${escapeHtml(item.hora)}</code>
            </div>
        `).join('');
    } catch (e) {
        logEl.innerHTML = '';
    }
}

function initUserModal() {
    const modal = document.getElementById('modal-usuario');
    const closeBtn = document.getElementById('close-user-modal-btn');
    const form = document.getElementById('form-user-login');

    window.openUserModal = function() {
        updateUserUI();
        if (modal) modal.classList.add('active');
    };

    const closeModal = () => {
        if (modal) modal.classList.remove('active');
    };

    if (closeBtn) closeBtn.addEventListener('click', closeModal);
    if (modal) {
        modal.addEventListener('click', (e) => {
            if (e.target === modal) closeModal();
        });
    }

    if (form) {
        form.addEventListener('submit', (e) => {
            e.preventDefault();
            const nombre = document.getElementById('user-input-name').value.trim();
            const correo = document.getElementById('user-input-email').value.trim();
            const rol = document.getElementById('user-input-role').value;
            if (!nombre || !correo) return;
            setActiveSimulatedUser({ nombre, correo, rol });
            closeModal();
        });
    }
}

// ================= NAVEGACIÓN PRINCIPAL =================
function initNavigation() {
    const navButtons = document.querySelectorAll('.nav-btn');
    navButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const targetView = btn.getAttribute('data-view');
            switchView(targetView);
        });
    });
}

window.switchView = function(viewId) {
    const navButtons = document.querySelectorAll('.nav-btn, .nav-btn-alert');
    const viewPanes = document.querySelectorAll('.view-pane');
    const searchBar = document.getElementById('catalog-search-bar');

    navButtons.forEach(btn => {
        if (btn.getAttribute('data-view') === viewId) {
            btn.classList.add('active');
        } else {
            btn.classList.remove('active');
        }
    });

    viewPanes.forEach(pane => {
        if (pane.id === viewId) {
            pane.classList.add('active');
        } else {
            pane.classList.remove('active');
        }
    });

    const catalogViews = ['view-reactivos', 'view-biomateriales', 'view-insumos', 'view-equipos', 'view-historial'];
    if (searchBar) {
        searchBar.style.display = catalogViews.includes(viewId) ? 'flex' : 'none';
    }

    window.scrollTo({ top: 0, behavior: 'smooth' });
};

async function refreshAllData() {
    await Promise.all([
        loadDashboardStats(),
        loadAlertas(),
        loadReactivos(),
        loadBiomateriales(),
        loadInsumos(),
        loadEquipos(),
        loadHistorial()
    ]);
    renderDashboardAnalytics();
}

// ================= BÚSQUEDA EN CATÁLOGOS =================
function initGlobalSearch() {
    const searchInput = document.getElementById('global-search-input');
    const clearBtn = document.getElementById('btn-clear-search');

    if (!searchInput) return;

    searchInput.addEventListener('input', (e) => {
        const query = e.target.value.toLowerCase().trim();
        if (clearBtn) {
            clearBtn.style.display = query ? 'inline-block' : 'none';
        }
        filterTables(query);
    });

    if (clearBtn) {
        clearBtn.addEventListener('click', () => {
            searchInput.value = '';
            clearBtn.style.display = 'none';
            filterTables('');
            searchInput.focus();
        });
    }
}

function filterTables(query) {
    const tableIds = ['tbody-reactivos', 'tbody-biomateriales', 'tbody-insumos', 'tbody-equipos', 'tbody-historial'];
    tableIds.forEach(id => {
        const tbody = document.getElementById(id);
        if (!tbody) return;
        const rows = tbody.querySelectorAll('tr');
        rows.forEach(tr => {
            if (!query) {
                tr.style.display = '';
                return;
            }
            const text = tr.textContent.toLowerCase();
            tr.style.display = text.includes(query) ? '' : 'none';
        });
    });
}

// ================= CARGA DE DATOS REST =================
async function loadDashboardStats() {
    try {
        const res = await fetch('/api/dashboard/stats');
        if (!res.ok) return;
        appState.stats = await res.json();
    } catch (e) {
        console.error('Error cargando stats:', e);
    }
}

async function loadAlertas() {
    try {
        const res = await fetch('/api/dashboard/alertas');
        if (!res.ok) return;
        appState.alertas = await res.json();
        updateAlertNotificationButton(appState.alertas);
        updateAlertCenterSummary(appState.alertas);
        renderAlertList();
    } catch (e) {
        console.error('Error cargando alertas:', e);
    }
}

function updateAlertNotificationButton(alertas) {
    const btn = document.getElementById('btn-nav-alertas');
    const badge = document.getElementById('nav-alert-count');
    if (!btn || !badge) return;

    const totalAlertas = alertas.length;
    const criticas = alertas.filter(a => a.nivel === 'CRITICO').length;
    badge.textContent = totalAlertas;

    if (criticas >= 1 || totalAlertas >= 1) {
        btn.classList.add('has-emergency');
    } else {
        btn.classList.remove('has-emergency');
    }
}

function updateAlertCenterSummary(alertas) {
    const criticas = alertas.filter(a => a.nivel === 'CRITICO').length;
    const advertencias = alertas.filter(a => a.nivel !== 'CRITICO').length;
    const reactivos = alertas.filter(a => a.categoria === 'REACTIVO').length;
    const equipos = alertas.filter(a => a.categoria === 'EQUIPO').length;
    const insumos = alertas.filter(a => a.categoria === 'INSUMO_GENERAL').length;

    const elCrit = document.getElementById('alert-count-critical');
    const elWarn = document.getElementById('alert-count-warning');
    const elReact = document.getElementById('alert-count-reactivos');
    const elEquip = document.getElementById('alert-count-equipos');
    const elInsumos = document.getElementById('alert-count-insumos');

    if (elCrit) elCrit.textContent = criticas;
    if (elWarn) elWarn.textContent = advertencias;
    if (elReact) elReact.textContent = reactivos;
    if (elEquip) elEquip.textContent = equipos;
    if (elInsumos) elInsumos.textContent = insumos;
}

window.filterAlertList = function(filterValue, btnElement) {
    appState.alertFilter = filterValue;
    document.querySelectorAll('.alert-filter-btn').forEach(b => b.classList.remove('active'));
    if (btnElement) btnElement.classList.add('active');
    renderAlertList();
};

function renderAlertList() {
    const container = document.getElementById('alerts-container');
    if (!container) return;

    const filter = appState.alertFilter;
    let filtered = appState.alertas;

    if (filter === 'CRITICO' || filter === 'ADVERTENCIA') {
        filtered = appState.alertas.filter(a => a.nivel === filter);
    } else if (filter === 'REACTIVO' || filter === 'EQUIPO' || filter === 'INSUMO_GENERAL') {
        filtered = appState.alertas.filter(a => a.categoria === filter);
    }

    container.innerHTML = '';

    if (filtered.length === 0) {
        container.innerHTML = `
            <div class="alert-banner success">
                <div class="alert-info">
                    <h4>Sin alertas activas</h4>
                    <p>Todos los parámetros de conservación y calibración están dentro de rango.</p>
                </div>
                <span class="badge-tag">OPTIMO</span>
            </div>
        `;
        return;
    }

    filtered.forEach(a => {
        const isCritical = a.nivel === 'CRITICO';
        const banner = document.createElement('div');
        banner.className = `alert-banner ${isCritical ? '' : 'warning'}`;

        const consultaTexto = a.categoria === 'EQUIPO'
            ? `Consultar estado y calibración del equipo`
            : `Consultar stock y estado de ${a.titulo.replace(/^.*:\s*/, '')}`;

        const actionMovBtn = (a.categoria === 'REACTIVO' || a.categoria === 'INSUMO_GENERAL')
            ? `<button class="btn-alert-action" onclick="openPrefilledModal('${escapeHtml(a.categoria)}', ${a.referenciaId})">Registrar Entrada</button>`
            : '';

        banner.innerHTML = `
            <div class="alert-info">
                <h4>${isCritical ? '[CRÍTICA]' : '[PREVENTIVA]'} ${escapeHtml(a.titulo)}</h4>
                <p>${escapeHtml(a.descripcion)}</p>
                <p style="margin-top:0.25rem;"><strong>Acción:</strong> ${escapeHtml(a.recomendacion)}</p>
            </div>
            <div class="alert-card-actions">
                <span class="badge-tag">${escapeHtml(a.categoria)} #${a.referenciaId}</span>
                <button class="btn-alert-action" onclick="consultarAlertaConIA('${escapeHtml(consultaTexto)}')">Hablar con Makila</button>
                ${actionMovBtn}
            </div>
        `;
        container.appendChild(banner);
    });
}

window.consultarAlertaConIA = function(prompt) {
    switchView('view-chat');
    appendUserMessage(prompt);
    sendToAgent(prompt);
};

window.openPrefilledModal = function(categoria, id) {
    openModalMovimiento();
    const catSelect = document.getElementById('mov-categoria');
    const idInput = document.getElementById('mov-id');
    if (catSelect) catSelect.value = categoria;
    if (idInput) idInput.value = id;
};

async function loadReactivos() {
    const tbody = document.getElementById('tbody-reactivos');
    try {
        const res = await fetch('/api/reactivos');
        if (!res.ok) return;
        const data = await res.json();
        appState.reactivos = data;

        if (!tbody) return;
        tbody.innerHTML = '';
        data.forEach(r => {
            const tr = document.createElement('tr');
            const alertBadges = [];
            if (r.tempMaxCelsius && r.tempMaxCelsius <= 30) {
                alertBadges.push(`<span class="badge-sensible">Máx ${r.tempMaxCelsius}°C</span>`);
            }
            if (r.requiereAislamientoLuz) {
                alertBadges.push(`<span class="badge-sensible">Aislamiento UV</span>`);
            }

            tr.innerHTML = `
                <td><strong>#${r.id}</strong></td>
                <td><strong>${escapeHtml(r.nombre)}</strong></td>
                <td><code>${escapeHtml(r.formula)}</code></td>
                <td><strong>${r.cantidad}</strong> ${escapeHtml(r.unidad)}</td>
                <td>${escapeHtml(r.ubicacion)}</td>
                <td>${escapeHtml(r.fechaCaducidad)}</td>
                <td>
                    ${escapeHtml(r.estadoSeguridad)}<br>
                    ${alertBadges.join(' ')}
                </td>
            `;
            tbody.appendChild(tr);
        });
    } catch (e) {
        console.error('Error cargando reactivos:', e);
    }
}

async function loadBiomateriales() {
    const tbody = document.getElementById('tbody-biomateriales');
    try {
        const res = await fetch('/api/biomateriales');
        if (!res.ok) return;
        const data = await res.json();
        appState.biomateriales = data;

        if (!tbody) return;
        tbody.innerHTML = '';
        data.forEach(b => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><strong>#${b.id}</strong></td>
                <td><span class="badge-tag">${escapeHtml(b.tipoMaterial)}</span></td>
                <td><strong>${escapeHtml(b.nombre)}</strong></td>
                <td><code>${escapeHtml(b.lote)}</code></td>
                <td><strong>${b.cantidadDisponible}</strong> ${escapeHtml(b.unidad)}</td>
                <td><small>${escapeHtml(b.especificacionesTecnicas)}</small></td>
            `;
            tbody.appendChild(tr);
        });
    } catch (e) {
        console.error('Error cargando biomateriales:', e);
    }
}

async function loadInsumos() {
    const tbody = document.getElementById('tbody-insumos');
    try {
        const res = await fetch('/api/insumos');
        if (!res.ok) return;
        const data = await res.json();
        appState.insumos = data;

        if (!tbody) return;
        tbody.innerHTML = '';
        data.forEach(ins => {
            const tr = document.createElement('tr');
            const stockBajo = ins.stockMinimo != null && ins.cantidad <= ins.stockMinimo;
            const stockBadge = stockBajo ? `<span class="badge-sensible" style="margin-left:6px;">BAJO</span>` : '';

            tr.innerHTML = `
                <td><strong>#${ins.id}</strong></td>
                <td><span class="badge-tag">${escapeHtml(ins.categoriaInsumo)}</span></td>
                <td><strong>${escapeHtml(ins.nombre)}</strong></td>
                <td><strong>${ins.cantidad}</strong> ${escapeHtml(ins.unidad)}${stockBadge}</td>
                <td>${ins.stockMinimo ?? 0} ${escapeHtml(ins.unidad)}</td>
                <td>${escapeHtml(ins.ubicacion)}</td>
                <td><small>${escapeHtml(ins.especificaciones)}</small></td>
            `;
            tbody.appendChild(tr);
        });
    } catch (e) {
        console.error('Error cargando insumos:', e);
    }
}

async function loadEquipos() {
    const tbody = document.getElementById('tbody-equipos');
    try {
        const res = await fetch('/api/equipos');
        if (!res.ok) return;
        const data = await res.json();
        appState.equipos = data;

        if (!tbody) return;
        tbody.innerHTML = '';
        data.forEach(e => {
            const tr = document.createElement('tr');
            let badgeClass = 'operativo';
            if (e.estadoActual === 'EN_MANTENIMIENTO') badgeClass = 'mantenimiento';
            if (e.estadoActual === 'FUERA_DE_SERVICIO') badgeClass = 'fuera';

            tr.innerHTML = `
                <td><strong>#${e.id}</strong></td>
                <td><strong>${escapeHtml(e.nombreEquipo)}</strong></td>
                <td><span class="badge-status ${badgeClass}">${escapeHtml(e.estadoActual)}</span></td>
                <td>${escapeHtml(e.fechaProximaCalibracion)}</td>
                <td>${escapeHtml(e.ubicacion)}</td>
                <td>${escapeHtml(e.responsable)}</td>
            `;
            tbody.appendChild(tr);
        });
    } catch (e) {
        console.error('Error cargando equipos:', e);
    }
}

async function loadHistorial() {
    const tbody = document.getElementById('tbody-historial');
    try {
        const res = await fetch('/api/movimientos');
        if (!res.ok) return;
        const data = await res.json();
        appState.movimientos = data;

        if (!tbody) return;
        tbody.innerHTML = '';
        data.forEach(m => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><strong>#${m.id}</strong></td>
                <td><span class="badge-tag">${escapeHtml(m.tipoMovimiento)}</span></td>
                <td>${escapeHtml(m.categoria)}</td>
                <td>${escapeHtml(m.itemNombre)} (ID: ${m.itemId})</td>
                <td><strong>${m.cantidad}</strong> ${escapeHtml(m.unidad)}</td>
                <td><code>${escapeHtml(m.experimentoOLote)}</code></td>
                <td>${escapeHtml(m.usuarioResponsable)}</td>
                <td><small>${escapeHtml(m.fechaHora)}</small></td>
            `;
            tbody.appendChild(tr);
        });
    } catch (e) {
        console.error('Error cargando historial:', e);
    }
}

// ================= ANALÍTICA ESTADÍSTICA & ADMINISTRACIÓN (DASHBOARD) =================
function renderDashboardAnalytics() {
    computeABCAnalysis();
    renderAdminKPIs();
    renderPieChart1();
    renderPieChart2();
    renderParetoChart();
    renderFlowChart();
    renderDescriptiveStatsTable();
    renderABCAndROPTable();
}

function renderAdminKPIs() {
    const totalEquipos = appState.equipos.length;
    const operativos = appState.equipos.filter(e => e.estadoActual === 'OPERATIVO').length;
    const totalMat = appState.reactivos.length + appState.biomateriales.length + appState.insumos.length;
    const matConStock = appState.reactivos.filter(r => r.cantidad > 0).length +
                        appState.biomateriales.filter(b => b.cantidadDisponible > 0).length +
                        appState.insumos.filter(i => i.cantidad > 0).length;

    const dispEquipos = totalEquipos > 0 ? (operativos / totalEquipos) : 1;
    const dispStock = totalMat > 0 ? (matConStock / totalMat) : 1;
    const saludGlobal = (((dispEquipos + dispStock) / 2) * 100).toFixed(1);

    const elSalud = document.getElementById('stat-salud-pct');
    const elSaludMeta = document.getElementById('stat-salud-meta');
    if (elSalud) elSalud.textContent = `${saludGlobal}%`;
    if (elSaludMeta) elSaludMeta.textContent = `${operativos}/${totalEquipos} equipos • ${matConStock}/${totalMat} materiales`;

    const volEntrada = appState.movimientos
        .filter(m => m.tipoMovimiento === 'ENTRADA')
        .reduce((acc, m) => acc + (m.cantidad || 0), 0);
    const volSalida = appState.movimientos
        .filter(m => m.tipoMovimiento === 'SALIDA' || m.tipoMovimiento === 'CONSUMO')
        .reduce((acc, m) => acc + (m.cantidad || 0), 0);

    const ratio = volSalida > 0 ? (volEntrada / volSalida).toFixed(2) : (volEntrada > 0 ? 'MAX' : '1.00');
    const estadoFlujo = volSalida === 0 ? 'Sin egresos' : (volEntrada >= volSalida ? 'Superávit' : 'Consumo neto');
    const elRatio = document.getElementById('stat-rotacion-ratio');
    const elRatioMeta = document.getElementById('stat-rotacion-meta');
    if (elRatio) elRatio.textContent = `${ratio}x`;
    if (elRatioMeta) elRatioMeta.textContent = `${estadoFlujo} (${volEntrada.toFixed(0)} in / ${volSalida.toFixed(0)} out)`;

    const totalReactivos = appState.reactivos.length;
    const criticosQuimicos = appState.reactivos.filter(r =>
        (r.tempMaxCelsius !== null && r.tempMaxCelsius <= 30) || r.requiereAislamientoLuz
    ).length;
    const riesgoPct = totalReactivos > 0 ? ((criticosQuimicos / totalReactivos) * 100).toFixed(1) : '0.0';
    const elRiesgo = document.getElementById('stat-riesgo-pct');
    const elRiesgoMeta = document.getElementById('stat-riesgo-meta');
    if (elRiesgo) elRiesgo.textContent = `${riesgoPct}%`;
    if (elRiesgoMeta) elRiesgoMeta.textContent = `${criticosQuimicos} de ${totalReactivos} reactivos`;

    const allStocks = [
        ...appState.reactivos.map(r => r.cantidad || 0),
        ...appState.biomateriales.map(b => b.cantidadDisponible || 0),
        ...appState.insumos.map(i => i.cantidad || 0)
    ];
    const stats = calcDescriptiveStats(allStocks);
    const elCv = document.getElementById('stat-cv-pct');
    const elCvMeta = document.getElementById('stat-cv-meta');
    if (elCv) elCv.textContent = `${stats.cv.toFixed(1)}%`;
    if (elCvMeta) elCvMeta.textContent = `Media: ${stats.mean.toFixed(1)} | s: ${stats.stdDev.toFixed(1)}`;
}

// ----------------- GRÁFICAS DE PASTEL (SVG NATIVO) -----------------
window.renderPieChart1 = function() {
    const modeSelect = document.getElementById('pie1-mode');
    const mode = modeSelect ? modeSelect.value : 'categorias';
    let slices = [];

    if (mode === 'categorias') {
        slices = [
            { label: 'Reactivos Químicos', value: appState.reactivos.length, color: '#085979' },
            { label: 'Biomateriales', value: appState.biomateriales.length, color: '#64BABE' },
            { label: 'Insumos Generales', value: appState.insumos.length, color: '#4FA8AD' },
            { label: 'Equipos', value: appState.equipos.length, color: '#343434' }
        ];
    } else if (mode === 'movimientos') {
        const cntEnt = appState.movimientos.filter(m => m.tipoMovimiento === 'ENTRADA').length;
        const cntSal = appState.movimientos.filter(m => m.tipoMovimiento === 'SALIDA').length;
        const cntCon = appState.movimientos.filter(m => m.tipoMovimiento === 'CONSUMO').length;
        slices = [
            { label: 'Entradas', value: cntEnt, color: '#085979' },
            { label: 'Salidas', value: cntSal, color: '#F3505A' },
            { label: 'Consumos', value: cntCon, color: '#64BABE' }
        ];
    } else if (mode === 'abc') {
        const countA = appState.abcItems.filter(i => i.clase === 'A').length;
        const countB = appState.abcItems.filter(i => i.clase === 'B').length;
        const countC = appState.abcItems.filter(i => i.clase === 'C').length;
        slices = [
            { label: 'Clase A', value: countA, color: '#F3505A' },
            { label: 'Clase B', value: countB, color: '#085979' },
            { label: 'Clase C', value: countC, color: '#64BABE' }
        ];
    }

    drawSVGPieChart('pie1-svg-wrap', 'pie1-legend', slices, 'TOTAL');
};

window.renderPieChart2 = function() {
    const modeSelect = document.getElementById('pie2-mode');
    const mode = modeSelect ? modeSelect.value : 'riesgos';
    let slices = [];

    if (mode === 'riesgos') {
        let termo = 0, foto = 0, estandar = 0;
        appState.reactivos.forEach(r => {
            if (r.requiereAislamientoLuz) foto++;
            else if (r.tempMaxCelsius !== null && r.tempMaxCelsius <= 30) termo++;
            else estandar++;
        });
        slices = [
            { label: 'Termolábiles (<= 30°C)', value: termo, color: '#F3505A' },
            { label: 'Fotosensibles (UV)', value: foto, color: '#085979' },
            { label: 'Estándar', value: estandar, color: '#64BABE' }
        ];
    } else if (mode === 'equipos') {
        const op = appState.equipos.filter(e => e.estadoActual === 'OPERATIVO').length;
        const mant = appState.equipos.filter(e => e.estadoActual === 'EN_MANTENIMIENTO').length;
        const fuera = appState.equipos.filter(e => e.estadoActual === 'FUERA_DE_SERVICIO').length;
        slices = [
            { label: 'Operativos', value: op, color: '#64BABE' },
            { label: 'Mantenimiento', value: mant, color: '#F3505A' },
            { label: 'Fuera de Servicio', value: fuera, color: '#343434' }
        ];
    } else if (mode === 'alertas') {
        const crit = appState.alertas.filter(a => a.nivel === 'CRITICO').length;
        const adv = appState.alertas.filter(a => a.nivel !== 'CRITICO').length;
        slices = [
            { label: 'Críticas', value: crit, color: '#F3505A' },
            { label: 'Preventivas', value: adv, color: '#085979' }
        ];
    }

    drawSVGPieChart('pie2-svg-wrap', 'pie2-legend', slices, 'TOTAL');
};

function drawSVGPieChart(svgWrapId, legendId, rawSlices, centerTag) {
    const svgWrap = document.getElementById(svgWrapId);
    const legendEl = document.getElementById(legendId);
    if (!svgWrap || !legendEl) return;

    const slices = rawSlices.filter(s => s.value > 0);
    const total = slices.reduce((sum, s) => sum + s.value, 0);

    if (total === 0) {
        svgWrap.innerHTML = `<div class="pie-center-readout"><span class="pie-center-num">0</span><span class="pie-center-lbl">SIN DATOS</span></div>`;
        legendEl.innerHTML = `<div class="legend-item"><span>Sin registros.</span></div>`;
        return;
    }

    const cx = 105, cy = 105, r = 92;
    let pathsHtml = '';
    let currentAngle = 0;

    if (slices.length === 1) {
        const s = slices[0];
        pathsHtml = `<circle cx="${cx}" cy="${cy}" r="${r}" fill="${s.color}" class="pie-slice" data-label="${escapeHtml(s.label)}" data-val="${s.value}" data-pct="100.0" />`;
    } else {
        slices.forEach(s => {
            const sliceAngle = (s.value / total) * 360;
            const endAngle = currentAngle + sliceAngle;
            const d = describePieSlice(cx, cy, r, currentAngle, endAngle);
            const pct = ((s.value / total) * 100).toFixed(1);
            pathsHtml += `<path d="${d}" fill="${s.color}" stroke="#ECEBEA" stroke-width="2" class="pie-slice" data-label="${escapeHtml(s.label)}" data-val="${s.value}" data-pct="${pct}" />`;
            currentAngle = endAngle;
        });
    }

    svgWrap.innerHTML = `
        <svg viewBox="0 0 210 210" aria-label="Gráfica de pastel">
            ${pathsHtml}
        </svg>
        <div class="pie-center-readout">
            <span class="pie-center-num" id="${svgWrapId}-num">${total}</span>
            <span class="pie-center-lbl" id="${svgWrapId}-lbl">${centerTag}</span>
        </div>
    `;

    const numEl = document.getElementById(`${svgWrapId}-num`);
    const lblEl = document.getElementById(`${svgWrapId}-lbl`);
    svgWrap.querySelectorAll('.pie-slice').forEach(el => {
        el.addEventListener('mouseenter', () => {
            if (numEl) numEl.textContent = `${el.getAttribute('data-pct')}%`;
            if (lblEl) lblEl.textContent = `${el.getAttribute('data-val')} uds`;
        });
        el.addEventListener('mouseleave', () => {
            if (numEl) numEl.textContent = total;
            if (lblEl) lblEl.textContent = centerTag;
        });
    });

    legendEl.innerHTML = rawSlices.map(s => {
        const pct = total > 0 ? ((s.value / total) * 100).toFixed(1) : '0.0';
        return `
            <div class="legend-item">
                <div class="legend-left">
                    <span class="legend-swatch" style="background-color: ${s.color};"></span>
                    <span class="legend-name">${escapeHtml(s.label)}</span>
                </div>
                <span class="legend-val">${s.value} (${pct}%)</span>
            </div>
        `;
    }).join('');
}

function polarToCartesian(cx, cy, radius, angleInDegrees) {
    const angleInRadians = (angleInDegrees - 90) * Math.PI / 180.0;
    return {
        x: cx + (radius * Math.cos(angleInRadians)),
        y: cy + (radius * Math.sin(angleInRadians))
    };
}

function describePieSlice(cx, cy, radius, startAngle, endAngle) {
    const start = polarToCartesian(cx, cy, radius, endAngle);
    const end = polarToCartesian(cx, cy, radius, startAngle);
    const largeArcFlag = endAngle - startAngle <= 180 ? '0' : '1';
    return [
        'M', cx, cy,
        'L', start.x, start.y,
        'A', radius, radius, 0, largeArcFlag, 0, end.x, end.y,
        'Z'
    ].join(' ');
}

// ----------------- ANÁLISIS ABC / PARETO -----------------
function computeABCAnalysis() {
    const items = [];

    appState.reactivos.forEach(r => {
        const movs = appState.movimientos.filter(m => m.categoria === 'REACTIVO' && m.itemId === r.id);
        const totalMovQty = movs.reduce((acc, m) => acc + (m.cantidad || 0), 0);
        const isCritical = (r.tempMaxCelsius !== null && r.tempMaxCelsius <= 30) || r.requiereAislamientoLuz;
        const score = (r.cantidad || 0) + totalMovQty + (isCritical ? 150 : 0);

        items.push({
            categoria: 'REACTIVO',
            id: r.id,
            nombre: r.nombre,
            stock: r.cantidad || 0,
            unidad: r.unidad,
            movimientosCount: movs.length,
            consumoTotal: movs.filter(m => m.tipoMovimiento !== 'ENTRADA').reduce((a, m) => a + (m.cantidad || 0), 0),
            score: score
        });
    });

    appState.biomateriales.forEach(b => {
        const movs = appState.movimientos.filter(m => m.categoria === 'BIOMATERIAL' && m.itemId === b.id);
        const totalMovQty = movs.reduce((acc, m) => acc + (m.cantidad || 0), 0);
        const score = (b.cantidadDisponible || 0) + totalMovQty;

        items.push({
            categoria: 'BIOMATERIAL',
            id: b.id,
            nombre: b.nombre,
            stock: b.cantidadDisponible || 0,
            unidad: b.unidad,
            movimientosCount: movs.length,
            consumoTotal: movs.filter(m => m.tipoMovimiento !== 'ENTRADA').reduce((a, m) => a + (m.cantidad || 0), 0),
            score: score
        });
    });

    appState.insumos.forEach(ins => {
        const movs = appState.movimientos.filter(m => m.categoria === 'INSUMO_GENERAL' && m.itemId === ins.id);
        const totalMovQty = movs.reduce((acc, m) => acc + (m.cantidad || 0), 0);
        const score = (ins.cantidad || 0) + totalMovQty;

        items.push({
            categoria: 'INSUMO_GENERAL',
            id: ins.id,
            nombre: ins.nombre,
            stock: ins.cantidad || 0,
            unidad: ins.unidad,
            movimientosCount: movs.length,
            consumoTotal: movs.filter(m => m.tipoMovimiento !== 'ENTRADA').reduce((a, m) => a + (m.cantidad || 0), 0),
            score: score
        });
    });

    items.sort((a, b) => b.score - a.score);

    const totalScore = items.reduce((acc, i) => acc + i.score, 0) || 1;
    let acumulado = 0;

    items.forEach((item, index) => {
        const pct = (item.score / totalScore) * 100;
        acumulado += pct;
        item.participacionPct = pct;
        item.acumuladoPct = Math.min(100, acumulado);

        if (index === 0 || item.acumuladoPct <= 80) {
            item.clase = 'A';
        } else if (item.acumuladoPct <= 95) {
            item.clase = 'B';
        } else {
            item.clase = 'C';
        }

        const egresosCount = appState.movimientos.filter(m => m.categoria === item.categoria && m.itemId === item.id && m.tipoMovimiento !== 'ENTRADA').length;
        const avgConsumo = egresosCount > 0 ? (item.consumoTotal / egresosCount) : (item.stock * 0.15);
        item.rop = Math.max(5, Math.ceil(avgConsumo * 1.5));
        item.requiereReorden = item.stock <= item.rop;
    });

    appState.abcItems = items;
}

function renderParetoChart() {
    const wrap = document.getElementById('pareto-chart-wrap');
    const summaryEl = document.getElementById('abc-summary-bars');
    if (!wrap || !summaryEl) return;

    const items = appState.abcItems.slice(0, 6);
    if (items.length === 0) {
        wrap.innerHTML = `<p style="font-size:0.85rem; color:#6C6963;">Sin datos.</p>`;
        return;
    }

    const maxScore = Math.max(...items.map(i => i.score), 1);

    let barsHtml = '';
    items.forEach(item => {
        const widthPct = ((item.score / maxScore) * 100).toFixed(1);
        const color = item.clase === 'A' ? '#F3505A' : (item.clase === 'B' ? '#085979' : '#64BABE');
        barsHtml += `
            <div class="flow-bar-row">
                <div class="flow-bar-label">
                    <span>[${item.clase}] ${escapeHtml(item.nombre)}</span>
                    <span style="font-family: var(--font-mono); color: var(--venice-blue);">${item.participacionPct.toFixed(1)}% (Acum: ${item.acumuladoPct.toFixed(1)}%)</span>
                </div>
                <div class="flow-bar-track">
                    <div class="flow-bar-fill" style="width: ${widthPct}%; background: ${color};"></div>
                </div>
            </div>
        `;
    });
    wrap.innerHTML = barsHtml;

    const cntA = appState.abcItems.filter(i => i.clase === 'A').length;
    const cntB = appState.abcItems.filter(i => i.clase === 'B').length;
    const cntC = appState.abcItems.filter(i => i.clase === 'C').length;

    summaryEl.innerHTML = `
        <div class="abc-pill-card clase-a">
            <span class="kpi-label">CLASE A</span>
            <div style="font-size:1.25rem; font-weight:800; color:var(--carnation);">${cntA} ítems</div>
        </div>
        <div class="abc-pill-card clase-b">
            <span class="kpi-label">CLASE B</span>
            <div style="font-size:1.25rem; font-weight:800; color:var(--venice-blue);">${cntB} ítems</div>
        </div>
        <div class="abc-pill-card clase-c">
            <span class="kpi-label">CLASE C</span>
            <div style="font-size:1.25rem; font-weight:800; color:var(--fountain-blue-hover);">${cntC} ítems</div>
        </div>
    `;
}

// ----------------- BALANCE DE FLUJOS -----------------
function renderFlowChart() {
    const wrap = document.getElementById('flow-chart-wrap');
    const footerEl = document.getElementById('flow-metrics-footer');
    if (!wrap || !footerEl) return;

    const movs = appState.movimientos;
    const ent = movs.filter(m => m.tipoMovimiento === 'ENTRADA');
    const sal = movs.filter(m => m.tipoMovimiento === 'SALIDA');
    const con = movs.filter(m => m.tipoMovimiento === 'CONSUMO');

    const volEnt = ent.reduce((a, m) => a + (m.cantidad || 0), 0);
    const volSal = sal.reduce((a, m) => a + (m.cantidad || 0), 0);
    const volCon = con.reduce((a, m) => a + (m.cantidad || 0), 0);
    const maxVol = Math.max(volEnt, volSal, volCon, 1);

    const rows = [
        { label: `Entradas (${ent.length})`, vol: volEnt, color: '#085979' },
        { label: `Salidas (${sal.length})`, vol: volSal, color: '#F3505A' },
        { label: `Consumos (${con.length})`, vol: volCon, color: '#64BABE' }
    ];

    wrap.innerHTML = rows.map(r => {
        const w = ((r.vol / maxVol) * 100).toFixed(1);
        return `
            <div class="flow-bar-row">
                <div class="flow-bar-label">
                    <span>${r.label}</span>
                    <span style="font-family: var(--font-mono);">${r.vol.toFixed(2)} uds</span>
                </div>
                <div class="flow-bar-track">
                    <div class="flow-bar-fill" style="width: ${w}%; background: ${r.color};"></div>
                </div>
            </div>
        `;
    }).join('');

    const neto = volEnt - (volSal + volCon);
    footerEl.innerHTML = `
        <div><strong>Ingresos:</strong><br><span style="color:var(--venice-blue); font-weight:700;">+${volEnt.toFixed(2)} uds</span></div>
        <div><strong>Egresos:</strong><br><span style="color:var(--carnation); font-weight:700;">-${(volSal + volCon).toFixed(2)} uds</span></div>
        <div><strong>Balance Neto:</strong><br><span style="font-weight:800;">${neto >= 0 ? '+' : ''}${neto.toFixed(2)} uds</span></div>
    `;
}

// ----------------- ESTADÍSTICA DESCRIPTIVA -----------------
function calcDescriptiveStats(arr) {
    if (!arr || arr.length === 0) {
        return { n: 0, sum: 0, mean: 0, median: 0, min: 0, max: 0, stdDev: 0, cv: 0 };
    }
    const sorted = [...arr].sort((a, b) => a - b);
    const n = sorted.length;
    const sum = sorted.reduce((acc, v) => acc + v, 0);
    const mean = sum / n;
    const mid = Math.floor(n / 2);
    const median = n % 2 !== 0 ? sorted[mid] : (sorted[mid - 1] + sorted[mid]) / 2;
    const min = sorted[0];
    const max = sorted[n - 1];

    let variance = 0;
    if (n > 1) {
        const sqDiffs = sorted.reduce((acc, v) => acc + Math.pow(v - mean, 2), 0);
        variance = sqDiffs / (n - 1);
    }
    const stdDev = Math.sqrt(variance);
    const cv = mean > 0 ? (stdDev / mean) * 100 : 0;

    return { n, sum, mean, median, min, max, stdDev, cv };
}

function renderDescriptiveStatsTable() {
    const tbody = document.getElementById('tbody-descriptive-stats');
    if (!tbody) return;

    const series = [
        { name: 'Reactivos Químicos', values: appState.reactivos.map(r => r.cantidad || 0) },
        { name: 'Biomateriales & Aleaciones', values: appState.biomateriales.map(b => b.cantidadDisponible || 0) },
        { name: 'Insumos Generales y EPP', values: appState.insumos.map(i => i.cantidad || 0) },
        { name: 'Entradas', values: appState.movimientos.filter(m => m.tipoMovimiento === 'ENTRADA').map(m => m.cantidad || 0) },
        { name: 'Salidas / Consumos', values: appState.movimientos.filter(m => m.tipoMovimiento !== 'ENTRADA').map(m => m.cantidad || 0) }
    ];

    tbody.innerHTML = series.map(s => {
        const st = calcDescriptiveStats(s.values);
        return `
            <tr>
                <td><strong>${escapeHtml(s.name)}</strong></td>
                <td><code>${st.n}</code></td>
                <td><strong>${st.sum.toFixed(2)}</strong></td>
                <td>${st.mean.toFixed(2)}</td>
                <td>${st.median.toFixed(2)}</td>
                <td>${st.min.toFixed(2)}</td>
                <td>${st.max.toFixed(2)}</td>
                <td>${st.stdDev.toFixed(2)}</td>
                <td><span class="badge-tag">${st.cv.toFixed(1)}%</span></td>
            </tr>
        `;
    }).join('');
}

// ----------------- MATRIZ ABC Y PUNTO DE REORDEN (ROP) -----------------
function renderABCAndROPTable() {
    const tbody = document.getElementById('tbody-abc-rop');
    if (!tbody) return;

    tbody.innerHTML = appState.abcItems.map(item => {
        const claseBadge = item.clase === 'A' ? 'badge-sensible' : 'badge-tag';
        const ropBadge = item.requiereReorden
            ? `<span class="badge-sensible">REORDENAR</span>`
            : `<span class="badge-status operativo">OPTIMO</span>`;

        return `
            <tr>
                <td><span class="badge-tag">${item.categoria}</span></td>
                <td><strong>${escapeHtml(item.nombre)}</strong></td>
                <td><strong>${item.stock}</strong> ${escapeHtml(item.unidad)}</td>
                <td>${item.movimientosCount}</td>
                <td>${item.participacionPct.toFixed(1)}%</td>
                <td><code>${item.acumuladoPct.toFixed(1)}%</code></td>
                <td><span class="${claseBadge}">${item.clase}</span></td>
                <td><strong>${item.rop}</strong> ${escapeHtml(item.unidad)}</td>
                <td>${ropBadge}</td>
            </tr>
        `;
    }).join('');
}

// ----------------- EXPORTACIÓN DE REPORTE CSV -----------------
window.exportarReporteCSV = function() {
    const fecha = new Date().toISOString().split('T')[0];
    const autor = appState.currentUser ? appState.currentUser.nombre : 'Sistema';
    let csv = '\uFEFF';
    csv += 'MAKILA - REPORTE ESTADISTICO DE INVENTARIO\n';
    csv += `Fecha,${fecha}\n`;
    csv += `Generado por,${autor}\n\n`;

    csv += '1. ESTADISTICA DESCRIPTIVA\n';
    csv += 'Dimension,N,Suma,Media,Mediana,Minimo,Maximo,Desviacion Estandar,CV (%)\n';

    const series = [
        { name: 'Reactivos Quimicos', values: appState.reactivos.map(r => r.cantidad || 0) },
        { name: 'Biomateriales y Aleaciones', values: appState.biomateriales.map(b => b.cantidadDisponible || 0) },
        { name: 'Insumos Generales y EPP', values: appState.insumos.map(i => i.cantidad || 0) },
        { name: 'Entradas', values: appState.movimientos.filter(m => m.tipoMovimiento === 'ENTRADA').map(m => m.cantidad || 0) },
        { name: 'Salidas y Consumos', values: appState.movimientos.filter(m => m.tipoMovimiento !== 'ENTRADA').map(m => m.cantidad || 0) }
    ];

    series.forEach(s => {
        const st = calcDescriptiveStats(s.values);
        csv += `"${s.name}",${st.n},${st.sum.toFixed(2)},${st.mean.toFixed(2)},${st.median.toFixed(2)},${st.min.toFixed(2)},${st.max.toFixed(2)},${st.stdDev.toFixed(2)},${st.cv.toFixed(1)}\n`;
    });

    csv += '\n2. CLASIFICACION ABC Y PUNTO DE REORDEN (ROP)\n';
    csv += 'Categoria,Material,Stock Actual,Unidad,Movimientos,% Participacion,% Acumulado,Clase,ROP Sugerido,Estado\n';

    appState.abcItems.forEach(it => {
        const estado = it.requiereReorden ? 'REORDENAR' : 'OPTIMO';
        csv += `"${it.categoria}","${it.nombre.replace(/"/g, '""')}",${it.stock},"${it.unidad}",${it.movimientosCount},${it.participacionPct.toFixed(1)},${it.acumuladoPct.toFixed(1)},${it.clase},${it.rop},${estado}\n`;
    });

    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `makila_reporte_${fecha}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
    logSessionActivity('Exportación de reporte CSV');
};

// ================= PLANIFICADOR DE LOTES =================
window.setPlannerPreset = function(prompt) {
    const input = document.getElementById('planner-input');
    if (input) {
        input.value = prompt;
        evaluarLoteExperimental();
    }
};

window.evaluarLoteExperimental = async function() {
    const input = document.getElementById('planner-input');
    const container = document.getElementById('planner-results');
    if (!input || !container) return;

    const texto = input.value.trim();
    if (!texto) {
        container.innerHTML = `<div class="planner-empty-state"><p>Ingresa los materiales requeridos.</p></div>`;
        return;
    }

    container.innerHTML = `<div class="planner-empty-state"><p>Evaluando disponibilidad...</p></div>`;
    const usuarioActivo = appState.currentUser ? appState.currentUser.nombre : 'Investigador Biomédico';

    try {
        const res = await fetch('/api/chat', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ mensaje: 'Planificar lote experimental con ' + texto, usuario: usuarioActivo })
        });

        const data = await res.json();
        logSessionActivity(`Planificación de lote: ${texto.substring(0, 30)}`);
        renderPlannerResults(data);
    } catch (e) {
        container.innerHTML = `<div class="planner-empty-state"><p>Error: ${escapeHtml(e.message)}</p></div>`;
    }
};

function renderPlannerResults(data) {
    const container = document.getElementById('planner-results');
    if (!container) return;

    if (!data.data || !data.data.items) {
        container.innerHTML = `
            <div class="planner-report-box">
                <p style="white-space: pre-line; line-height: 1.6;">${escapeHtml(data.respuesta)}</p>
            </div>
        `;
        return;
    }

    const plan = data.data;
    const isViable = plan.viable;
    let itemsHtml = '';

    plan.items.forEach(it => {
        itemsHtml += `
            <div class="planner-item-row">
                <div>
                    <strong>${escapeHtml(it.material)}</strong>
                    <div style="font-size: 0.78rem; color: #555350; margin-top: 2px;">
                        ${escapeHtml(it.advertenciaSeguridad)}
                    </div>
                </div>
                <div style="text-align: right;">
                    <div style="font-size: 0.88rem; font-weight: 700;">
                        Stock: ${it.stockActual} ${escapeHtml(it.unidad)}
                    </div>
                    <span class="badge-status ${it.disponible ? 'operativo' : 'mantenimiento'}">
                        ${it.disponible ? 'DISPONIBLE' : 'ABASTECER'}
                    </span>
                </div>
            </div>
        `;
    });

    container.innerHTML = `
        <div class="planner-report-box">
            <div class="planner-header-badge ${isViable ? 'viable' : 'alert'}">
                <strong>${isViable ? 'LOTE VIABLE' : 'REQUIERE ABASTECIMIENTO'}</strong>
                <span>${plan.items.length} Insumos</span>
            </div>
            <div style="display: flex; flex-direction: column; gap: 0.5rem;">
                ${itemsHtml}
            </div>
        </div>
    `;
}

// ================= HABLA CON MAKILA (CHAT) =================
function initChat() {
    const form = document.getElementById('chat-form');
    const input = document.getElementById('chat-input');

    if (form && input) {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            const text = input.value.trim();
            if (!text) return;
            appendUserMessage(text);
            input.value = '';
            await sendToAgent(text);
        });
    }

    document.querySelectorAll('.quick-prompt-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            const prompt = btn.getAttribute('data-prompt');
            appendUserMessage(prompt);
            sendToAgent(prompt);
        });
    });
}

function appendUserMessage(text) {
    const box = document.getElementById('chat-messages');
    if (!box) return;
    const shortName = appState.currentUser ? getShortName(appState.currentUser.nombre) : 'Usuario';
    const bubble = document.createElement('div');
    bubble.className = 'message-bubble user';
    bubble.innerHTML = `
        <div style="font-family: var(--font-mono); font-size: 0.7rem; font-weight: 700; margin-bottom: 0.3rem; color: var(--fountain-blue);">[${escapeHtml(shortName)}]</div>
        <p>${escapeHtml(text)}</p>
    `;
    box.appendChild(bubble);
    box.scrollTop = box.scrollHeight;
}

async function sendToAgent(text) {
    const box = document.getElementById('chat-messages');
    if (!box) return;

    const loadingBubble = document.createElement('div');
    loadingBubble.className = 'message-bubble agent';
    loadingBubble.id = 'agent-loading';
    loadingBubble.innerHTML = `<p>Procesando...</p>`;
    box.appendChild(loadingBubble);
    box.scrollTop = box.scrollHeight;

    const usuarioActivo = appState.currentUser ? appState.currentUser.nombre : 'Investigador Biomédico';

    try {
        const res = await fetch('/api/chat', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ mensaje: text, usuario: usuarioActivo })
        });

        const loadingElem = document.getElementById('agent-loading');
        if (loadingElem) loadingElem.remove();

        const data = await res.json();
        renderAgentResponse(data);
        refreshAllData();
    } catch (err) {
        const loadingElem = document.getElementById('agent-loading');
        if (loadingElem) loadingElem.remove();

        const errorBubble = document.createElement('div');
        errorBubble.className = 'message-bubble agent';
        errorBubble.innerHTML = `
            <span class="badge-sensible">ERROR</span>
            <p>${escapeHtml(err.message)}</p>
        `;
        box.appendChild(errorBubble);
    }
}

function renderAgentResponse(res) {
    const box = document.getElementById('chat-messages');
    if (!box) return;
    const bubble = document.createElement('div');
    bubble.className = 'message-bubble agent';

    let toolHtml = '';
    if (res.toolUsed) {
        toolHtml = `<div class="tool-indicator">[${escapeHtml(res.toolUsed)}]</div>`;
    }

    let statusContent = '';
    if (res.status === 'BLOCKED_GUARDRAIL') {
        statusContent = `
            <div class="guardrail-blocked">
                <strong>[BLOQUEO DE SEGURIDAD P07]</strong>
                <p>${escapeHtml(res.respuesta)}</p>
            </div>
        `;
    } else if (res.status === 'AMBIGUOUS') {
        let optionsHtml = '';
        if (res.opcionesDesambiguacion && res.opcionesDesambiguacion.length > 0) {
            optionsHtml = '<div class="disambiguation-options">';
            res.opcionesDesambiguacion.forEach(op => {
                optionsHtml += `<button class="option-btn" onclick="sendDisambiguationChoice('${escapeHtml(op)}')">${escapeHtml(op)}</button>`;
            });
            optionsHtml += '</div>';
        }

        statusContent = `
            <div style="background: rgba(100, 186, 190, 0.12); padding: 0.9rem; border-left: 4px solid var(--venice-blue); border: 1px solid rgba(8, 89, 121, 0.25);">
                <strong style="color: var(--venice-blue);">[ACLARACIÓN REQUERIDA]</strong>
                <p style="margin-top: 0.3rem;">${escapeHtml(res.respuesta)}</p>
                ${optionsHtml}
            </div>
        `;
    } else {
        statusContent = `<p style="white-space: pre-line;">${escapeHtml(res.respuesta)}</p>`;
    }

    bubble.innerHTML = `
        ${toolHtml}
        ${statusContent}
    `;
    box.appendChild(bubble);
    box.scrollTop = box.scrollHeight;
}

window.sendDisambiguationChoice = function(choice) {
    const prompt = `Confirmar y consultar: ${choice}`;
    appendUserMessage(prompt);
    sendToAgent(prompt);
};

// ================= MODAL DE TRANSACCIÓN MANUAL =================
function initModal() {
    const modal = document.getElementById('modal-movimiento');
    const closeBtn = document.getElementById('close-modal-btn');
    const cancelBtn = document.getElementById('cancel-modal-btn');
    const form = document.getElementById('form-movimiento');

    window.openModalMovimiento = function() {
        const respInput = document.getElementById('mov-usuario');
        if (respInput && appState.currentUser) {
            respInput.value = appState.currentUser.nombre;
        }
        if (modal) modal.classList.add('active');
    };

    const closeModal = () => {
        if (modal) modal.classList.remove('active');
    };

    if (closeBtn) closeBtn.addEventListener('click', closeModal);
    if (cancelBtn) cancelBtn.addEventListener('click', closeModal);

    if (form) {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            const payload = {
                tipoMovimiento: document.getElementById('mov-tipo').value,
                categoria: document.getElementById('mov-categoria').value,
                itemId: parseInt(document.getElementById('mov-id').value),
                cantidad: parseFloat(document.getElementById('mov-cantidad').value),
                unidad: document.getElementById('mov-unidad').value,
                experimentoOLote: document.getElementById('mov-experimento').value,
                usuarioResponsable: document.getElementById('mov-usuario').value,
                notas: document.getElementById('mov-notas').value
            };

            try {
                const res = await fetch('/api/movimientos', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });
                const data = await res.json();
                if (!res.ok) {
                    alert(`[ERROR] (${data.codigo || 'VALIDACION'}): ${data.mensaje || 'Error al procesar'}`);
                    return;
                }
                logSessionActivity(`Movimiento ${payload.tipoMovimiento} (${payload.cantidad} uds, ID ${payload.itemId})`);
                alert('Movimiento registrado.');
                closeModal();
                form.reset();
                refreshAllData();
            } catch (err) {
                alert('Error de conexión: ' + err.message);
            }
        });
    }
}

// ================= MODALES DE ALTA EN CATÁLOGOS =================
window.openModalReactivo = function() {
    const m = document.getElementById('modal-reactivo');
    if (m) m.classList.add('active');
};

window.openModalBiomaterial = function() {
    const m = document.getElementById('modal-biomaterial');
    if (m) m.classList.add('active');
};

window.openModalEquipo = function() {
    const resp = document.getElementById('eq-responsable');
    if (resp && appState.currentUser) resp.value = appState.currentUser.nombre;
    const m = document.getElementById('modal-equipo');
    if (m) m.classList.add('active');
};

window.openModalInsumo = function() {
    const m = document.getElementById('modal-insumo');
    if (m) m.classList.add('active');
};

window.closeCatalogModal = function(modalId) {
    const m = document.getElementById(modalId);
    if (m) m.classList.remove('active');
};

function initCatalogModals() {
    ['modal-reactivo', 'modal-biomaterial', 'modal-equipo', 'modal-insumo'].forEach(id => {
        const m = document.getElementById(id);
        if (m) {
            m.addEventListener('click', (e) => {
                if (e.target === m) closeCatalogModal(id);
            });
        }
    });

    const formReactivo = document.getElementById('form-nuevo-reactivo');
    if (formReactivo) {
        formReactivo.addEventListener('submit', async (e) => {
            e.preventDefault();
            const tempVal = document.getElementById('reac-temp').value;
            const payload = {
                nombre: document.getElementById('reac-nombre').value.trim(),
                formula: document.getElementById('reac-formula').value.trim(),
                cantidad: parseFloat(document.getElementById('reac-cantidad').value),
                unidad: document.getElementById('reac-unidad').value.trim(),
                fechaCaducidad: document.getElementById('reac-caducidad').value,
                ubicacion: document.getElementById('reac-ubicacion').value.trim(),
                tempMaxCelsius: tempVal !== '' ? parseFloat(tempVal) : null,
                requiereAislamientoLuz: document.getElementById('reac-luz').checked,
                estadoSeguridad: document.getElementById('reac-seguridad').value.trim(),
                msdsResumen: document.getElementById('reac-msds').value.trim()
            };
            await submitCatalogForm('/api/reactivos', payload, 'modal-reactivo', formReactivo, `Alta de reactivo: ${payload.nombre}`);
        });
    }

    const formBiomat = document.getElementById('form-nuevo-biomaterial');
    if (formBiomat) {
        formBiomat.addEventListener('submit', async (e) => {
            e.preventDefault();
            const payload = {
                tipoMaterial: document.getElementById('bio-tipo').value.trim(),
                nombre: document.getElementById('bio-nombre').value.trim(),
                lote: document.getElementById('bio-lote').value.trim(),
                cantidadDisponible: parseFloat(document.getElementById('bio-cantidad').value),
                unidad: document.getElementById('bio-unidad').value.trim(),
                especificacionesTecnicas: document.getElementById('bio-specs').value.trim()
            };
            await submitCatalogForm('/api/biomateriales', payload, 'modal-biomaterial', formBiomat, `Alta de biomaterial: ${payload.nombre}`);
        });
    }

    const formEquipo = document.getElementById('form-nuevo-equipo');
    if (formEquipo) {
        formEquipo.addEventListener('submit', async (e) => {
            e.preventDefault();
            const payload = {
                nombreEquipo: document.getElementById('eq-nombre').value.trim(),
                estadoActual: document.getElementById('eq-estado').value,
                fechaProximaCalibracion: document.getElementById('eq-calibracion').value,
                ubicacion: document.getElementById('eq-ubicacion').value.trim(),
                responsable: document.getElementById('eq-responsable').value.trim()
            };
            await submitCatalogForm('/api/equipos', payload, 'modal-equipo', formEquipo, `Alta de equipo: ${payload.nombreEquipo}`);
        });
    }

    const formInsumo = document.getElementById('form-nuevo-insumo');
    if (formInsumo) {
        formInsumo.addEventListener('submit', async (e) => {
            e.preventDefault();
            const payload = {
                categoriaInsumo: document.getElementById('ins-categoria').value,
                nombre: document.getElementById('ins-nombre').value.trim(),
                cantidad: parseFloat(document.getElementById('ins-cantidad').value),
                unidad: document.getElementById('ins-unidad').value.trim(),
                stockMinimo: parseFloat(document.getElementById('ins-minimo').value),
                ubicacion: document.getElementById('ins-ubicacion').value.trim(),
                especificaciones: document.getElementById('ins-specs').value.trim()
            };
            await submitCatalogForm('/api/insumos', payload, 'modal-insumo', formInsumo, `Alta de insumo: ${payload.nombre}`);
        });
    }
}

async function submitCatalogForm(endpoint, payload, modalId, formEl, activityMsg) {
    try {
        const res = await fetch(endpoint, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (!res.ok) {
            alert(`[ERROR] (${data.codigo || 'VALIDACION'}): ${data.mensaje || 'No se pudo guardar el registro.'}`);
            return;
        }
        logSessionActivity(activityMsg);
        closeCatalogModal(modalId);
        formEl.reset();
        await refreshAllData();
    } catch (err) {
        alert('Error de conexión: ' + err.message);
    }
}

function escapeHtml(text) {
    if (!text) return '';
    return String(text)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}
