# MAKILA - Asistente Agéntico de Inventario Biomédico

**Proyecto Final: Desarrollo Agéntico de Software**  
*Tópicos de Ingeniería Biomédica II*  
**Universidad de Guadalajara — Centro Universitario de Ciencias Exactas e Ingenierías (CUCEI)**  
**Autor:** José Martín Quiroz Castro  
**Docente:** Dr. José Luis Bonilla Carranza  

---

## 📌 1. Visión y Problemática Resuelta

El control de inventario en laboratorios de biomateriales requiere atención rigurosa tanto al volumen disponible como a las **condiciones de almacenamiento y conservación crítica**:
- Reactivos termolábiles (ej. *Bismuto de Cobre* que se degrada por encima de los 30°C).
- Materiales fotosensibles (ej. *Fluido de Micras Óptico* que requiere aislamiento total de luz solar UV).
- Aleaciones biocompatibles y polímeros de grado implante (*Titanio-Zirconio ASTM F67*, *PCL*, *Nitinol*).
- Equipos e instrumentación que exigen control estricto de calibración periódica (*Máquina Universal de Ensayos Instron*, *Microscopio de Fuerza Atómica AFM*).

**Makila** es una aplicación con interfaz dual (**Tablero CRUD Visual + Asistente Conversacional con Function Calling seguro**) diseñada con **Arquitectura Hexagonal en Java** para garantizar portabilidad multiplataforma nativa, seguridad y robustez.

---

## 🏗️ 2. Arquitectura Hexagonal (Ports & Adapters)

El código fuente está estructurado de acuerdo con la bitácora técnica de arquitectura hexagonal:

```
src/
├── main/
│   ├── java/com/makila/
│   │   ├── domain/                               # NÚCLEO DE DOMINIO (Independiente de Frameworks)
│   │   │   ├── model/                            # Entidades de negocio
│   │   │   │   ├── ReactivoQuimico.java          # Fórmulas, stock, MSDS, temp_max, luz
│   │   │   │   ├── AleacionBiomaterial.java      # Lotes, tipo, especificaciones ASTM
│   │   │   │   ├── EquipoCaracterizacion.java    # Estados (OPERATIVO, MANTENIMIENTO), calibración
│   │   │   │   ├── MovimientoInventario.java     # Trazabilidad (ENTRADA, SALIDA, CONSUMO)
│   │   │   │   ├── AgentToolResult.java          # Resultado de Function Calling (SUCCESS, BLOCKED, AMBIGUOUS)
│   │   │   │   └── TipoMovimiento.java / CategoriaMaterial.java / EstadoEquipo.java
│   │   │   ├── port/
│   │   │   │   ├── in/                           # Puertos de Entrada (Casos de uso)
│   │   │   │   │   ├── CatalogoMaterialesPort.java
│   │   │   │   │   ├── GestionMovimientosPort.java
│   │   │   │   │   ├── AgenteConversacionalPort.java
│   │   │   │   │   └── AuditoriaSeguridadPort.java
│   │   │   │   └── out/                          # Puertos de Salida (Persistencia & LLM)
│   │   │   │       ├── ReactivoRepositoryPort.java
│   │   │   │       ├── BiomaterialRepositoryPort.java
│   │   │   │       ├── EquipoRepositoryPort.java
│   │   │   │       ├── MovimientoRepositoryPort.java
│   │   │   │       └── LlmServicePort.java
│   │   │   └── exception/                        # Excepciones de dominio y guardrails
│   │   │       ├── StockInsuficienteException.java
│   │   │       ├── RegistroNoEncontradoException.java
│   │   │       ├── MaterialAmbiguoException.java
│   │   │       ├── InyeccionDetectadaException.java
│   │   │       └── OperacionInvalidaException.java
│   │   │
│   │   ├── application/                          # CAPA DE APLICACIÓN
│   │   │   ├── service/                          # Implementaciones de servicios
│   │   │   │   ├── CatalogoMaterialesService.java
│   │   │   │   ├── GestionMovimientosService.java
│   │   │   │   ├── AuditoriaSeguridadService.java
│   │   │   │   └── AgenteInventarioService.java
│   │   │   ├── guardrail/                        # Capa de Seguridad P07
│   │   │   │   └── SecurityGuardrails.java
│   │   │   └── dto/                              # Objetos de Transferencia de Datos
│   │   │       ├── ChatMessageDTO.java
│   │   │       ├── ChatResponseDTO.java
│   │   │       ├── MovimientoRequestDTO.java
│   │   │       ├── DashboardStatsDTO.java
│   │   │       └── AlertaSeguridadDTO.java
│   │   │
│   │   └── infrastructure/                       # CAPA DE INFRAESTRUCTURA
│   │       ├── adapter/
│   │       │   ├── in/web/                       # Controladores REST & UI
│   │       │   │   ├── AgentChatController.java  # POST /api/chat
│   │       │   │   ├── ReactivoController.java   # /api/reactivos
│   │       │   │   ├── BiomaterialController.java# /api/biomateriales
│   │       │   │   ├── EquipoController.java     # /api/equipos
│   │       │   │   ├── MovimientoController.java # /api/movimientos (trazabilidad)
│   │       │   │   ├── DashboardController.java  # /api/dashboard (stats y alertas)
│   │       │   │   └── GlobalExceptionHandler.java
│   │       │   └── out/
│   │       │       ├── persistence/              # Repositorios JDBC SQLite (Consultas Parametrizadas)
│   │       │       │   ├── JdbcReactivoRepository.java
│   │       │       │   ├── JdbcBiomaterialRepository.java
│   │       │       │   ├── JdbcEquipoRepository.java
│   │       │       │   └── JdbcMovimientoRepository.java
│   │       │       └── ai/                       # Motor Agéntico de Function Calling
│   │       │           └── DeterministicFunctionCallingAgent.java
│   │       └── config/                           # Configuración Spring & SQLite
│   │           ├── DatabaseConfig.java
│   │           └── WebConfig.java
│   │
│   └── resources/
│       ├── schema.sql                            # DDL SQLite (4 tablas e índices)
│       ├── data.sql                              # Datos semilla biomédicos
│       ├── application.properties
│       └── static/                               # Frontend Web (Chat + CRUD Visual)
│           ├── index.html
│           ├── css/styles.css                    # Paleta de diseño oficial
│           ├── js/app.js                         # Cliente interactivo
│           └── images/logo.png                   # Identidad visual Makila
│
└── test/
    └── java/com/makila/
        ├── eval/
        │   └── AgentEvalSuiteTest.java           # Eval Suite: 10 Casos de Prueba (Requisito P09)
        ├── domain/
        │   └── GestionMovimientosTest.java       # Pruebas unitarias de lógica y cálculo de stock
        └── infrastructure/
            └── JdbcPersistenceTest.java          # Pruebas de integración con SQLite
```

