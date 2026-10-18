USE ExpresoFastc5h753_II2026;
GO

-- ===== Roles =====
INSERT INTO dbo.Rol (nombre_rol) VALUES ('ROLE_ADMIN'), ('ROLE_OPERADOR'), ('ROLE_CONDUCTOR');
GO

-- ===== Users (BCrypt strength 10) =====
-- admin / admin123, operador1 / oper123, conductor1 / cond123
INSERT INTO dbo.Usuario (username, password_hash, nombre_completo, email, activo) VALUES
('admin',      '$2b$10$V1UNFqLbFCmloLCP0zk3Mu18tsXKnEjZE7AWmk/PFzqPUULP2JBcO', 'Carlos Alvarado', 'admin@expresofast.cr',      1),
('operador1',  '$2b$10$WOYXi/STLRdPaLIB8QI6guPAtZuFs09Csmpyg5Xrxdz2e011fbCo.', 'Laura Jimenez',   'operador1@expresofast.cr',  1),
('conductor1', '$2b$10$beBZq/ZhIewva6VrtXSu7uBnMHB2TD6w2QMJHJMAtRGXS.kwXHvbS', 'Mario Rojas',     'conductor1@expresofast.cr', 1);
GO

INSERT INTO dbo.UsuarioRol (usuario_id, rol_id)
SELECT u.usuario_id, r.rol_id
FROM (VALUES ('admin', 'ROLE_ADMIN'), ('operador1', 'ROLE_OPERADOR'), ('conductor1', 'ROLE_CONDUCTOR')) AS m (username, nombre_rol)
JOIN dbo.Usuario u ON u.username = m.username
JOIN dbo.Rol r ON r.nombre_rol = m.nombre_rol;
GO

-- ===== Domain data =====
INSERT INTO dbo.EmpresaLogistica (nombre, cedula_juridica, telefono, activa) VALUES
('ExpresoFast S.A.',        '3-101-123456', '2574-0000', 1),
('Transportes del Atlantico', '3-101-654321', '2574-1111', 1);
GO

INSERT INTO dbo.Conductor (nombre_completo, numero_licencia, activo, empresa_id) VALUES
('Mario Rojas',  'LIC-0001', 1, 1),
('Ana Mora',     'LIC-0002', 1, 1),
('Pedro Solano', 'LIC-0003', 0, 2);
GO

INSERT INTO dbo.Vehiculo (placa, marca, modelo, capacidad_kg, empresa_id, conductor_id) VALUES
('ABC-123', 'Toyota', 'Hilux',   1000.00, 1, 1),
('DEF-456', 'Hino',   '300',     3500.00, 1, 2),
('GHI-789', 'Isuzu',  'NPR',     5000.00, 2, NULL);
GO

INSERT INTO dbo.Envio (codigo_rastreo, direccion_destino, peso_kg, costo, estado_envio, fecha_creacion, vehiculo_id, conductor_id) VALUES
('EXP-1001', 'Paraiso, Cartago',      12.50,  5250.00, 'PENDIENTE',   DATEADD(DAY, -3, GETDATE()), 1, 1),
('EXP-1002', 'Turrialba, Cartago',    80.00, 11300.00, 'EN_TRANSITO', DATEADD(DAY, -2, GETDATE()), 2, 2),
('EXP-1003', 'Cartago Centro',         5.00,  2500.00, 'ENTREGADO',   DATEADD(DAY, -5, GETDATE()), 1, 1),
('EXP-1004', 'Orosi, Paraiso',        30.00,  7500.00, 'CANCELADO',   DATEADD(DAY, -4, GETDATE()), 2, 2);
GO

INSERT INTO dbo.BitacoraEnvio (envio_id, estado_anterior, estado_nuevo, fecha_cambio, usuario_id, observaciones)
SELECT e.envio_id, m.anterior, m.nuevo, DATEADD(HOUR, m.horas, e.fecha_creacion), u.usuario_id, m.obs
FROM (VALUES
    ('EXP-1002', 'PENDIENTE',   'EN_TRANSITO', 4, 'admin', 'Dispatched from warehouse'),
    ('EXP-1003', 'PENDIENTE',   'EN_TRANSITO', 2, 'admin', 'Dispatched from warehouse'),
    ('EXP-1003', 'EN_TRANSITO', 'ENTREGADO',   6, 'admin', 'Delivered to customer'),
    ('EXP-1004', 'PENDIENTE',   'CANCELADO',   1, 'admin', 'Cancelled at customer request')
) AS m (codigo, anterior, nuevo, horas, username, obs)
JOIN dbo.Envio e ON e.codigo_rastreo = m.codigo
JOIN dbo.Usuario u ON u.username = m.username;
GO
