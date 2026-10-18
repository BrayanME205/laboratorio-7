IF DB_ID(N'ExpresoFastc5h753_II2026') IS NULL
    CREATE DATABASE ExpresoFastc5h753_II2026;
GO

USE ExpresoFastc5h753_II2026;
GO

-- ===== EmpresaLogistica =====
IF OBJECT_ID(N'dbo.EmpresaLogistica', N'U') IS NULL
CREATE TABLE dbo.EmpresaLogistica (
    empresa_id      INT IDENTITY(1,1) PRIMARY KEY,
    nombre          VARCHAR(100) NOT NULL,
    cedula_juridica VARCHAR(20)  NOT NULL UNIQUE,
    telefono        VARCHAR(20)  NULL,
    activa          BIT          NOT NULL DEFAULT 1
);
GO

-- ===== Conductor =====
IF OBJECT_ID(N'dbo.Conductor', N'U') IS NULL
CREATE TABLE dbo.Conductor (
    conductor_id    INT IDENTITY(1,1) PRIMARY KEY,
    nombre_completo VARCHAR(100) NOT NULL,
    numero_licencia VARCHAR(30)  NOT NULL UNIQUE,
    activo          BIT          NOT NULL DEFAULT 1,
    empresa_id      INT          NULL,
    CONSTRAINT FK_Conductor_Empresa FOREIGN KEY (empresa_id) REFERENCES dbo.EmpresaLogistica (empresa_id)
);
GO

-- ===== Vehiculo =====
IF OBJECT_ID(N'dbo.Vehiculo', N'U') IS NULL
CREATE TABLE dbo.Vehiculo (
    vehiculo_id  INT IDENTITY(1,1) PRIMARY KEY,
    placa        VARCHAR(10)   NOT NULL UNIQUE,
    marca        VARCHAR(50)   NOT NULL,
    modelo       VARCHAR(50)   NOT NULL,
    capacidad_kg DECIMAL(10,2) NOT NULL,
    empresa_id   INT           NOT NULL,
    conductor_id INT           NULL,
    CONSTRAINT FK_Vehiculo_Empresa   FOREIGN KEY (empresa_id)   REFERENCES dbo.EmpresaLogistica (empresa_id),
    CONSTRAINT FK_Vehiculo_Conductor FOREIGN KEY (conductor_id) REFERENCES dbo.Conductor (conductor_id)
);
GO

-- ===== Envio =====
IF OBJECT_ID(N'dbo.Envio', N'U') IS NULL
CREATE TABLE dbo.Envio (
    envio_id          INT IDENTITY(1,1) PRIMARY KEY,
    codigo_rastreo    VARCHAR(20)   NOT NULL UNIQUE,
    direccion_destino VARCHAR(250)  NOT NULL,
    peso_kg           DECIMAL(10,2) NOT NULL,
    costo             DECIMAL(12,2) NOT NULL,
    estado_envio      VARCHAR(20)   NOT NULL,
    fecha_creacion    DATETIME      NOT NULL DEFAULT GETDATE(),
    vehiculo_id       INT           NOT NULL,
    conductor_id      INT           NOT NULL,
    CONSTRAINT CK_Envio_Estado CHECK (estado_envio IN ('PENDIENTE', 'EN_TRANSITO', 'ENTREGADO', 'CANCELADO')),
    CONSTRAINT FK_Envio_Vehiculo   FOREIGN KEY (vehiculo_id)  REFERENCES dbo.Vehiculo (vehiculo_id),
    CONSTRAINT FK_Envio_Conductor  FOREIGN KEY (conductor_id) REFERENCES dbo.Conductor (conductor_id)
);
GO

CREATE INDEX IX_Envio_Estado ON dbo.Envio (estado_envio);
GO