---

## 🎨 3. Identidad Visual y Paleta UI-UX

La interfaz respeta al 100% las especificaciones de diseño de la bitácora:
- **Valencia (`#343434`)**: Textos legibles, contrastes y tipografía.
- **Carnation (`#F3505A`)**: Alertas térmicas, caducidades y rechazos de seguridad.
- **Gallery (`#ECEBEA`)**: Fondo general y tarjetas ergonómicas (**sin blanco puro `#FFFFFF`** para descanso visual prolongado).
- **Venice Blue (`#085979`)**: Color primario institucional, barras de navegación y pestañas activas.
- **Fountain Blue (`#64BABE`)**: Acentos, badges de equipos operativos y selector de tools.
- **Logotipo Oficial**: Integrado en la cabecera y favicon (`/images/logo.png`).

---

## 🛡️ 4. Capa de Seguridad y Guardrails (Requisito P07)

1. **Validación de Entradas y Stock**:
   - Impide registrar consumos o salidas superiores al stock actual disponible (`StockInsuficienteException`).
   - Bloquea operaciones sobre identificadores inexistentes (`RegistroNoEncontradoException`).
   - Valida cantidades positivas mayores a cero.
2. **Mitigación de Inyección de Prompts y SQL Injection**:
   - Consultas a base de datos **100% parametrizadas** mediante JDBC `PreparedStatement`.
   - Filtro de patrones de escape SQL (`' OR 1=1; DROP TABLE...`) con rechazo inmediato.
   - Detección de jailbreaks y prompt injections (`ignore previous instructions`, `DAN`, etc.).
3. **Manejo de Ambigüedad**:
   - Si el usuario solicita consultar o mover un material con término genérico o ambiguo (ej. `"aleación"` que coincide tanto con *Titanio-Zirconio* como con *Nitinol*), el sistema detiene la ejecución destructiva y retorna una respuesta con estado `AMBIGUOUS` y botones de desambiguación para que el usuario elija la opción exacta.

---

## 🧪 5. Verificación y Calidad: Eval Suite (Requisito P09)

Ubicada en `src/test/java/com/makila/eval/AgentEvalSuiteTest.java`, incluye los **10 casos de prueba automatizados**:

### Consultas y Operaciones Exitosas (5 casos):
1. **Eval 1**: Consulta de existencias exactas de aleación de titanio-zirconio (`consultar_stock`).
2. **Eval 2**: Consulta de condiciones de almacenamiento fotosensible del fluido de micras (`verificar_msds_seguridad`).
3. **Eval 3**: Verificación de ficha MSDS y límite térmico (<30°C) para bismuto de cobre (`verificar_msds_seguridad`).
4. **Eval 4**: Consulta de instrumentación y fechas de próxima calibración (`consultar_equipos_calibracion`).
5. **Eval 5**: Registro controlado de entrada de reactivo químico con parámetros válidos (`registrar_movimiento`).

### Intentos de Operación Inválida Rechazados por Guardrails (5 casos):
6. **Eval 6**: Rechazo de salida con stock superior al disponible (`stock_guardrail_p07`).
7. **Eval 7**: Rechazo de operación sobre material no registrado en catálogo (`existence_guardrail_p07`).
8. **Eval 8**: Detección y bloqueo de intento malicioso de SQL Injection (`security_guardrail_p07`).
9. **Eval 9**: Detección y bloqueo de Prompt Injection / Jailbreak (`security_guardrail_p07`).
10. **Eval 10**: Detección de ambigüedad ante coincidencias múltiples con solicitud de aclaración (`disambiguation_engine`).

---

## 🚀 6. Compilación y Ejecución

### Ejecutar Pruebas Automatizadas (Eval Suite + Tests Backend):
```bash
./gradlew test
```

### Iniciar la Aplicación:
```bash
./gradlew bootRun
```

Una vez iniciada, abre tu navegador en:
```
http://localhost:8080/
```
Allí podrás interactuar con el **Tablero CRUD Visual** y con el **Asistente Agéntico Conversacional**.
