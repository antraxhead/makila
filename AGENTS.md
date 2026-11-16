# AGENTS.md — Contexto y Directrices de Desarrollo para Agentes de Código

Este archivo define el contexto operativo, directrices de arquitectura y reglas que cualquier agente de código autónomo (Antigravity, Claude Code, Codex o equivalente) debe respetar rigurosamente al colaborar en el repositorio **Makila**.

---

## 1. Descripción del Proyecto

**Makila** es un asistente inteligente y sistema de control de inventario para laboratorios de investigación y desarrollo de biomateriales y materiales de uso médico (Universidad de Guadalajara — CUCEI).
- **Dominio Biomédico:** Gestión de reactivos químicos termolábiles (ej. bismuto de cobre <30°C), fotosensibles (ej. fluido de micras con aislamiento de radiación solar UV), aleaciones y biomateriales de grado implante (sistemas Ti-Zr ASTM F67, PCL, Nitinol) y equipos de caracterización e instrumentación con seguimiento estricto de calibración (Instron 3366, Microscopio AFM Park NX10).
- **Componente de IA:** Motor agéntico con **Function Calling avanzado** y acceso seguro a base de datos relacional (SQL Agent seguro con consultas 100% parametrizadas).
- **Interfaz Dual:** Aplicación web con tablero visual CRUD y chat conversacional interactivo.

---

## 2. Arquitectura de Software y Stack Tecnológico

El proyecto se rige por la **Arquitectura Hexagonal (Puertos y Adaptadores)**:

```
src/main/java/com/makila/
├── domain/                               # Núcleo de Dominio puro (independiente de Spring/Frameworks)
│   ├── model/                            # Entidades y objetos de valor
│   ├── port/in/                          # Casos de uso (Driving / Inbound Ports)
│   ├── port/out/                         # Puertos de persistencia e IA (Driven / Outbound Ports)
│   └── exception/                        # Excepciones de negocio y violaciones de guardrails
├── application/                          # Capa de Aplicación y Orquestación
│   ├── service/                          # Implementación de puertos de entrada
│   ├── guardrail/                        # Guardrails de Seguridad (Requisito P07)
│   └── dto/                              # DTOs de comunicación
└── infrastructure/                       # Capa de Infraestructura (Adaptadores)
    ├── adapter/in/web/                   # REST Controllers y endpoints HTTP
    ├── adapter/out/persistence/          # Repositorios SQLite JDBC parametrizados
    ├── adapter/out/ai/                   # Adaptador de Agente y Function Calling
    └── config/                           # Configuración de base de datos y Web MVC
```

### Stack Tecnológico:
- **Lenguaje:** Java 21 (Eclipse Adoptium OpenJDK 21 LTS).
- **Framework:** Spring Boot 3.3.4 (Spring Web, Spring Validation, Spring JDBC).
- **Base de Datos:** SQLite local (`makila_inventory.db`) mediante `org.xerial:sqlite-jdbc`.
- **Motor de Construcción:** Gradle 9.7 / 8.8 (con Gradle Wrapper multiplataforma).
- **Testing:** JUnit 5 (Jupiter), AssertJ, Spring Boot Test.
- **Frontend:** Vanilla HTML5, CSS3 ergonómico (paleta Valencia, Carnation, Gallery, Venice Blue, Fountain Blue sin `#FFFFFF` puro) y JavaScript ES6+.

---

## 3. Comandos de Ejecución y Pruebas

Cualquier agente debe validar el proyecto ejecutando estos comandos:

```powershell
# Ejecutar todas las pruebas (Unitarias, Integración y Eval Suite P09)
.\gradlew.bat test

# Ejecutar únicamente la suite de evaluación agéntica (P09)
.\gradlew.bat test --tests com.makila.eval.AgentEvalSuiteTest

# Iniciar la aplicación en modo desarrollo (puerto 8080)
.\gradlew.bat bootRun
```

---

## 4. Reglas Críticas que el Agente Debe Respetar

1. **Seguridad y Secretos (P07):**
   - **NUNCA** incrustar credenciales, API keys ni contraseñas en código duro. Todo secreto debe consumirse mediante variables de entorno (`GEMINI_API_KEY`, etc.).
2. **Mitigación Estricta de SQL Injection (P07):**
   - **PROHIBIDO** concatenar strings directamente en consultas SQL (`"SELECT ... WHERE id = " + id`).
   - Todas las consultas a la base de datos deben usar sentencias parametrizadas (`PreparedStatement` / `JdbcTemplate` con `?`).
3. **Guardrails de Operación y Stock (P07):**
   - Validar en código que ninguna salida o consumo exceda el stock actual disponible antes de modificar el repositorio. Si se supera, lanzar `StockInsuficienteException`.
   - Bloquear operaciones sobre entidades inexistentes con `RegistroNoEncontradoException`.
   - Rechazar cantidades menores o iguales a cero.
4. **Mitigación de Inyección de Prompts y Jailbreaks (P07):**
   - El agente debe sanitizar y evaluar los prompts entrantes mediante `SecurityGuardrails` antes de invocar cualquier herramienta o LLM.
5. **Manejo de Ambigüedad:**
   - Si una solicitud contiene un nombre incompleto o ambiguo con múltiples coincidencias, el agente **NO** debe adivinar ni modificar datos; debe invocar la herramienta de desambiguación y solicitar confirmación al usuario.
6. **Calidad y Verificación (P09):**
   - Toda nueva funcionalidad o caso de uso debe contar con su correspondiente prueba automatizada en `src/test/java/com/makila/`.
   - Mantener el 100% de la suite de pruebas en estado verde (`BUILD SUCCESSFUL`).
7. **Prohibición Total de Emojis y Directrices UI/UX (Requisito Obligatorio):**
   - **Queda estrictamente prohibido el uso de emojis** en cualquier parte del proyecto: interfaz web, mensajes del asistente agéntico, logs, respuestas JSON, backend, plantillas HTML, estilos CSS y scripts.
   - En su lugar, utilizar tipografía técnica sobria, indicadores textuales estandarizados entre corchetes (ej. `[INFO]`, `[ALERTA]`, `[CRITICO]`, `[OPTIMO]`, `[TOOL]`, `[GUARDRAIL]`) o iconografía vectorial SVG minimalista y nítida.
   - **Esquinas Afiladas Obligatorias:** Todo elemento visual (tarjetas, botones, inputs, tablas, modales, burbujas de diálogo) debe tener esquinas completamente rectas y afiladas (`border-radius: 0 !important`). No se permiten bordes redondeados.
   - **Apariencia Glasslike (Glassmorphism Profesional):** El fondo y los contenedores de la UI deben presentar acabado de vidrio esmerilado translúcido con `backdrop-filter: blur(...)`, bordes de cristal sutiles de 1px y reflejos ergonómicos basados en la paleta oficial (Valencia `#343434`, Carnation `#F3505A`, Gallery `#ECEBEA`, Venice Blue `#085979`, Fountain Blue `#64BABE`), sin utilizar blanco puro (`#FFFFFF`) de fondo para evitar fatiga visual en jornadas de laboratorio.
