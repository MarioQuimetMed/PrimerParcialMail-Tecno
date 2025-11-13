-- =====================================================
-- SISTEMA DE GESTIÓN DE AGENCIA DE VIAJES
-- Base de Datos - PostgreSQL
-- =====================================================

-- =====================================================
-- CU1 - GESTIÓN DE USUARIOS
-- =====================================================
CREATE TABLE USUARIO
(
    id       SERIAL PRIMARY KEY,
    nombre   VARCHAR(100)        NOT NULL,
    apellido VARCHAR(100)        NOT NULL,
    cedula   VARCHAR(20)         NOT NULL UNIQUE,
    email    VARCHAR(255) UNIQUE NOT NULL,
    telefono VARCHAR(20),
    password VARCHAR(255)        NOT NULL,
    rol      VARCHAR(20)         NOT NULL CHECK (rol IN ('PROPIETARIO', 'VENDEDOR', 'CLIENTE')),
    activo   BOOLEAN DEFAULT TRUE,
    fecha_registro TIMESTAMP DEFAULT NOW()
);
CREATE INDEX idx_usuario_rol ON USUARIO (rol);
CREATE INDEX idx_usuario_activo ON USUARIO (activo);
CREATE INDEX idx_usuario_email ON USUARIO (email);
CREATE INDEX idx_usuario_cedula ON USUARIO (cedula);

-- =====================================================
-- CU2 - GESTIÓN DE DESTINOS
-- =====================================================
CREATE TABLE DESTINO
(
    id          SERIAL PRIMARY KEY,
    nombre      VARCHAR(200) NOT NULL,
    pais        VARCHAR(100) NOT NULL,
    ciudad      VARCHAR(100) NOT NULL,
    descripcion TEXT,
    clima       VARCHAR(50),
    idioma      VARCHAR(50),
    moneda      VARCHAR(50),
    precio_base DECIMAL(10, 2) NOT NULL CHECK (precio_base >= 0),
    imagen_url  VARCHAR(500),
    activo      BOOLEAN DEFAULT TRUE,
    CONSTRAINT uk_destino_nombre_ciudad UNIQUE (nombre, ciudad)
);
CREATE INDEX idx_destino_pais ON DESTINO (pais);
CREATE INDEX idx_destino_ciudad ON DESTINO (ciudad);
CREATE INDEX idx_destino_activo ON DESTINO (activo);
CREATE INDEX idx_destino_precio ON DESTINO (precio_base);

-- =====================================================
-- CU3 - GESTIÓN DE PLANES DE VIAJE
-- =====================================================
CREATE TABLE PLAN_VIAJE
(
    id                 SERIAL PRIMARY KEY,
    nombre             VARCHAR(200) NOT NULL,
    descripcion        TEXT,
    destino_id         INTEGER      NOT NULL,
    duracion_dias      INTEGER      NOT NULL CHECK (duracion_dias > 0),
    precio_total       DECIMAL(10, 2) NOT NULL CHECK (precio_total >= 0),
    incluye_hotel      BOOLEAN DEFAULT TRUE,
    incluye_transporte BOOLEAN DEFAULT TRUE,
    incluye_comidas    BOOLEAN DEFAULT FALSE,
    categoria          VARCHAR(50) CHECK (categoria IN ('ECONOMICO', 'ESTANDAR', 'PREMIUM', 'LUJO')),
    cupo_maximo        INTEGER CHECK (cupo_maximo > 0),
    activo             BOOLEAN DEFAULT TRUE,
    fecha_creacion     TIMESTAMP DEFAULT NOW(),
    CONSTRAINT fk_plan_destino FOREIGN KEY (destino_id)
        REFERENCES DESTINO (id) ON DELETE RESTRICT
);
CREATE INDEX idx_plan_destino ON PLAN_VIAJE (destino_id);
CREATE INDEX idx_plan_categoria ON PLAN_VIAJE (categoria);
CREATE INDEX idx_plan_activo ON PLAN_VIAJE (activo);
CREATE INDEX idx_plan_precio ON PLAN_VIAJE (precio_total);

CREATE TABLE DIA_PLAN
(
    id           SERIAL PRIMARY KEY,
    plan_viaje_id INTEGER NOT NULL,
    numero_dia   INTEGER NOT NULL CHECK (numero_dia > 0),
    titulo       VARCHAR(200) NOT NULL,
    descripcion  TEXT,
    CONSTRAINT fk_dia_plan FOREIGN KEY (plan_viaje_id)
        REFERENCES PLAN_VIAJE (id) ON DELETE CASCADE,
    CONSTRAINT uk_plan_dia UNIQUE (plan_viaje_id, numero_dia)
);
CREATE INDEX idx_dia_plan ON DIA_PLAN (plan_viaje_id);

