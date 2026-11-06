-- Datos iniciales del Laboratorio de Biomateriales Makila

-- 1. Reactivos Químicos
INSERT OR IGNORE INTO reactivos_quimicos (id, nombre, formula, cantidad, unidad, ubicacion, fecha_caducidad, estado_seguridad, temp_max_celsius, requiere_aislamiento_luz, msds_resumen)
VALUES 
(1, 'Bismuto de Cobre (Nanopolvo)', 'CuBi', 250.0, 'g', 'Refrigerador Criogénico R-02', '2027-03-15', 'Termolábil. Se degrada rápidamente por encima de los 30°C.', 30.0, 0, 'MSDS-CB-01: Mantener refrigerado entre 2°C y 8°C. Uso exclusivo en síntesis biomédica.'),
(2, 'Fluido de Micras Óptico (Sílice Coloidal)', 'SiO2-aq', 500.0, 'mL', 'Gabinete Oscuro UV G-04', '2026-12-01', 'Fotosensible crítico. Requiere aislamiento estricto de cualquier luz solar directa.', 25.0, 1, 'MSDS-FM-02: Envase ámbar sellado. La exposición a radiación UV produce gelificación irreversible.'),
(3, 'Ácido Hialurónico Grado Inyectable 1%', '(C14H21NO11)n', 120.0, 'mL', 'Refrigerador R-01', '2027-01-20', 'Estable refrigerado. Biocompatible clase III.', 8.0, 0, 'MSDS-AH-03: Almacenar a 4°C. No congelar. Destinado a andamios celulares.'),
(4, 'Quitosano de Alto Peso Molecular', '(C6H11NO4)n', 800.0, 'g', 'Almacén General Seco E-1', '2028-06-30', 'Higroscópico. Mantener desecado.', 35.0, 0, 'MSDS-QT-04: Polvo inflamable en suspensión fina. pH neutro.');

-- 2. Aleaciones y Biomateriales
INSERT OR IGNORE INTO aleaciones_biomateriales (id, tipo_material, nombre, lote, cantidad_disponible, unidad, especificaciones_tecnicas)
VALUES
(1, 'Aleación Titanio-Zirconio (Ti-Zr)', 'Sistemas Titanio-Zirconio Grado Implante', 'LOT-TIZR-2026-001', 45.0, 'barras', 'ASTM F67 Grado Quirúrgico. Alta biocompatibilidad para prótesis óseas y ensayos de fatiga mecánica.'),
(2, 'Polímero Biorreabsorbible', 'PCL Poli(caprolactona) Mw 80,000', 'LOT-PCL-2026-B4', 1500.0, 'g', 'Polímero semicristalino biocompatible para bioimpresión 3D y andamios tisulares biodegradables.'),
(3, 'Aleación con Memoria de Forma', 'Nitinol Biomédico (NiTi 55/45)', 'LOT-NITI-2025-X9', 18.0, 'láminas', 'Superelasticidad a temperatura corporal (37°C). Destinado a endoprótesis vasculares (stents).');

-- 3. Equipos de Caracterización e Instrumentación
INSERT OR IGNORE INTO equipos_caracterizacion (id, nombre_equipo, estado_actual, fecha_proxima_calibracion, ubicacion, responsable)
VALUES
(1, 'Máquina Universal de Ensayos Mecánicos (Instron 3366)', 'OPERATIVO', '2026-11-20', 'Área de Biomecánica Lab B-101', 'Dr. José Martín Quiroz'),
(2, 'Microscopio de Fuerza Atómica (AFM Park NX10)', 'OPERATIVO', '2026-12-15', 'Sala Blanca Nanoescala N-02', 'Dra. Bonilla Carranza'),
(3, 'Espectrofotómetro UV-Vis Lambda 365', 'EN_MANTENIMIENTO', '2026-11-10', 'Laboratorio Analítico A-204', 'Ing. Biomédico Residente'),
(4, 'Reómetro Rotacional Anton Paar MCR 302', 'OPERATIVO', '2027-02-01', 'Área de Reología y Polímeros', 'Dr. José Martín Quiroz');

-- 4. Historial de Movimientos (Trazabilidad)
INSERT OR IGNORE INTO historial_movimientos (id, tipo_movimiento, categoria, item_id, item_nombre, cantidad, unidad, experimento_o_lote, usuario_responsable, fecha_hora, notas)
VALUES
(1, 'ENTRADA', 'BIOMATERIAL', 1, 'Sistemas Titanio-Zirconio Grado Implante', 50.0, 'barras', 'LOT-TIZR-2026-001', 'Dr. José Martín Quiroz', '2026-10-15 09:30:00', 'Recepción de proveedor certificado ISO 13485'),
(2, 'CONSUMO', 'BIOMATERIAL', 1, 'Sistemas Titanio-Zirconio Grado Implante', 5.0, 'barras', 'EXP-BIO-2026-TRAC01', 'Ing. Biomédico Residente', '2026-10-22 14:15:00', 'Mecanizado de probetas para ensayos de tracción uniaxial'),
(3, 'ENTRADA', 'REACTIVO', 1, 'Bismuto de Cobre (Nanopolvo)', 250.0, 'g', 'MSDS-CB-01', 'Dra. Bonilla Carranza', '2026-11-01 11:00:00', 'Almacenado bajo protocolo térmico estricto a 4°C');

