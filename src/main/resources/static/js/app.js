/* ==========================================================================
   MAKILA - Frontend Application Controller
   Arquitectura Hexagonal - Adaptador Web Primario
   ========================================================================== */

document.addEventListener('DOMContentLoaded', () => {
    initTabs();
    loadDashboardStats();
    loadAlertas();
    loadReactivos();
    loadBiomateriales();
    loadEquipos();
    loadHistorial();
    initChat();
    initModal();
});

// ================= NAVEGACIÓN POR PESTAÑAS =================
function initTabs() {
    const tabButtons = document.querySelectorAll('.tab-btn');
    const tabPanes = document.querySelectorAll('.tab-pane');

    tabButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const targetId = btn.getAttribute('data-tab');
            tabButtons.forEach(b => b.classList.remove('active'));
            tabPanes.forEach(p => p.classList.remove('active'));

            btn.classList.add('active');
            const targetPane = document.getElementById(targetId);
            if (targetPane) targetPane.classList.add('active');

            if (targetId === 'tab-crud') {
                refreshAllData();
            }
        });
    });
}

function refreshAllData() {
    loadDashboardStats();
    loadAlertas();
    loadReactivos();
    loadBiomateriales();
    loadEquipos();
    loadHistorial();
}

// ================= CARGA DE DATOS REST =================
async function loadDashboardStats() {
    try {
        const res = await fetch('/api/dashboard/stats');
        if (!res.ok) return;
        const data = await res.json();
        document.getElementById('kpi-reactivos').textContent = data.totalReactivos;
        document.getElementById('kpi-biomateriales').textContent = data.totalBiomateriales;
        document.getElementById('kpi-equipos').textContent = `${data.equiposOperativos} / ${data.totalEquipos}`;
        document.getElementById('kpi-movimientos').textContent = data.totalMovimientos;
        document.getElementById('kpi-alertas').textContent = data.alertasCriticas;
    } catch (e) {
        console.error('Error cargando KPIs:', e);
    }
}

async function loadAlertas() {
    const container = document.getElementById('alerts-container');
    try {
        const res = await fetch('/api/dashboard/alertas');
        if (!res.ok) return;
        const alertas = await res.json();
        container.innerHTML = '';

        if (alertas.length === 0) {
            container.innerHTML = `
                <div class="alert-banner" style="border-left-color: var(--fountain-blue); background-color: #E2F0D9; color: #276A3C;">
                    <div class="alert-info">
                        <h4>✓ Todos los sistemas y parámetros de laboratorio en estado óptimo</h4>
                        <p>No hay alertas críticas de temperatura, luz UV ni calibraciones vencidas.</p>
                    </div>
                </div>
            `;
            return;
        }

        alertas.forEach(a => {
            const isCritical = a.nivel === 'CRITICO';
            const banner = document.createElement('div');
            banner.className = `alert-banner ${isCritical ? '' : 'warning'}`;
            banner.innerHTML = `
                <div class="alert-info">
                    <h4>${isCritical ? '⚠️' : 'ℹ️'} ${escapeHtml(a.titulo)}</h4>
                    <p>${escapeHtml(a.descripcion)} <strong>Recomendación:</strong> ${escapeHtml(a.recomendacion)}</p>
                </div>
                <span class="badge-tag">${escapeHtml(a.categoria)}</span>
            `;
            container.appendChild(banner);
        });
    } catch (e) {
        console.error('Error cargando alertas:', e);
    }
}

