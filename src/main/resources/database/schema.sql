PRAGMA foreign_keys = ON;
PRAGMA encoding = 'UTF-8';

BEGIN TRANSACTION;

CREATE TABLE IF NOT EXISTS schema_version (
    id INTEGER PRIMARY KEY CHECK (id = 1),
    version TEXT NOT NULL,
    descripcion TEXT NOT NULL,
    fecha_aplicacion TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS rol (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL UNIQUE COLLATE NOCASE,
    descripcion TEXT NOT NULL,
    activo INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0, 1)),
    fecha_creacion TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS usuario (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    rol_id INTEGER NOT NULL,
    nombre_usuario TEXT NOT NULL UNIQUE COLLATE NOCASE,
    password_hash TEXT NOT NULL,
    password_algoritmo TEXT NOT NULL DEFAULT 'SHA-256',
    nombre_mostrar TEXT NOT NULL,
    correo_electronico TEXT UNIQUE COLLATE NOCASE,
    activo INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0, 1)),
    debe_cambiar_password INTEGER NOT NULL DEFAULT 1 CHECK (debe_cambiar_password IN (0, 1)),
    ultimo_acceso TEXT,
    fecha_creacion TEXT NOT NULL DEFAULT (datetime('now')),
    fecha_actualizacion TEXT,
    CONSTRAINT fk_usuario_rol
        FOREIGN KEY (rol_id)
        REFERENCES rol (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT ck_usuario_nombre_usuario
        CHECK (length(trim(nombre_usuario)) >= 3),
    CONSTRAINT ck_usuario_password_hash
        CHECK (length(password_hash) >= 64),
    CONSTRAINT ck_usuario_correo
        CHECK (correo_electronico IS NULL OR instr(correo_electronico, '@') > 1)
);

CREATE TABLE IF NOT EXISTS pais (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL UNIQUE COLLATE NOCASE,
    codigo_iso TEXT NOT NULL UNIQUE COLLATE NOCASE,
    activo INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0, 1)),
    fecha_creacion TEXT NOT NULL DEFAULT (datetime('now')),
    fecha_actualizacion TEXT,
    CONSTRAINT ck_pais_nombre
        CHECK (length(trim(nombre)) >= 2),
    CONSTRAINT ck_pais_codigo_iso
        CHECK (length(trim(codigo_iso)) BETWEEN 2 AND 3)
);

CREATE TABLE IF NOT EXISTS departamento (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    empresa_id INTEGER NOT NULL,
    nombre TEXT NOT NULL COLLATE NOCASE,
    presupuesto NUMERIC NOT NULL DEFAULT 0 CHECK (presupuesto >= 0),
    activo INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0, 1)),
    fecha_creacion TEXT NOT NULL DEFAULT (datetime('now')),
    fecha_actualizacion TEXT,
    CONSTRAINT fk_departamento_empresa
        FOREIGN KEY (empresa_id)
        REFERENCES empresa (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT uq_departamento_empresa_nombre
        UNIQUE (empresa_id, nombre),
    CONSTRAINT ck_departamento_nombre
        CHECK (length(trim(nombre)) >= 2)
);

CREATE TABLE IF NOT EXISTS cargo (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL UNIQUE COLLATE NOCASE,
    descripcion TEXT,
    activo INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0, 1)),
    fecha_creacion TEXT NOT NULL DEFAULT (datetime('now')),
    fecha_actualizacion TEXT,
    CONSTRAINT ck_cargo_nombre
        CHECK (length(trim(nombre)) >= 2)
);

CREATE TABLE IF NOT EXISTS persona (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    identidad TEXT NOT NULL UNIQUE COLLATE NOCASE,
    nombres TEXT NOT NULL COLLATE NOCASE,
    apellidos TEXT NOT NULL COLLATE NOCASE,
    telefono TEXT,
    correo_electronico TEXT UNIQUE COLLATE NOCASE,
    direccion TEXT,
    fecha_nacimiento TEXT,
    activo INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0, 1)),
    fecha_creacion TEXT NOT NULL DEFAULT (datetime('now')),
    fecha_actualizacion TEXT,
    CONSTRAINT ck_persona_identidad
        CHECK (length(trim(identidad)) >= 5),
    CONSTRAINT ck_persona_nombres
        CHECK (length(trim(nombres)) >= 2),
    CONSTRAINT ck_persona_apellidos
        CHECK (length(trim(apellidos)) >= 2),
    CONSTRAINT ck_persona_correo
        CHECK (correo_electronico IS NULL OR instr(correo_electronico, '@') > 1),
    CONSTRAINT ck_persona_fecha_nacimiento
        CHECK (fecha_nacimiento IS NULL OR date(fecha_nacimiento) IS NOT NULL)
);

