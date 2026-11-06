-- Esquema SQLite para Makila: Asistente Inteligente de Inventario Biomédico

CREATE TABLE IF NOT EXISTS reactivos_quimicos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    formula TEXT NOT NULL,
    cantidad REAL NOT NULL CHECK(cantidad >= 0),
    unidad TEXT NOT NULL,
    ubicacion TEXT NOT NULL,
    fecha_caducidad TEXT NOT NULL,
    estado_seguridad TEXT NOT NULL,
    temp_max_celsius REAL,
    requiere_aislamiento_luz INTEGER DEFAULT 0,
    msds_resumen TEXT
);

CREATE TABLE IF NOT EXISTS aleaciones_biomateriales (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tipo_material TEXT NOT NULL,
    nombre TEXT NOT NULL,
    lote TEXT NOT NULL UNIQUE,
    cantidad_disponible REAL NOT NULL CHECK(cantidad_disponible >= 0),
    unidad TEXT NOT NULL,
    especificaciones_tecnicas TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS equipos_caracterizacion (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre_equipo TEXT NOT NULL,
    estado_actual TEXT NOT NULL CHECK(estado_actual IN ('OPERATIVO', 'EN_MANTENIMIENTO', 'FUERA_DE_SERVICIO')),
    fecha_proxima_calibracion TEXT NOT NULL,
    ubicacion TEXT NOT NULL,
    responsable TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS historial_movimientos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tipo_movimiento TEXT NOT NULL CHECK(tipo_movimiento IN ('ENTRADA', 'SALIDA', 'CONSUMO')),
    categoria TEXT NOT NULL CHECK(categoria IN ('REACTIVO', 'BIOMATERIAL', 'EQUIPO')),
    item_id INTEGER NOT NULL,
    item_nombre TEXT NOT NULL,
    cantidad REAL NOT NULL CHECK(cantidad > 0),
    unidad TEXT NOT NULL,
    experimento_o_lote TEXT NOT NULL,
    usuario_responsable TEXT NOT NULL,
    fecha_hora TEXT NOT NULL,
    notas TEXT
);

CREATE INDEX IF NOT EXISTS idx_reactivos_nombre ON reactivos_quimicos(nombre);
CREATE INDEX IF NOT EXISTS idx_biomat_tipo ON aleaciones_biomateriales(tipo_material);
CREATE INDEX IF NOT EXISTS idx_equipos_estado ON equipos_caracterizacion(estado_actual);
CREATE INDEX IF NOT EXISTS idx_movimientos_fecha ON historial_movimientos(fecha_hora);