async function loadReactivos() {
    const tbody = document.getElementById('tbody-reactivos');
    try {
        const res = await fetch('/api/reactivos');
        if (!res.ok) return;
        const data = await res.json();
        tbody.innerHTML = '';
        data.forEach(r => {
            const tr = document.createElement('tr');
            const alertBadges = [];
            if (r.tempMaxCelsius && r.tempMaxCelsius <= 30) {
                alertBadges.push(`<span class="badge-sensible">Máx ${r.tempMaxCelsius}°C</span>`);
            }
            if (r.requiereAislamientoLuz) {
                alertBadges.push(`<span class="badge-sensible">UV / Fotosensible</span>`);
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

async function loadEquipos() {
    const tbody = document.getElementById('tbody-equipos');
    try {
        const res = await fetch('/api/equipos');
        if (!res.ok) return;
        const data = await res.json();
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
        tbody.innerHTML = '';
        data.forEach(m => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>#${m.id}</td>
                <td><strong>${escapeHtml(m.tipoMovimiento)}</strong></td>
                <td><span class="badge-tag">${escapeHtml(m.categoria)}</span></td>
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

// ================= CHAT CONVERSACIONAL AGÉNTICO =================
function initChat() {
    const form = document.getElementById('chat-form');
    const input = document.getElementById('chat-input');
    const messagesBox = document.getElementById('chat-messages');

    // Manejar envío de mensaje
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const text = input.value.trim();
        if (!text) return;
        appendUserMessage(text);
        input.value = '';
        await sendToAgent(text);
    });

    // Manejar botones de prompts rápidos
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
    const bubble = document.createElement('div');
    bubble.className = 'message-bubble user';
    bubble.innerHTML = `<strong>Tú:</strong><p>${escapeHtml(text)}</p>`;
    box.appendChild(bubble);
    box.scrollTop = box.scrollHeight;
}

async function sendToAgent(text) {
    const box = document.getElementById('chat-messages');
    
    // Bubble de carga
    const loadingBubble = document.createElement('div');
    loadingBubble.className = 'message-bubble agent';
    loadingBubble.id = 'agent-loading';
    loadingBubble.innerHTML = `<em>Analizando intención con herramientas seguras...</em>`;
    box.appendChild(loadingBubble);
    box.scrollTop = box.scrollHeight;

    try {
        const res = await fetch('/api/chat', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ mensaje: text, usuario: 'Investigador Biomédico' })
        });

        const loadingElem = document.getElementById('agent-loading');
        if (loadingElem) loadingElem.remove();

        const data = await res.json();
        renderAgentResponse(data);
        refreshAllData(); // Sincroniza datos en tiempo real tras operaciones
    } catch (err) {
        const loadingElem = document.getElementById('agent-loading');
        if (loadingElem) loadingElem.remove();

        const errorBubble = document.createElement('div');
        errorBubble.className = 'message-bubble agent';
        errorBubble.innerHTML = `<span class="badge-sensible">Error de Red</span><p>No se pudo contactar al servidor: ${escapeHtml(err.message)}</p>`;
        box.appendChild(errorBubble);
    }
}

function renderAgentResponse(res) {
    const box = document.getElementById('chat-messages');
    const bubble = document.createElement('div');
    bubble.className = 'message-bubble agent';

    let toolHtml = '';
    if (res.toolUsed) {
        toolHtml = `<div class="tool-indicator">🛠️ Herramienta: ${escapeHtml(res.toolUsed)}</div>`;
    }

    let statusContent = '';
    if (res.status === 'BLOCKED_GUARDRAIL') {
        statusContent = `
            <div class="guardrail-blocked">
                <strong>🛡️ Guardrail de Seguridad P07 Activado:</strong>
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
            <div style="background: #FFF3CD; padding: 0.8rem; border-radius: 6px; border-left: 4px solid #FFC107;">
                <strong>⚠️ Ambigüedad detectada:</strong>
                <p>${escapeHtml(res.respuesta)}</p>
                <p style="margin-top:0.4rem; font-size:0.85rem;">Por favor selecciona la opción exacta o especifica tu solicitud:</p>
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

// ================= MODAL MANUAL DE MOVIMIENTO =================
function initModal() {
    const modal = document.getElementById('modal-movimiento');
    const openBtn = document.getElementById('btn-nuevo-movimiento');
    const closeBtn = document.getElementById('close-modal-btn');
    const cancelBtn = document.getElementById('cancel-modal-btn');
    const form = document.getElementById('form-movimiento');

    if (openBtn) {
        openBtn.addEventListener('click', () => {
            modal.classList.add('active');
        });
    }

    const closeModal = () => modal.classList.remove('active');
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
                    alert(`Error (${data.codigo || 'VALIDACION'}): ${data.mensaje || 'Error al procesar movimiento'}`);
                    return;
                }
                alert('Movimiento registrado correctamente.');
                closeModal();
                form.reset();
                refreshAllData();
            } catch (err) {
                alert('Error de conexión: ' + err.message);
            }
        });
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