CREATE TABLE ACTIVIDAD
(
    id          SERIAL PRIMARY KEY,
    dia_plan_id INTEGER NOT NULL,
    hora        TIME,
    nombre      VARCHAR(200) NOT NULL,
    descripcion TEXT,
    lugar       VARCHAR(200),
    costo_extra DECIMAL(10, 2) DEFAULT 0 CHECK (costo_extra >= 0),
    obligatoria BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_actividad_dia FOREIGN KEY (dia_plan_id)
        REFERENCES DIA_PLAN (id) ON DELETE CASCADE
);
CREATE INDEX idx_actividad_dia ON ACTIVIDAD (dia_plan_id);

-- =====================================================
-- CU4 - GESTIÓN DE VENTAS
-- =====================================================
CREATE TABLE VENTA
(
    id               SERIAL PRIMARY KEY,
    cliente_id       INTEGER        NOT NULL,
    vendedor_id      INTEGER        NOT NULL,
    viaje_id         INTEGER        NOT NULL,
    fecha_venta      TIMESTAMP DEFAULT NOW(),
    tipo_venta       VARCHAR(20)    NOT NULL CHECK (tipo_venta IN ('CONTADO', 'CREDITO')),
    monto_total      DECIMAL(10, 2) NOT NULL CHECK (monto_total > 0),
    descuento        DECIMAL(10, 2) DEFAULT 0 CHECK (descuento >= 0),
    monto_final      DECIMAL(10, 2) NOT NULL CHECK (monto_final > 0),
    monto_pagado     DECIMAL(10, 2) DEFAULT 0 CHECK (monto_pagado >= 0),
    monto_pendiente  DECIMAL(10, 2) DEFAULT 0 CHECK (monto_pendiente >= 0),
    estado           VARCHAR(20) DEFAULT 'PENDIENTE' CHECK (estado IN ('PENDIENTE', 'CONFIRMADA', 'CANCELADA', 'COMPLETADA', 'PAGADA')),
    observaciones    TEXT,
    numero_factura   VARCHAR(50) UNIQUE,
    CONSTRAINT fk_venta_cliente FOREIGN KEY (cliente_id)
        REFERENCES USUARIO (id) ON DELETE RESTRICT,
    CONSTRAINT fk_venta_vendedor FOREIGN KEY (vendedor_id)
        REFERENCES USUARIO (id) ON DELETE RESTRICT
);
CREATE INDEX idx_venta_cliente ON VENTA (cliente_id);
CREATE INDEX idx_venta_vendedor ON VENTA (vendedor_id);
CREATE INDEX idx_venta_fecha ON VENTA (fecha_venta);
CREATE INDEX idx_venta_tipo ON VENTA (tipo_venta);
CREATE INDEX idx_venta_estado ON VENTA (estado);

-- =====================================================
-- CU5 - GESTIÓN DE PLAN DE PAGOS
-- =====================================================
CREATE TABLE PLAN_PAGO
(
    id                SERIAL PRIMARY KEY,
    venta_id          INTEGER        NOT NULL,
    numero_cuotas     INTEGER        NOT NULL CHECK (numero_cuotas > 0),
    monto_cuota       DECIMAL(10, 2) NOT NULL CHECK (monto_cuota > 0),
    interes_porcentaje DECIMAL(5, 2) DEFAULT 0 CHECK (interes_porcentaje >= 0),
    fecha_primer_pago DATE           NOT NULL,
    estado            VARCHAR(20) DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO', 'COMPLETADO', 'CANCELADO', 'VENCIDO')),
    CONSTRAINT fk_plan_pago_venta FOREIGN KEY (venta_id)
        REFERENCES VENTA (id) ON DELETE CASCADE,
    CONSTRAINT uk_plan_pago_venta UNIQUE (venta_id)
);
CREATE INDEX idx_plan_pago_venta ON PLAN_PAGO (venta_id);
CREATE INDEX idx_plan_pago_estado ON PLAN_PAGO (estado);

CREATE TABLE CUOTA
(
    id             SERIAL PRIMARY KEY,
    plan_pago_id   INTEGER        NOT NULL,
    numero_cuota   INTEGER        NOT NULL CHECK (numero_cuota > 0),
    monto          DECIMAL(10, 2) NOT NULL CHECK (monto > 0),
    fecha_vencimiento DATE        NOT NULL,
    fecha_pago     DATE,
    estado         VARCHAR(20) DEFAULT 'PENDIENTE' CHECK (estado IN ('PENDIENTE', 'PAGADA', 'VENCIDA', 'CANCELADA')),
    monto_pagado   DECIMAL(10, 2) DEFAULT 0 CHECK (monto_pagado >= 0),
    mora           DECIMAL(10, 2) DEFAULT 0 CHECK (mora >= 0),
    CONSTRAINT fk_cuota_plan_pago FOREIGN KEY (plan_pago_id)
        REFERENCES PLAN_PAGO (id) ON DELETE CASCADE,
    CONSTRAINT uk_plan_cuota UNIQUE (plan_pago_id, numero_cuota)
);
CREATE INDEX idx_cuota_plan_pago ON CUOTA (plan_pago_id);
CREATE INDEX idx_cuota_estado ON CUOTA (estado);
CREATE INDEX idx_cuota_vencimiento ON CUOTA (fecha_vencimiento);

