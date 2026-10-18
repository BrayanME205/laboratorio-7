USE ExpresoFastc5h753_II2026;
GO

-- ===== Usuario =====
IF OBJECT_ID(N'dbo.Usuario', N'U') IS NULL
CREATE TABLE dbo.Usuario (
    usuario_id      INT IDENTITY(1,1) PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    nombre_completo VARCHAR(100) NOT NULL,
    email           VARCHAR(100) NOT NULL UNIQUE,
    activo          BIT          NOT NULL DEFAULT 1
);
GO

-- ===== Rol =====
IF OBJECT_ID(N'dbo.Rol', N'U') IS NULL
CREATE TABLE dbo.Rol (
    rol_id     INT IDENTITY(1,1) PRIMARY KEY,
    nombre_rol VARCHAR(30) NOT NULL UNIQUE,
    CONSTRAINT CK_Rol_Nombre CHECK (nombre_rol IN ('ROLE_ADMIN', 'ROLE_OPERADOR', 'ROLE_CONDUCTOR'))
);
GO

-- ===== UsuarioRol =====
IF OBJECT_ID(N'dbo.UsuarioRol', N'U') IS NULL
CREATE TABLE dbo.UsuarioRol (
    usuario_id INT NOT NULL,
    rol_id     INT NOT NULL,
    CONSTRAINT PK_UsuarioRol PRIMARY KEY (usuario_id, rol_id),
    CONSTRAINT FK_UsuarioRol_Usuario FOREIGN KEY (usuario_id) REFERENCES dbo.Usuario (usuario_id),
    CONSTRAINT FK_UsuarioRol_Rol     FOREIGN KEY (rol_id)     REFERENCES dbo.Rol (rol_id)
);
GO

-- ===== BitacoraEnvio =====
IF OBJECT_ID(N'dbo.BitacoraEnvio', N'U') IS NULL
CREATE TABLE dbo.BitacoraEnvio (
    bitacora_id     INT IDENTITY(1,1) PRIMARY KEY,
    envio_id        INT          NOT NULL,
    estado_anterior VARCHAR(20)  NOT NULL,
    estado_nuevo    VARCHAR(20)  NOT NULL,
    fecha_cambio    DATETIME     NOT NULL,
    usuario_id      INT          NOT NULL,
    observaciones   VARCHAR(250) NULL,
    CONSTRAINT FK_Bitacora_Envio   FOREIGN KEY (envio_id)   REFERENCES dbo.Envio (envio_id),
    CONSTRAINT FK_Bitacora_Usuario FOREIGN KEY (usuario_id) REFERENCES dbo.Usuario (usuario_id)
);
GO

CREATE INDEX IX_Bitacora_Envio_Fecha ON dbo.BitacoraEnvio (envio_id, fecha_cambio DESC);
GO
