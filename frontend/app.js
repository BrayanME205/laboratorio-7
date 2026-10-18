const API_BASE_URL = 'http://localhost:8080';

const ROLE_ADMIN = 'ROLE_ADMIN';
const ROLE_OPERATOR = 'ROLE_OPERADOR';
const ROLE_DRIVER = 'ROLE_CONDUCTOR';

const STATUS_LABELS = {
    PENDIENTE: 'Pending',
    EN_TRANSITO: 'In transit',
    ENTREGADO: 'Delivered',
    CANCELADO: 'Cancelled'
};

const state = {
    username: localStorage.getItem('username'),
    roles: JSON.parse(localStorage.getItem('roles') || '[]'),
    auditEntries: []
};

/* ===== Session ===== */

function redirectToLogin() {
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('username');
    localStorage.removeItem('roles');
    localStorage.removeItem('token_expiration');
    window.location.replace('login.html');
}

function hasAnyRole(...roles) {
    return roles.some((role) => state.roles.includes(role));
}

async function fetchWithAuth(path, options = {}) {
    const token = localStorage.getItem('jwt_token');
    if (!token) {
        redirectToLogin();
        throw new Error('No active session');
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
        throw new Error('Session expired or access denied');
    }
    return response;
}

async function readError(response) {
    try {
        const problem = await response.json();
        if (Array.isArray(problem.errors) && problem.errors.length > 0) {
            return problem.errors.map((item) => item.message).join('. ');
        }
        return problem.detail || problem.title || 'Unexpected error';
    } catch (error) {
        return 'Unexpected error';
    }
}

/* ===== DOM helpers ===== */

function el(tag, options = {}, children = []) {
    const node = document.createElement(tag);
    if (options.className) node.className = options.className;
    if (options.text !== undefined && options.text !== null) node.textContent = options.text;
    if (options.type) node.type = options.type;
    if (options.onClick) node.addEventListener('click', options.onClick);
    children.forEach((child) => node.appendChild(child));
    return node;
}

function showMessage(text, isError = false) {
    const box = document.getElementById('message');
    box.textContent = text;
    box.className = `alert ${isError ? 'alert-error' : 'alert-success'}`;
    window.clearTimeout(showMessage.timer);
    showMessage.timer = window.setTimeout(() => box.classList.add('hidden'), 6000);
}

function formatMoney(value) {
    return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'CRC', maximumFractionDigits: 2 })
        .format(value);
}

function statusLabel(status) {
    return STATUS_LABELS[status] || status;
}

/* ===== Shipments ===== */

function buildShipmentCard(shipment) {
    const canChangeStatus = hasAnyRole(ROLE_ADMIN, ROLE_DRIVER);
    const canManage = hasAnyRole(ROLE_ADMIN, ROLE_OPERATOR);
    const actions = el('div', { className: 'card-actions' });

    if (canChangeStatus && shipment.estadoEnvio === 'PENDIENTE') {
        actions.appendChild(el('button', {
            className: 'btn btn-small', type: 'button', text: 'Mark in transit',
            onClick: () => changeStatus(shipment.id, 'EN_TRANSITO')
        }));
    }
    if (canChangeStatus && shipment.estadoEnvio === 'EN_TRANSITO') {
        actions.appendChild(el('button', {
            className: 'btn btn-small', type: 'button', text: 'Mark delivered',
            onClick: () => changeStatus(shipment.id, 'ENTREGADO')
        }));
    }
    if (canManage && shipment.estadoEnvio === 'PENDIENTE') {
        actions.appendChild(el('button', {
            className: 'btn btn-small btn-danger', type: 'button', text: 'Cancel',
            onClick: () => cancelShipment(shipment.id)
        }));
    }
    if (canManage) {
        actions.appendChild(el('button', {
            className: 'btn btn-small btn-secondary', type: 'button', text: 'View audit log',
            onClick: () => openAuditModal(shipment)
        }));
    }

    const details = el('dl', {}, [
        el('dt', { text: 'Destination' }), el('dd', { text: shipment.direccionDestino }),
        el('dt', { text: 'Weight' }), el('dd', { text: `${shipment.pesoKg} kg` }),
        el('dt', { text: 'Cost' }), el('dd', { text: formatMoney(shipment.costo) }),
        el('dt', { text: 'Vehicle' }), el('dd', { text: shipment.placaVehiculo }),
        el('dt', { text: 'Driver' }), el('dd', { text: shipment.nombreConductor })
    ]);

    const header = el('div', { className: 'card-header' }, [
        el('span', { className: 'card-title', text: shipment.codigoRastreo }),
        el('span', { className: 'status-badge', text: statusLabel(shipment.estadoEnvio) })
    ]);

    return el('article', { className: `card ${shipment.estadoEnvio}` }, [header, details, actions]);
}

