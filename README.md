# ExpresoFast – Laboratorios 6 y 7

**Universidad de Costa Rica – Sede del Atlántico, Recinto Paraíso**
Carrera de Informática Empresarial
**Curso:** IF0009 – Desarrollo de Software IV
**Profesor:** Mag. Jonathan Granados C.
**Semestre:** II-2026
**Laboratorios:** 6, 7 y 8 
**Estudiante:** Brayan Murillo Estrada
**Carné:** c5h753

---

## 1. Requisitos de entorno

| Componente | Versión |
|---|---|
| Java (JDK) | 21 |
| Maven | 3.9+ |
| Microsoft SQL Server | Developer Edition 2019 o superior (TCP/IP habilitado en el puerto 1433) |
| Spring Boot | 3.3.5 |
| Navegador | Chrome / Edge / Firefox actualizado |
| Servidor estático para el frontend | VS Code Live Server (puerto 5500) o `python -m http.server 5500` |

## 2. Estructura del repositorio

```
expresofast-lab6-c5h753/
├── backend/            API REST (Spring Boot)
│   ├── src/
│   ├── pom.xml
│   └── application.properties.template
├── database/           Scripts SQL Server
│   ├── 01_schema_lab5.sql
│   ├── 02_schema_lab6_extension.sql
│   └── 03_data_seeds.sql
├── frontend/           Portal web (HTML, CSS, JS)
├── docs/               Colección Postman y script de commits
└── README.md
```

Paquete base: `cr.ac.ucr.paraiso.ie.c5h753.expresofast`
(`domain`, `dto`, `repository`, `service`, `controller`, `security`, `config`, `exception`).

## 3. Configuración de la base de datos

Desde SSMS o `sqlcmd`, ejecute en orden:

1. `database/01_schema_lab5.sql` – crea la base `ExpresoFastc5h753_II2026` y las tablas del dominio.
2. `database/02_schema_lab6_extension.sql` – crea `Usuario`, `Rol`, `UsuarioRol` y `BitacoraEnvio`.
3. `database/03_data_seeds.sql` – inserta roles, usuarios (contraseñas con BCrypt), empresas, conductores, vehículos y envíos de ejemplo.

```bash
sqlcmd -S localhost -U sa -P "<password>" -i database/01_schema_lab5.sql
sqlcmd -S localhost -U sa -P "<password>" -i database/02_schema_lab6_extension.sql
sqlcmd -S localhost -U sa -P "<password>" -i database/03_data_seeds.sql
```

### Credenciales de conexión

`backend/src/main/resources/application.properties` lee la conexión desde variables de entorno, con valores por defecto para desarrollo local:

| Variable | Valor por defecto |
|---|---|
| `DB_URL` | `jdbc:sqlserver://localhost:1433;databaseName=ExpresoFastc5h753_II2026;encrypt=true;trustServerCertificate=true` |
| `DB_USERNAME` | `sa` |
| `DB_PASSWORD` | `YourStrong@Passw0rd` |
| `JWT_SECRET` | clave de desarrollo (mínimo 32 caracteres) |
| `JWT_EXPIRATION_MS` | `3600000` (1 hora) |
| `CORS_ORIGINS` | `http://127.0.0.1:5500,http://localhost:5500,http://localhost:3000,http://127.0.0.1:3000` |

Ajuste `DB_PASSWORD` a su instancia local (variable de entorno o editando el archivo). `application.properties.template` muestra la plantilla sin credenciales.

## 4. Usuarios de prueba

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `admin123` | `ROLE_ADMIN` |
| `operador1` | `oper123` | `ROLE_OPERADOR` |
| `conductor1` | `cond123` | `ROLE_CONDUCTOR` |

## 5. Instrucciones de ejecución

### Backend

```bash
cd backend
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

### Frontend

```bash
cd frontend
python -m http.server 5500
```

Abra `http://127.0.0.1:5500/index.html`. (También funciona con la extensión Live Server de VS Code.)
La URL del backend se configura en la constante `API_BASE_URL` de `app.js` y `login.html`.

## 6. Matriz de permisos (RBAC)

| Endpoint | Método | Roles |
|---|---|---|
| `/api/auth/login` | POST | Público |
| `/api/envios/optimizados` | GET | ADMIN, OPERADOR, CONDUCTOR |
| `/api/envios/{id}` | GET | ADMIN, OPERADOR, CONDUCTOR |
| `/api/envios` | POST | ADMIN, OPERADOR |
| `/api/envios/{id}/estado` | PATCH | ADMIN, OPERADOR, CONDUCTOR |
| `/api/envios/{id}/cancelar` | PATCH | ADMIN, OPERADOR |
| `/api/envios/{id}/bitacora` | GET | ADMIN, OPERADOR |
| `/api/envios/tarifa` | GET | ADMIN, OPERADOR |
| `/api/vehiculos/**` | ALL | ADMIN |
| `/api/empresas/**` | ALL | ADMIN |

Reglas de transición de estado: `PENDIENTE → EN_TRANSITO | CANCELADO`, `EN_TRANSITO → ENTREGADO`. Los estados `ENTREGADO` y `CANCELADO` son finales; una transición inválida responde `400` con el mensaje `Transición de estado no permitida para el envío [CÓDIGO]`.

Matriz de tarifas (`EnvioService.calcularTarifa`):

| Peso | Fórmula |
|---|---|
| ≤ 50 kg | 250 + 400 × kg + 25 × km |
| > 50 kg | 500 + 110 × kg + 200 × km |

## 7. Pruebas y cobertura (Laboratorio 7)

```bash
cd backend
mvn clean test       # ejecuta la suite completa y genera el reporte JaCoCo
mvn clean verify     # además exige >= 85% de cobertura de instrucciones en el paquete service
```

Reporte visual de cobertura: `backend/target/site/jacoco/index.html`

| Clase de prueba | Alcance |
|---|---|
| `EnvioServiceTest` | Creación, transiciones de estado, cancelación, bitácora, tarifas parametrizadas |
| `VehiculoServiceTest` | Registro, placa duplicada, asignación de conductor, CRUD |
| `EmpresaLogisticaServiceTest` | CRUD y cédula jurídica duplicada |
| `AuthServiceTest` | Login exitoso y fallido |
| `EnvioControllerTest` | `@WebMvcTest` + MockMvc: 200, 201, 400, 404, 500 y formato RFC 7807 |
| `AuthControllerTest` | Login 200, 401 y validación 400 |
| `JwtTokenProviderTest`, `EstadoEnvioTest` | Token JWT y máquina de estados |

## 8. Colección Postman

Importe `docs/ExpresoFast_Postman_Collection.json`. Ejecute primero cualquier request de **Auth**; el token se guarda automáticamente en la variable `token`.

## 9. Historial de commits

`docs/commit-history.sh` crea un historial de 11 commits semánticos (`feat`, `test`, `docs`, `chore`) a partir de los archivos del proyecto.