-- =====================================================
-- CU6 - GESTIÓN DE VIAJES
-- =====================================================
CREATE TABLE VIAJE
(
    id                 SERIAL PRIMARY KEY,
    plan_viaje_id      INTEGER NOT NULL,
    codigo_viaje       VARCHAR(50) UNIQUE NOT NULL,
    fecha_salida       DATE    NOT NULL,
    fecha_retorno      DATE    NOT NULL,
    cupos_disponibles  INTEGER NOT NULL CHECK (cupos_disponibles >= 0),
    cupos_reservados   INTEGER DEFAULT 0 CHECK (cupos_reservados >= 0),
    estado             VARCHAR(20) DEFAULT 'PROGRAMADO' CHECK (estado IN ('PROGRAMADO', 'EN_CURSO', 'COMPLETADO', 'CANCELADO')),
    precio_actual      DECIMAL(10, 2) NOT NULL CHECK (precio_actual >= 0),
    guia_asignado      VARCHAR(200),
    observaciones      TEXT,
    CONSTRAINT fk_viaje_plan FOREIGN KEY (plan_viaje_id)
        REFERENCES PLAN_VIAJE (id) ON DELETE RESTRICT,
    CONSTRAINT chk_viaje_fechas CHECK (fecha_retorno > fecha_salida),
    CONSTRAINT chk_viaje_cupos CHECK (cupos_reservados <= cupos_disponibles)
);
CREATE INDEX idx_viaje_plan ON VIAJE (plan_viaje_id);
CREATE INDEX idx_viaje_fecha_salida ON VIAJE (fecha_salida);
CREATE INDEX idx_viaje_estado ON VIAJE (estado);
CREATE INDEX idx_viaje_codigo ON VIAJE (codigo_viaje);

CREATE TABLE RESERVA_VIAJE
(
    id              SERIAL PRIMARY KEY,
    viaje_id        INTEGER NOT NULL,
    cliente_id      INTEGER NOT NULL,
    numero_personas INTEGER NOT NULL CHECK (numero_personas > 0),
    fecha_reserva   TIMESTAMP DEFAULT NOW(),
    estado          VARCHAR(20) DEFAULT 'RESERVADA' CHECK (estado IN ('RESERVADA', 'CONFIRMADA', 'CANCELADA')),
    observaciones   TEXT,
    CONSTRAINT fk_reserva_viaje FOREIGN KEY (viaje_id)
        REFERENCES VIAJE (id) ON DELETE RESTRICT,
    CONSTRAINT fk_reserva_cliente FOREIGN KEY (cliente_id)
        REFERENCES USUARIO (id) ON DELETE RESTRICT
);
CREATE INDEX idx_reserva_viaje ON RESERVA_VIAJE (viaje_id);
CREATE INDEX idx_reserva_cliente ON RESERVA_VIAJE (cliente_id);
CREATE INDEX idx_reserva_estado ON RESERVA_VIAJE (estado);

-- =====================================================
-- CU7 - GESTIÓN DE PAGOS
-- =====================================================
CREATE TABLE PAGO
(
    id               SERIAL PRIMARY KEY,
    venta_id         INTEGER        NOT NULL,
    cuota_id         INTEGER,
    fecha_pago       TIMESTAMP DEFAULT NOW(),
    monto            DECIMAL(10, 2) NOT NULL CHECK (monto > 0),
    metodo_pago      VARCHAR(50)    NOT NULL CHECK (metodo_pago IN ('EFECTIVO', 'TARJETA', 'TRANSFERENCIA', 'QR')),
    numero_recibo    VARCHAR(50) UNIQUE NOT NULL,
    numero_referencia VARCHAR(100),
    observaciones    TEXT,
    CONSTRAINT fk_pago_venta FOREIGN KEY (venta_id)
        REFERENCES VENTA (id) ON DELETE RESTRICT,
    CONSTRAINT fk_pago_cuota FOREIGN KEY (cuota_id)
        REFERENCES CUOTA (id) ON DELETE SET NULL
);
CREATE INDEX idx_pago_venta ON PAGO (venta_id);
CREATE INDEX idx_pago_cuota ON PAGO (cuota_id);
CREATE INDEX idx_pago_fecha ON PAGO (fecha_pago);
CREATE INDEX idx_pago_metodo ON PAGO (metodo_pago);
CREATE INDEX idx_pago_recibo ON PAGO (numero_recibo);
