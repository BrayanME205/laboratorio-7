const paginaState = { page: 0, data: null };

function leerFiltros() {
    return {
        busqueda: document.getElementById('busqueda').value.trim(),
        estado: document.getElementById('estadoFiltro').value,
        sortBy: document.getElementById('sortBy').value,
        direction: document.getElementById('direction').value,
        size: document.getElementById('pageSize').value,
        procedimiento: document.getElementById('procedimiento').value
    };
}

function mostrarMensaje(textos, esError) {
    const box = document.getElementById('message');
    renderAlert(box, textos, esError);
}

async function errorDeRespuesta(response) {
    mostrarMensaje(await readErrors(response), true);
}

function pintarFilas(envios) {
    const body = document.getElementById('enviosBody');
    body.replaceChildren();
    if (envios.length === 0) {
        const fila = el('tr', {}, [el('td', { text: 'No hay envíos para los filtros seleccionados.' })]);
        fila.firstChild.colSpan = 6;
        body.appendChild(fila);
        return;
    }
    envios.forEach((envio) => body.appendChild(el('tr', {}, [
        el('td', { text: envio.codigoRastreo }),
        el('td', { text: envio.destinatario || '-' }),
        el('td', { text: envio.direccionDestino }),
        el('td', { text: formatMoney(envio.montoFlete) }),
        el('td', { text: ESTADOS[envio.estado] || envio.estado }),
        el('td', { text: new Date(envio.fechaCreacion).toLocaleDateString('es-CR') })
    ])));
}

function actualizarPaginador(data) {
    const info = document.getElementById('paginaInfo');
    const sinResultados = data.totalPages === 0;
    // Spring Data usa base 0: se muestra number + 1 pero se envía number (base 0).
    info.textContent = sinResultados
        ? 'Sin resultados (Total: 0 envíos)'
        : `Página ${data.number + 1} de ${data.totalPages} (Total: ${data.totalElements} envíos)`;
    document.getElementById('btnPrimera').disabled = data.first;
    document.getElementById('btnAnterior').disabled = data.first;
    document.getElementById('btnSiguiente').disabled = data.last;
    document.getElementById('btnUltima').disabled = data.last;
}

function paginadorModoProcedimiento(total, estado) {
    document.getElementById('paginaInfo').textContent =
        `Resultado de SP_OBTENER_ENVIOS_POR_ESTADO (${estado}): ${total} envíos`;
    ['btnPrimera', 'btnAnterior', 'btnSiguiente', 'btnUltima']
        .forEach((id) => { document.getElementById(id).disabled = true; });
}

async function cargarEnvios() {
    const filtros = leerFiltros();
    const origen = document.getElementById('origenDatos');
    try {
        if (filtros.procedimiento) {
            const response = await fetchWithAuth(
                `/api/v1/envios/procedimiento/${encodeURIComponent(filtros.procedimiento)}`);
            if (!response.ok) { await errorDeRespuesta(response); return; }
            const envios = await response.json();
            origen.textContent = 'Origen: procedimiento almacenado (sin paginación).';
            pintarFilas(envios);
            paginadorModoProcedimiento(envios.length, filtros.procedimiento);
            return;
        }

        const params = new URLSearchParams({
            page: String(paginaState.page),
            size: filtros.size,
            sortBy: filtros.sortBy,
            direction: filtros.direction
        });
        if (filtros.busqueda) params.set('busqueda', filtros.busqueda);
        if (filtros.estado) params.set('estado', filtros.estado);

        const response = await fetchWithAuth(`/api/v1/envios?${params.toString()}`);
        if (!response.ok) { await errorDeRespuesta(response); return; }
        const data = await response.json();
        paginaState.data = data;
        paginaState.page = data.number;
        origen.textContent = 'Origen: consulta paginada en base de datos (Pageable).';
        pintarFilas(data.content);
        actualizarPaginador(data);
    } catch (error) {
        if (sessionStorage.getItem('jwt_token')) {
            mostrarMensaje(['No se pudo conectar con el servidor. Verifique que el backend esté activo'], true);
        }
    }
}

async function cargarMetricas() {
    const body = document.getElementById('metricasBody');
    const response = await fetchWithAuth('/api/v1/envios/metricas');
    if (!response.ok) { await errorDeRespuesta(response); return; }
    const metricas = await response.json();
    body.replaceChildren();
    metricas.forEach((m) => body.appendChild(el('tr', {}, [
        el('td', { text: ESTADOS[m.estado] || m.estado }),
        el('td', { text: m.total }),
        el('td', { text: formatMoney(m.sumaFlete) })
    ])));
}

function irAPagina(numero) {
    paginaState.page = Math.max(0, numero);
    cargarEnvios();
}

function initPaginado() {
    const session = getSession();
    if (!session) { redirectToLogin(); return; }
    if (!session.roles.includes(ROLE_ADMIN) && !session.roles.includes(ROLE_OPERADOR)) {
        window.location.replace('dashboard.html');
        return;
    }
    document.getElementById('userName').textContent = session.username;
    const rolesBox = document.getElementById('userRoles');
    session.roles.forEach((role) =>
        rolesBox.appendChild(el('span', { className: 'role-badge', text: role.replace('ROLE_', '') })));

    document.getElementById('logoutButton').addEventListener('click', redirectToLogin);
    document.getElementById('filtrosForm').addEventListener('submit', (event) => {
        event.preventDefault();
        irAPagina(0);
    });
    ['estadoFiltro', 'sortBy', 'direction', 'pageSize', 'procedimiento'].forEach((id) =>
        document.getElementById(id).addEventListener('change', () => irAPagina(0)));
    document.getElementById('limpiarButton').addEventListener('click', () => {
        document.getElementById('filtrosForm').reset();
        irAPagina(0);
    });

    document.getElementById('btnPrimera').addEventListener('click', () => irAPagina(0));
    document.getElementById('btnAnterior').addEventListener('click', () => irAPagina(paginaState.page - 1));
    document.getElementById('btnSiguiente').addEventListener('click', () => irAPagina(paginaState.page + 1));
    document.getElementById('btnUltima').addEventListener('click', () =>
        irAPagina(paginaState.data ? paginaState.data.totalPages - 1 : 0));

    cargarEnvios();
    cargarMetricas();
}

if (document.body.dataset.page === 'paginado') {
    initPaginado();
}