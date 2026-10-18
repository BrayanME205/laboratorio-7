USE ExpresoFastc5h753_II2026;
GO

-- 1. Columna destinatario
IF COL_LENGTH('dbo.Envio', 'destinatario') IS NULL
    ALTER TABLE dbo.Envio ADD destinatario VARCHAR(100) NULL;
GO

UPDATE dbo.Envio SET destinatario = CASE codigo_rastreo
    WHEN 'EXP-1001' THEN 'Maria Fernandez'
    WHEN 'EXP-1002' THEN 'Jose Araya'
    WHEN 'EXP-1003' THEN 'Sofia Vargas'
    WHEN 'EXP-1004' THEN 'Luis Campos' END
WHERE destinatario IS NULL AND codigo_rastreo IN ('EXP-1001', 'EXP-1002', 'EXP-1003', 'EXP-1004');
GO

-- 2. Semillas para paginacion (18 envios)
INSERT INTO dbo.Envio (codigo_rastreo, destinatario, direccion_destino, peso_kg, costo, estado_envio, fecha_creacion, vehiculo_id, conductor_id)
SELECT s.codigo, s.destinatario, s.direccion, s.peso, s.costo, s.estado, DATEADD(HOUR, -s.horas, GETDATE()), s.vehiculo, s.conductor
FROM (VALUES
    ('EXP-2001', 'Andrea Solis',    'Paraiso, Cartago',       8.50,  3650.00, 'PENDIENTE',    5,  1, 1),
    ('EXP-2002', 'Bruno Mora',      'Cartago Centro',        12.00,  5050.00, 'PENDIENTE',   10,  1, 1),
    ('EXP-2003', 'Carla Rojas',     'Orosi, Paraiso',        25.00, 10250.00, 'PENDIENTE',   15,  2, 2),
    ('EXP-2004', 'Diego Vargas',    'Turrialba, Cartago',     3.20,  1530.00, 'PENDIENTE',   20,  2, 2),
    ('EXP-2005', 'Elena Chaves',    'Cervantes, Alvarado',   40.00, 16250.00, 'PENDIENTE',   25,  1, 1),
    ('EXP-2006', 'Fabian Quesada',  'Tres Rios, Cartago',     6.00,  2650.00, 'EN_TRANSITO', 30,  1, 1),
    ('EXP-2007', 'Gabriela Soto',   'La Union, Cartago',     18.50,  7650.00, 'EN_TRANSITO', 35,  2, 2),
    ('EXP-2008', 'Hugo Barrantes',  'Cachi, Paraiso',        55.00, 14300.00, 'EN_TRANSITO', 40,  2, 2),
    ('EXP-2009', 'Irene Camacho',   'Juan Vinas, Jimenez',    9.90,  4210.00, 'EN_TRANSITO', 45,  1, 1),
    ('EXP-2010', 'Javier Murillo',  'Pacayas, Alvarado',     14.00,  5850.00, 'EN_TRANSITO', 50,  1, 1),
    ('EXP-2011', 'Karla Jimenez',   'Cartago Centro',         2.50,  1250.00, 'ENTREGADO',   60,  1, 1),
    ('EXP-2012', 'Luis Alvarado',   'Paraiso Centro',        11.00,  4650.00, 'ENTREGADO',   70,  2, 2),
    ('EXP-2013', 'Monica Salas',    'Turrialba Centro',      30.00, 12250.00, 'ENTREGADO',   80,  2, 2),
    ('EXP-2014', 'Nelson Brenes',   'Orosi, Paraiso',         7.70,  3330.00, 'ENTREGADO',   90,  1, 1),
    ('EXP-2015', 'Olga Mendez',     'Tejar, El Guarco',      22.00,  9050.00, 'ENTREGADO',  100,  1, 1),
    ('EXP-2016', 'Pablo Granados',  'Cartago Centro',         4.00,  1850.00, 'CANCELADO',  110,  2, 2),
    ('EXP-2017', 'Rosa Arias',      'Paraiso, Cartago',      16.00,  6650.00, 'CANCELADO',  120,  1, 1),
    ('EXP-2018', 'Sergio Pineda',   'Cervantes, Alvarado',   35.00, 14250.00, 'CANCELADO',  130,  2, 2)
) AS s (codigo, destinatario, direccion, peso, costo, estado, horas, vehiculo, conductor)
WHERE NOT EXISTS (SELECT 1 FROM dbo.Envio e WHERE e.codigo_rastreo = s.codigo);
GO

-- 3. SP_OBTENER_ENVIOS_POR_ESTADO
CREATE OR ALTER PROCEDURE dbo.SP_OBTENER_ENVIOS_POR_ESTADO
    @pEstado VARCHAR(20)
AS
BEGIN
    SET NOCOUNT ON;
    SELECT e.*
    FROM dbo.Envio e
    WHERE e.estado_envio = @pEstado
    ORDER BY e.fecha_creacion DESC;
END;
GO

-- 4. SP_RESUMEN_METRICAS_ENVIOS (reto opcional)
CREATE OR ALTER PROCEDURE dbo.SP_RESUMEN_METRICAS_ENVIOS
AS
BEGIN
    SET NOCOUNT ON;
    SELECT e.estado_envio AS estado, COUNT(*) AS total, SUM(e.costo) AS suma_flete
    FROM dbo.Envio e
    GROUP BY e.estado_envio
    ORDER BY e.estado_envio;
END;
GO