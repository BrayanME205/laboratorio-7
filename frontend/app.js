const API_BASE_URL = 'http://localhost:8080';

const ROLE_ADMIN = 'ROLE_ADMIN';
const ROLE_OPERADOR = 'ROLE_OPERADOR';
const ROLE_CONDUCTOR = 'ROLE_CONDUCTOR';

const ESTADOS = {
    PENDIENTE: 'Pendiente',
    EN_TRANSITO: 'En tránsito',
    ENTREGADO: 'Entregado',
    CANCELADO: 'Cancelado'
};

/* ===== Sesión (sessionStorage + JWT) ===== */

function parseJwt(token) {
    try {
        const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
        const json = decodeURIComponent(atob(base64).split('')
            .map((c) => '%' + c.charCodeAt(0).toString(16).padStart(2, '0')).join(''));
        return JSON.parse(json);
    } catch (error) {
        return null;
    }
}

function clearSession() {
    sessionStorage.removeItem('jwt_token');
}

function redirectToLogin() {
    clearSession();
    window.location.replace('index.html');
}

function getSession() {
    const token = sessionStorage.getItem('jwt_token');
    if (!token) return null;
    const payload = parseJwt(token);
    if (!payload || (payload.exp && payload.exp * 1000 < Date.now())) {
        clearSession();
        return null;
    }
    return { token, username: payload.sub, roles: Array.isArray(payload.roles) ? payload.roles : [] };
}