CREATE TABLE IF NOT EXISTS empleado (
    id INTEGER PRIMARY KEY,
    cargo_id INTEGER NOT NULL,
    departamento_id INTEGER NOT NULL,
    codigo_empleado TEXT NOT NULL UNIQUE COLLATE NOCASE,
    fecha_contratacion TEXT NOT NULL,
    salario NUMERIC NOT NULL DEFAULT 0 CHECK (salario >= 0),
    foto_ruta TEXT,
    fecha_creacion TEXT NOT NULL DEFAULT (datetime('now')),
    fecha_actualizacion TEXT,
    CONSTRAINT fk_empleado_persona
        FOREIGN KEY (id)
        REFERENCES persona (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_empleado_cargo
        FOREIGN KEY (cargo_id)
        REFERENCES cargo (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_empleado_departamento
        FOREIGN KEY (departamento_id)
        REFERENCES departamento (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT ck_empleado_codigo
        CHECK (length(trim(codigo_empleado)) >= 3),
    CONSTRAINT ck_empleado_fecha_contratacion
        CHECK (date(fecha_contratacion) IS NOT NULL)
);

CREATE TABLE IF NOT EXISTS empresa (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    pais_id INTEGER NOT NULL,
    nombre TEXT NOT NULL COLLATE NOCASE,
    rtn TEXT NOT NULL UNIQUE COLLATE NOCASE,
    telefono TEXT,
    correo_electronico TEXT UNIQUE COLLATE NOCASE,
    direccion TEXT,
    activo INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0, 1)),
    fecha_creacion TEXT NOT NULL DEFAULT (datetime('now')),
    fecha_actualizacion TEXT,
    CONSTRAINT fk_empresa_pais
        FOREIGN KEY (pais_id)
        REFERENCES pais (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT ck_empresa_nombre
        CHECK (length(trim(nombre)) >= 2),
    CONSTRAINT ck_empresa_rtn
        CHECK (length(trim(rtn)) >= 8),
    CONSTRAINT ck_empresa_correo
        CHECK (correo_electronico IS NULL OR instr(correo_electronico, '@') > 1)
);

CREATE TABLE IF NOT EXISTS proyecto (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    empresa_id INTEGER NOT NULL,
    codigo TEXT NOT NULL UNIQUE COLLATE NOCASE,
    nombre TEXT NOT NULL COLLATE NOCASE,
    descripcion TEXT,
    fecha_inicio TEXT NOT NULL,
    fecha_fin TEXT,
    presupuesto NUMERIC NOT NULL DEFAULT 0 CHECK (presupuesto >= 0),
    activo INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0, 1)),
    fecha_creacion TEXT NOT NULL DEFAULT (datetime('now')),
    fecha_actualizacion TEXT,
    CONSTRAINT fk_proyecto_empresa
        FOREIGN KEY (empresa_id)
        REFERENCES empresa (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT ck_proyecto_codigo
        CHECK (length(trim(codigo)) >= 3),
    CONSTRAINT ck_proyecto_nombre
        CHECK (length(trim(nombre)) >= 2),
    CONSTRAINT ck_proyecto_fecha_inicio
        CHECK (date(fecha_inicio) IS NOT NULL),
    CONSTRAINT ck_proyecto_fecha_fin
        CHECK (fecha_fin IS NULL OR date(fecha_fin) IS NOT NULL),
    CONSTRAINT ck_proyecto_rango_fechas
        CHECK (fecha_fin IS NULL OR date(fecha_fin) >= date(fecha_inicio))
);

CREATE TABLE IF NOT EXISTS asignacion (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    empleado_id INTEGER NOT NULL,
    proyecto_id INTEGER NOT NULL,
    rol TEXT NOT NULL COLLATE NOCASE,
    fecha_inicio TEXT NOT NULL,
    fecha_fin TEXT,
    horas_asignadas INTEGER NOT NULL DEFAULT 0 CHECK (horas_asignadas >= 0),
    activo INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0, 1)),
    fecha_creacion TEXT NOT NULL DEFAULT (datetime('now')),
    fecha_actualizacion TEXT,
    CONSTRAINT fk_asignacion_empleado
        FOREIGN KEY (empleado_id)
        REFERENCES empleado (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_asignacion_proyecto
        FOREIGN KEY (proyecto_id)
        REFERENCES proyecto (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT uq_asignacion_empleado_proyecto_rol
        UNIQUE (empleado_id, proyecto_id, rol),
    CONSTRAINT ck_asignacion_rol
        CHECK (length(trim(rol)) >= 2),
    CONSTRAINT ck_asignacion_fecha_inicio
        CHECK (date(fecha_inicio) IS NOT NULL),
    CONSTRAINT ck_asignacion_fecha_fin
        CHECK (fecha_fin IS NULL OR date(fecha_fin) IS NOT NULL),
    CONSTRAINT ck_asignacion_rango_fechas
        CHECK (fecha_fin IS NULL OR date(fecha_fin) >= date(fecha_inicio))
);

CREATE INDEX IF NOT EXISTS idx_usuario_rol_id
    ON usuario (rol_id);

CREATE INDEX IF NOT EXISTS idx_departamento_empresa_id
    ON departamento (empresa_id);

CREATE INDEX IF NOT EXISTS idx_persona_nombre_completo
    ON persona (apellidos, nombres);

CREATE INDEX IF NOT EXISTS idx_empleado_cargo_id
    ON empleado (cargo_id);

CREATE INDEX IF NOT EXISTS idx_empleado_departamento_id
    ON empleado (departamento_id);

CREATE INDEX IF NOT EXISTS idx_empresa_pais_id
    ON empresa (pais_id);

CREATE INDEX IF NOT EXISTS idx_proyecto_empresa_id
    ON proyecto (empresa_id);

CREATE INDEX IF NOT EXISTS idx_proyecto_fechas
    ON proyecto (fecha_inicio, fecha_fin);

CREATE INDEX IF NOT EXISTS idx_asignacion_empleado_id
    ON asignacion (empleado_id);

CREATE INDEX IF NOT EXISTS idx_asignacion_proyecto_id
    ON asignacion (proyecto_id);

CREATE INDEX IF NOT EXISTS idx_asignacion_fechas
    ON asignacion (fecha_inicio, fecha_fin);

INSERT OR IGNORE INTO schema_version (id, version, descripcion)
VALUES (1, '1.0.0', 'Estructura inicial de base de datos');

INSERT OR IGNORE INTO rol (id, nombre, descripcion)
VALUES
    (1, 'Administrador', 'Acceso completo al sistema.'),
    (2, 'Consulta', 'Acceso de solo consulta a la informacion.');

INSERT OR IGNORE INTO usuario (
    id,
    rol_id,
    nombre_usuario,
    password_hash,
    password_algoritmo,
    nombre_mostrar,
    correo_electronico,
    activo,
    debe_cambiar_password
)
VALUES
    (
        1,
        1,
        'admin',
        '0a5bc3e342432f1bad92ffd51b785343ec72906cdba6a26131060b008e786656',
        'SHA-256',
        'Administrador',
        'admin@sistema.local',
        1,
        1
    ),
    (
        2,
        2,
        'consulta',
        '824ae985fa847e5c7b2aec9e7ecaf165a9093015b585110783dc79db1abcfa8b',
        'SHA-256',
        'Consulta',
        'consulta@sistema.local',
        1,
        1
    );

INSERT OR IGNORE INTO pais (id, nombre, codigo_iso)
VALUES
    (1, 'Honduras', 'HN'),
    (2, 'Guatemala', 'GT'),
    (3, 'El Salvador', 'SV'),
    (4, 'Nicaragua', 'NI'),
    (5, 'Costa Rica', 'CR'),
    (6, 'Panama', 'PA');

INSERT OR IGNORE INTO empresa (id, pais_id, nombre, rtn, telefono, correo_electronico, direccion)
VALUES
    (1, 1, 'Universidad Tecnologica de Honduras', '08019999000001', '2234-5678', 'contacto@uth.local', 'Campus principal');

INSERT OR IGNORE INTO departamento (id, empresa_id, nombre, presupuesto, activo)
VALUES
    (1, 1, 'Recursos Humanos', 0, 1),
    (2, 1, 'Finanzas', 0, 1),
    (3, 1, 'Tecnologia', 0, 1),
    (4, 1, 'Administracion', 0, 1),
    (5, 1, 'Ventas', 0, 1);
INSERT OR IGNORE INTO cargo (id, nombre, descripcion)
VALUES
    (1, 'Administrador de Sistemas', 'Responsable de administrar configuraciones y seguridad del sistema.'),
    (2, 'Gerente de Proyecto', 'Responsable de planificar y supervisar proyectos.'),
    (3, 'Analista', 'Responsable de analizar informacion operativa y tecnica.'),
    (4, 'Desarrollador', 'Responsable de construir y mantener soluciones de software.'),
    (5, 'Consultor', 'Responsable de apoyar procesos de negocio y seguimiento.');

COMMIT;

PRAGMA foreign_key_check;