async function loadShipments() {
    const container = document.getElementById('shipment-cards');
    try {
        const response = await fetchWithAuth('/api/envios/optimizados');
        if (!response.ok) {
            showMessage(await readError(response), true);
            return;
        }
        const shipments = await response.json();
        container.replaceChildren();
        if (shipments.length === 0) {
            container.appendChild(el('p', { className: 'empty-state', text: 'No shipments registered yet.' }));
            return;
        }
        shipments.forEach((shipment) => container.appendChild(buildShipmentCard(shipment)));
    } catch (error) {
        if (!localStorage.getItem('jwt_token')) return;
        showMessage('Cannot reach the server. Make sure the backend is running', true);
    }
}

async function changeStatus(id, newStatus) {
    const notes = window.prompt('Notes for the audit log (optional):', '');
    if (notes === null) return;
    const response = await fetchWithAuth(`/api/envios/${id}/estado`, {
        method: 'PATCH',
        body: JSON.stringify({ nuevoEstado: newStatus, observaciones: notes.trim() || null })
    });
    if (!response.ok) {
        showMessage(await readError(response), true);
        return;
    }
    showMessage('Shipment status updated');
    loadShipments();
}

async function cancelShipment(id) {
    const notes = window.prompt('Reason for cancellation (optional):', '');
    if (notes === null) return;
    const query = notes.trim() ? `?observaciones=${encodeURIComponent(notes.trim())}` : '';
    const response = await fetchWithAuth(`/api/envios/${id}/cancelar${query}`, { method: 'PATCH' });
    if (!response.ok) {
        showMessage(await readError(response), true);
        return;
    }
    showMessage('Shipment cancelled');
    loadShipments();
}

async function estimateFee() {
    const weight = document.getElementById('weightKg').value;
    const distance = document.getElementById('distanceKm').value;
    if (!weight || !distance) {
        showMessage('Enter weight and distance to estimate the fee', true);
        return;
    }
    const query = `?pesoKg=${encodeURIComponent(weight)}&distanciaKm=${encodeURIComponent(distance)}`;
    const response = await fetchWithAuth(`/api/envios/tarifa${query}`);
    if (!response.ok) {
        showMessage(await readError(response), true);
        return;
    }
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
    if (!response.ok) {
        showMessage(await readError(response), true);
        return;
    }
    event.target.reset();
    showMessage('Shipment created');
    loadShipments();
}

/* ===== Audit log modal ===== */

function renderAuditRows() {
    const from = document.getElementById('audit-from').value;
    const to = document.getElementById('audit-to').value;
    const rows = document.getElementById('audit-rows');
    rows.replaceChildren();

    const filtered = state.auditEntries.filter((entry) => {
        const day = entry.fechaCambio.slice(0, 10);
        return (!from || day >= from) && (!to || day <= to);
    });

    document.getElementById('audit-empty').classList.toggle('hidden', filtered.length > 0);

    filtered.forEach((entry) => {
        const change = el('td', {}, [
            el('span', { text: statusLabel(entry.estadoAnterior) }),
            el('span', { className: 'arrow', text: '\u2192' }),
            el('span', { text: statusLabel(entry.estadoNuevo) })
        ]);
        rows.appendChild(el('tr', {}, [
            change,
            el('td', { text: new Date(entry.fechaCambio).toLocaleString() }),
            el('td', { text: entry.usuario }),
            el('td', { text: entry.observaciones || '-' })
        ]));
    });
}

async function openAuditModal(shipment) {
    const response = await fetchWithAuth(`/api/envios/${shipment.id}/bitacora`);
    if (!response.ok) {
        showMessage(await readError(response), true);
        return;
    }
    state.auditEntries = await response.json();
    document.getElementById('audit-title').textContent = `Audit log - ${shipment.codigoRastreo}`;
    document.getElementById('audit-from').value = '';
    document.getElementById('audit-to').value = '';
    renderAuditRows();
    document.getElementById('audit-modal').classList.remove('hidden');
}

function closeAuditModal() {
    document.getElementById('audit-modal').classList.add('hidden');
}

/* ===== Fleet ===== */