async function fetchWithAuth(path, options = {}) {
    const token = sessionStorage.getItem('jwt_token');
    if (!token) {
        redirectToLogin();
        throw new Error('Sin sesión activa');
    }
    const response = await fetch(`${API_BASE_URL}${path}`, {
        ...options,
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`,
            ...(options.headers || {})
        }
    });
    if (response.status === 401 || response.status === 403) {
        redirectToLogin();
        throw new Error('Sesión expirada o acceso denegado');
    }
    return response;
}

/* ===== Utilidades ===== */

function el(tag, options = {}, children = []) {
    const node = document.createElement(tag);
    if (options.className) node.className = options.className;
    if (options.text !== undefined && options.text !== null) node.textContent = options.text;
    if (options.type) node.type = options.type;
    if (options.onClick) node.addEventListener('click', options.onClick);
    children.forEach((child) => node.appendChild(child));
    return node;
}

// Extrae los mensajes de un JSON RFC 7807 (400, 404, 409, ...)
async function readErrors(response) {
    try {
        const problem = await response.json();
        if (Array.isArray(problem.errors) && problem.errors.length > 0) {
            return problem.errors.map((item) => `${item.field}: ${item.message}`);
        }
        return [problem.detail || problem.title || `Error ${response.status}`];
    } catch (error) {
        return [`Error inesperado (${response.status})`];
    }
}

function renderAlert(box, messages, isError) {
    box.replaceChildren();
    box.className = `alert ${isError ? 'alert-error' : 'alert-success'}`;
    if (messages.length === 1) {
        box.textContent = messages[0];
    } else {
        box.appendChild(el('strong', { text: 'Se encontraron los siguientes errores:' }));
        const list = el('ul');
        messages.forEach((message) => list.appendChild(el('li', { text: message })));
        box.appendChild(list);
    }
}

function showMessage(messages, isError = false) {
    const box = document.getElementById('message');
    renderAlert(box, Array.isArray(messages) ? messages : [messages], isError);
    window.clearTimeout(showMessage.timer);
    showMessage.timer = window.setTimeout(() => { box.replaceChildren(); box.className = 'alert'; }, 8000);
}

async function showResponseError(response) {
    showMessage(await readErrors(response), true);
}

function formatMoney(value) {
    return new Intl.NumberFormat('es-CR', { style: 'currency', currency: 'CRC' }).format(value);
}

const estadoLabel = (estado) => ESTADOS[estado] || estado;

/* ===== Login (index.html) ===== */

function initLogin() {
    if (getSession()) {
        window.location.replace('dashboard.html');
        return;
    }
    const form = document.getElementById('loginForm');
    const errorBox = document.getElementById('loginError');
    const button = document.getElementById('loginButton');

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        errorBox.textContent = '';

        const username = form.username.value.trim();
        const password = form.password.value;
        if (!username || !password) {
            errorBox.textContent = 'El usuario y la contraseña son obligatorios';
            return;
        }

        button.disabled = true;
        try {
            const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });
            if (!response.ok) {
                errorBox.textContent = response.status === 401
                    ? 'Usuario o contraseña incorrectos'
                    : (await readErrors(response)).join('. ');
                return;
            }
            const data = await response.json();
            sessionStorage.setItem('jwt_token', data.token);
            window.location.replace('dashboard.html');
        } catch (error) {
            errorBox.textContent = 'No se pudo conectar con el servidor. Verifique que el backend esté activo';
        } finally {
            button.disabled = false;
        }
    });
}

/* ===== Dashboard (dashboard.html) ===== */

const state = { session: null, shipments: [], filter: 'TODOS', auditEntries: [] };

const hasAnyRole = (...roles) => roles.some((role) => state.session.roles.includes(role));

function updateKpis() {
    const active = new Set(state.shipments
        .filter((s) => s.estadoEnvio === 'PENDIENTE' || s.estadoEnvio === 'EN_TRANSITO')
        .map((s) => s.placaVehiculo));
    document.getElementById('kpiTotal').textContent = state.shipments.length;
    document.getElementById('kpiVehiculos').textContent = active.size;
    document.getElementById('kpiEntregados').textContent =
        state.shipments.filter((s) => s.estadoEnvio === 'ENTREGADO').length;
}

function buildCard(shipment) {
    const isAdmin = hasAnyRole(ROLE_ADMIN);
    const isOperator = hasAnyRole(ROLE_OPERADOR);
    const isDriver = hasAnyRole(ROLE_CONDUCTOR);
    const actions = el('div', { className: 'card-actions' });

    const canDispatch = isAdmin || isOperator;
    const canDeliver = isAdmin || isDriver;

    if (canDispatch && shipment.estadoEnvio === 'PENDIENTE') {
        actions.appendChild(el('button', {
            className: 'btn btn-small', type: 'button', text: 'Marcar en tránsito',
            onClick: () => changeStatus(shipment.id, 'EN_TRANSITO')
        }));
    }
    if (canDeliver && shipment.estadoEnvio === 'EN_TRANSITO') {
        actions.appendChild(el('button', {
            className: 'btn btn-small', type: 'button', text: 'Marcar entregado',
            onClick: () => changeStatus(shipment.id, 'ENTREGADO')
        }));
    }
    if ((isAdmin || isOperator) && shipment.estadoEnvio === 'PENDIENTE') {
        actions.appendChild(el('button', {
            className: 'btn btn-small btn-danger', type: 'button', text: 'Cancelar',
            onClick: () => cancelShipment(shipment.id)
        }));
    }
    if (isAdmin) {
        actions.appendChild(el('button', {
            className: 'btn btn-small btn-secondary', type: 'button', text: 'Ver bitácora',
            onClick: () => openAudit(shipment)
        }));
    }

    const details = el('dl', {}, [
        el('dt', { text: 'Destino' }), el('dd', { text: shipment.direccionDestino }),
        el('dt', { text: 'Peso' }), el('dd', { text: `${shipment.pesoKg} kg` }),
        el('dt', { text: 'Costo' }), el('dd', { text: formatMoney(shipment.costo) }),
        el('dt', { text: 'Vehículo' }), el('dd', { text: shipment.placaVehiculo }),
        el('dt', { text: 'Conductor' }), el('dd', { text: shipment.nombreConductor })
    ]);

    const header = el('header', { className: 'card-header' }, [
        el('h3', { className: 'card-title', text: shipment.codigoRastreo }),
        el('span', { className: 'status-badge', text: estadoLabel(shipment.estadoEnvio) })
    ]);

    return el('article', { className: `card ${shipment.estadoEnvio}` }, [
        el('div', {}, [header, details]), actions
    ]);
}

function renderShipments() {
    const grid = document.getElementById('enviosGrid');
    grid.replaceChildren();
    const visible = state.filter === 'TODOS'
        ? state.shipments
        : state.shipments.filter((s) => s.estadoEnvio === state.filter);
    if (visible.length === 0) {
        grid.appendChild(el('p', { className: 'empty-state', text: 'No hay envíos para mostrar.' }));
        return;
    }
    visible.forEach((shipment) => grid.appendChild(buildCard(shipment)));
}

async function loadShipments() {
    try {
        const response = await fetchWithAuth('/api/envios/optimizados');
        if (!response.ok) {
            await showResponseError(response);
            return;
        }
        state.shipments = await response.json();
        updateKpis();
        renderShipments();
    } catch (error) {
        if (sessionStorage.getItem('jwt_token')) {
            showMessage('No se pudo conectar con el servidor. Verifique que el backend esté activo', true);
        }
    }
}

async function changeStatus(id, newStatus) {
    const notes = window.prompt('Observaciones para la bitácora (opcional):', '');
    if (notes === null) return;
    const response = await fetchWithAuth(`/api/envios/${id}/estado`, {
        method: 'PATCH',
        body: JSON.stringify({ nuevoEstado: newStatus, observaciones: notes.trim() || null })
    });
    if (!response.ok) { await showResponseError(response); return; }
    showMessage('Estado del envío actualizado');
    loadShipments();
}

async function cancelShipment(id) {
    const notes = window.prompt('Motivo de la cancelación (opcional):', '');
    if (notes === null) return;
    const query = notes.trim() ? `?observaciones=${encodeURIComponent(notes.trim())}` : '';
    const response = await fetchWithAuth(`/api/envios/${id}/cancelar${query}`, { method: 'PATCH' });
    if (!response.ok) { await showResponseError(response); return; }
    showMessage('Envío cancelado');
    loadShipments();
}

async function estimateFee() {
    const weight = document.getElementById('weightKg').value;
    const distance = document.getElementById('distanceKm').value;
    if (!weight || !distance) {
        showMessage('Ingrese peso y distancia para estimar la tarifa', true);
        return;
    }
    const query = `?pesoKg=${encodeURIComponent(weight)}&distanciaKm=${encodeURIComponent(distance)}`;
    const response = await fetchWithAuth(`/api/envios/tarifa${query}`);
    if (!response.ok) { await showResponseError(response); return; }
    const data = await response.json();
    document.getElementById('cost').value = data.tarifa.toFixed(2);
}

async function createShipment(event) {
    event.preventDefault();
    const payload = {
        codigoRastreo: document.getElementById('trackingCode').value.trim(),
        direccionDestino: document.getElementById('destination').value.trim(),
        pesoKg: parseFloat(document.getElementById('weightKg').value),
        costo: parseFloat(document.getElementById('cost').value),
        vehiculoId: parseInt(document.getElementById('vehicleId').value, 10),
        conductorId: parseInt(document.getElementById('driverId').value, 10)
    };
    const response = await fetchWithAuth('/api/envios', { method: 'POST', body: JSON.stringify(payload) });
    if (!response.ok) { await showResponseError(response); return; }
    event.target.reset();
    showMessage('Envío creado correctamente');
    loadShipments();
}

/* ----- Bitácora (aside, solo ADMIN) ----- */

function renderAudit() {
    const from = document.getElementById('auditFrom').value;
    const to = document.getElementById('auditTo').value;
    const list = document.getElementById('auditList');
    list.replaceChildren();

    const filtered = state.auditEntries.filter((entry) => {
        const day = entry.fechaCambio.slice(0, 10);
        return (!from || day >= from) && (!to || day <= to);
    });
    document.getElementById('auditEmpty').classList.toggle('hidden', filtered.length > 0);

    filtered.forEach((entry) => {
        list.appendChild(el('li', {}, [
            el('div', { className: 'audit-change' }, [
                el('span', { text: estadoLabel(entry.estadoAnterior) }),
                el('span', { className: 'arrow', text: '\u2192' }),
                el('span', { text: estadoLabel(entry.estadoNuevo) })
            ]),
            el('div', { className: 'audit-meta', text: `${new Date(entry.fechaCambio).toLocaleString('es-CR')} · ${entry.usuario}` }),
            el('div', { text: entry.observaciones || 'Sin observaciones' })
        ]));
    });
}

async function openAudit(shipment) {
    const response = await fetchWithAuth(`/api/envios/${shipment.id}/bitacora`);
    if (!response.ok) { await showResponseError(response); return; }
    state.auditEntries = await response.json();
    document.getElementById('auditTitle').textContent = `Bitácora - ${shipment.codigoRastreo}`;
    document.getElementById('auditFrom').value = '';
    document.getElementById('auditTo').value = '';
    document.getElementById('auditHint').classList.add('hidden');
    document.getElementById('auditContent').classList.remove('hidden');
    renderAudit();
    document.getElementById('auditAside').scrollIntoView({ behavior: 'smooth', block: 'start' });
}

/* ----- Flota (solo ADMIN) ----- */

async function loadFleet() {
    const [vehiclesResponse, companiesResponse] = await Promise.all([
        fetchWithAuth('/api/vehiculos'),
        fetchWithAuth('/api/empresas')
    ]);
    if (!vehiclesResponse.ok || !companiesResponse.ok) {
        await showResponseError(vehiclesResponse.ok ? companiesResponse : vehiclesResponse);
        return;
    }
    const vehicles = await vehiclesResponse.json();
    const companies = await companiesResponse.json();

    const vehicleRows = document.getElementById('vehicleRows');
    vehicleRows.replaceChildren();
    vehicles.forEach((vehicle) => {
        const actions = el('td', {}, [
            el('button', {
                className: 'btn btn-small btn-secondary', type: 'button', text: 'Asignar conductor',
                onClick: () => assignDriver(vehicle.id)
            }),
            document.createTextNode(' '),
            el('button', {
                className: 'btn btn-small btn-danger', type: 'button', text: 'Eliminar',
                onClick: () => deleteVehicle(vehicle.id)
            })
        ]);
        vehicleRows.appendChild(el('tr', {}, [
            el('td', { text: vehicle.id }),
            el('td', { text: vehicle.placa }),
            el('td', { text: `${vehicle.marca} ${vehicle.modelo}` }),
            el('td', { text: vehicle.capacidadKg }),
            el('td', { text: vehicle.nombreEmpresa }),
            el('td', { text: vehicle.nombreConductor || 'Sin asignar' }),
            actions
        ]));
    });

    const companyRows = document.getElementById('companyRows');
    companyRows.replaceChildren();
    companies.forEach((company) => companyRows.appendChild(el('tr', {}, [
        el('td', { text: company.id }),
        el('td', { text: company.nombre }),
        el('td', { text: company.cedulaJuridica }),
        el('td', { text: company.telefono || '-' })
    ])));
}

async function createVehicle(event) {
    event.preventDefault();
    const payload = {
        placa: document.getElementById('plate').value.trim().toUpperCase(),
        marca: document.getElementById('brand').value.trim(),
        modelo: document.getElementById('model').value.trim(),
        capacidadKg: parseFloat(document.getElementById('capacityKg').value),
        empresaId: parseInt(document.getElementById('companyId').value, 10)
    };
    const response = await fetchWithAuth('/api/vehiculos', { method: 'POST', body: JSON.stringify(payload) });
    if (!response.ok) { await showResponseError(response); return; }
    event.target.reset();
    showMessage('Vehículo registrado');
    loadFleet();
}

async function assignDriver(vehicleId) {
    const value = window.prompt('ID del conductor a asignar:', '');
    if (value === null || value.trim() === '') return;
    const response = await fetchWithAuth(`/api/vehiculos/${vehicleId}/conductor`, {
        method: 'PATCH',
        body: JSON.stringify({ conductorId: parseInt(value, 10) })
    });
    if (!response.ok) { await showResponseError(response); return; }
    showMessage('Conductor asignado');
    loadFleet();
}

async function deleteVehicle(vehicleId) {
    if (!window.confirm('¿Eliminar este vehículo?')) return;
    const response = await fetchWithAuth(`/api/vehiculos/${vehicleId}`, { method: 'DELETE' });
    if (!response.ok) { await showResponseError(response); return; }
    showMessage('Vehículo eliminado');
    loadFleet();
}

/* ----- Arranque ----- */

function applyRoleView() {
    document.getElementById('userName').textContent = state.session.username;
    const rolesBox = document.getElementById('userRoles');
    state.session.roles.forEach((role) =>
        rolesBox.appendChild(el('span', { className: 'role-badge', text: role.replace('ROLE_', '') })));

    if (hasAnyRole(ROLE_ADMIN, ROLE_OPERADOR)) {
        document.getElementById('createSection').classList.remove('hidden');
        document.getElementById('paginadoLink').classList.remove('hidden');
    }
    if (hasAnyRole(ROLE_ADMIN)) {
        document.getElementById('fleetSection').classList.remove('hidden');
        document.getElementById('auditAside').classList.remove('hidden');
        document.getElementById('layout').classList.add('has-aside');
        loadFleet();
    }
}

function initDashboard() {
    state.session = getSession();
    if (!state.session) {
        redirectToLogin();
        return;
    }
    applyRoleView();

    document.getElementById('logoutButton').addEventListener('click', redirectToLogin);
    document.getElementById('shipmentForm').addEventListener('submit', createShipment);
    document.getElementById('estimateButton').addEventListener('click', estimateFee);
    document.getElementById('vehicleForm').addEventListener('submit', createVehicle);
    document.getElementById('auditFrom').addEventListener('change', renderAudit);
    document.getElementById('auditTo').addEventListener('change', renderAudit);
    document.getElementById('auditClear').addEventListener('click', () => {
        document.getElementById('auditFrom').value = '';
        document.getElementById('auditTo').value = '';
        renderAudit();
    });
    document.querySelectorAll('.filter-btn').forEach((button) =>
        button.addEventListener('click', () => {
            state.filter = button.dataset.filter;
            document.querySelectorAll('.filter-btn').forEach((b) => {
                const active = b === button;
                b.classList.toggle('active', active);
                b.setAttribute('aria-pressed', String(active));
            });
            renderShipments();
        }));

    loadShipments();
}

const CURRENT_PAGE = document.body.dataset.page;
if (CURRENT_PAGE === 'login') {
    initLogin();
} else if (CURRENT_PAGE === 'dashboard') {
    initDashboard();
}