async function loadFleet() {
    const [vehiclesResponse, companiesResponse] = await Promise.all([
        fetchWithAuth('/api/vehiculos'),
        fetchWithAuth('/api/empresas')
    ]);
    if (!vehiclesResponse.ok || !companiesResponse.ok) {
        showMessage(await readError(vehiclesResponse.ok ? companiesResponse : vehiclesResponse), true);
        return;
    }
    const vehicles = await vehiclesResponse.json();
    const companies = await companiesResponse.json();

    const vehicleRows = document.getElementById('vehicle-rows');
    vehicleRows.replaceChildren();
    vehicles.forEach((vehicle) => {
        const actions = el('td', {}, [
            el('button', {
                className: 'btn btn-small btn-secondary', type: 'button', text: 'Assign driver',
                onClick: () => assignDriver(vehicle.id)
            }),
            document.createTextNode(' '),
            el('button', {
                className: 'btn btn-small btn-danger', type: 'button', text: 'Delete',
                onClick: () => deleteVehicle(vehicle.id)
            })
        ]);
        vehicleRows.appendChild(el('tr', {}, [
            el('td', { text: vehicle.id }),
            el('td', { text: vehicle.placa }),
            el('td', { text: `${vehicle.marca} ${vehicle.modelo}` }),
            el('td', { text: vehicle.capacidadKg }),
            el('td', { text: vehicle.nombreEmpresa }),
            el('td', { text: vehicle.nombreConductor || 'Unassigned' }),
            actions
        ]));
    });

    const companyRows = document.getElementById('company-rows');
    companyRows.replaceChildren();
    companies.forEach((company) => {
        companyRows.appendChild(el('tr', {}, [
            el('td', { text: company.id }),
            el('td', { text: company.nombre }),
            el('td', { text: company.cedulaJuridica }),
            el('td', { text: company.telefono || '-' })
        ]));
    });
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
    if (!response.ok) {
        showMessage(await readError(response), true);
        return;
    }
    event.target.reset();
    showMessage('Vehicle registered');
    loadFleet();
}

async function assignDriver(vehicleId) {
    const value = window.prompt('Driver ID to assign:', '');
    if (value === null || value.trim() === '') return;
    const response = await fetchWithAuth(`/api/vehiculos/${vehicleId}/conductor`, {
        method: 'PATCH',
        body: JSON.stringify({ conductorId: parseInt(value, 10) })
    });
    if (!response.ok) {
        showMessage(await readError(response), true);
        return;
    }
    showMessage('Driver assigned');
    loadFleet();
}

async function deleteVehicle(vehicleId) {
    if (!window.confirm('Delete this vehicle?')) return;
    const response = await fetchWithAuth(`/api/vehiculos/${vehicleId}`, { method: 'DELETE' });
    if (!response.ok) {
        showMessage(await readError(response), true);
        return;
    }
    showMessage('Vehicle deleted');
    loadFleet();
}

/* ===== Tabs and bootstrap ===== */

function switchTab(tab) {
    document.querySelectorAll('.tab').forEach((button) =>
        button.classList.toggle('active', button.dataset.tab === tab));
    document.getElementById('section-shipments').classList.toggle('hidden', tab !== 'shipments');
    document.getElementById('section-fleet').classList.toggle('hidden', tab !== 'fleet');
    if (tab === 'fleet') loadFleet();
}

function applyRoleView() {
    document.getElementById('user-name').textContent = state.username;
    const rolesBox = document.getElementById('user-roles');
    state.roles.forEach((role) =>
        rolesBox.appendChild(el('span', { className: 'role-badge', text: role.replace('ROLE_', '') })));

    if (hasAnyRole(ROLE_ADMIN, ROLE_OPERATOR)) {
        document.getElementById('create-panel').classList.remove('hidden');
    }
    if (hasAnyRole(ROLE_ADMIN)) {
        document.getElementById('tab-fleet').classList.remove('hidden');
    }
}

function init() {
    if (!localStorage.getItem('jwt_token')) {
        redirectToLogin();
        return;
    }
    applyRoleView();

    document.getElementById('logout-button').addEventListener('click', redirectToLogin);
    document.getElementById('shipment-form').addEventListener('submit', createShipment);
    document.getElementById('estimate-button').addEventListener('click', estimateFee);
    document.getElementById('vehicle-form').addEventListener('submit', createVehicle);
    document.getElementById('audit-close').addEventListener('click', closeAuditModal);
    document.getElementById('audit-from').addEventListener('change', renderAuditRows);
    document.getElementById('audit-to').addEventListener('change', renderAuditRows);
    document.getElementById('audit-clear').addEventListener('click', () => {
        document.getElementById('audit-from').value = '';
        document.getElementById('audit-to').value = '';
        renderAuditRows();
    });
    document.getElementById('audit-modal').addEventListener('click', (event) => {
        if (event.target.id === 'audit-modal') closeAuditModal();
    });
    document.querySelectorAll('.tab').forEach((button) =>
        button.addEventListener('click', () => switchTab(button.dataset.tab)));

    loadShipments();
}

init();
