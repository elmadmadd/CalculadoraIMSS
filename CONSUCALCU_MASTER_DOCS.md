# ConsuCalcu Pro - Documentación Técnica Maestra

**Fecha de Actualización:** 06 Enero 2026
**Versión:** 2.0 (Juicy Update)
**Tecnología:** Android Nativo (Kotlin) + Jetpack Compose

---

## 1. Visión General
ConsuCalcu Pro es una herramienta financiera avanzada diseñada para asesores de crédito. Permite simular préstamos (refinanciamientos y créditos nuevos), calcular seguros de vida, gestionar una cartera básica de clientes y generar propuestas comerciales visuales ("Tickets Dorados"). La aplicación se distingue por su enfoque en la "Gamificación" y "Juicy UI", ofreciendo una experiencia de usuario rica con niveles, recompensas y retroalimentación háptica/visual.

---

## 2. Arquitectura del Sistema
La aplicación sigue una arquitectura **Single Activity** utilizando **Jetpack Compose** para toda la interfaz de usuario.
*   **Patrón:** MVVM (Model-View-ViewModel).
*   **Estado:** Centralizado en `CalculatorViewModel` y expuesto vía `StateFlow` (`CalculatorUiState`).
*   **Persistencia:** `SharedPreferences` para datos ligeros (perfil, estado de gamificación) y serialización JSON (Gson) para estructuras complejas.

### Componentes Clave
*   **MainActivity.kt:** Contiene **TODA** la lógica de la aplicación (Views, ViewModel, Modelos, Utils). Es un monolito funcional.
*   **FinancialEngine (Object):** Núcleo de cálculo puro. Contiene factores, tasas y algoritmos de validación.
*   **RewardsManager:** Gestiona la lógica de desbloqueo de recompensas y persistencia del sistema de gamificación.
*   **TicketGenerator:** Generador de imágenes (Bitmaps) usando Canvas nativo para compartir propuestas.

---

## 3. Motor Financiero (FinancialEngine)
El corazón de la aplicación. Maneja dos tipos principales de cálculo: **Tradicional** y **Retención (SIPRE)**.

### A. Lógica de Cálculo
1.  **Capacidad de Crédito (CNCA):** Se calcula sumando la capacidad libre del cliente (`conceptos 01 + 02 - descuentos`) + los pagos de las deudas seleccionadas para refinanciar.
2.  **Fórmulas:**
    *   `Monto Máximo = Capacidad * Factor`
    *   `Pago Mensual = Monto / Factor` (Simplificado, depende del producto).
    *   Uso estricto de `BigDecimal` para precisión monetaria.
3.  **Refinanciamiento:**
    *   Calcula si el nuevo préstamo cubre los saldos de las deudas seleccionadas.
    *   Genera un "Remanente" (Dinero libre para el cliente).

### B. Reglas de Negocio (Retención/SIPRE)
Esta modalidad ofrece tasas preferenciales y factores más agresivos, pero tiene reglas estrictas:
*   **Plazo Exclusivo:** Solo aplica a **60 meses**.
*   **Monto Mínimo:** Requiere un monto mínimo (ej. $5,000) definido por producto.
*   **Lógica de Selección:** El motor calcula ambas opciones (Tradicional y Retención) y si el cliente cumple las reglas de Retención y obtiene un mayor beneficio, se prioriza o se muestra como opción destacada "Tasa Preferencial".

---

## 4. Módulo de Seguros
Calculadora especializada para pólizas de seguro de vida asociadas a créditos.

### Variables de Entrada
*   Edad del cliente.
*   Monto Asegurado.
*   Plazo del crédito.
*   Tipo de Pensión (Invalidez, Vejez, etc.).
*   Proveedor (CONSUBANCO, etc.).
*   Estatus (Cliente Nuevo vs Renovación).

### Reglas de Validación (Hardcoded)
1.  **Edad Máxima Global:** Rechazo automático si `Edad + (Plazo/12) > 85 años`.
2.  **Pensionados por Invalidez:**
    *   Rango crítico: 51-70 años.
    *   Si es Cliente Nuevo en este rango -> Solo aplica cobertura por **1 año**.
3.  **Pensionados por Vejez:**
    *   76+ años: Reglas estrictas, tope 80 años edad final.
    *   66-75 años: Si la edad final > 75 años -> Aplica cobertura por **Plazo Total**.

---

## 5. Sistema de Gamificación (Juicy UI)
Diseñado para aumentar la retención y satisfacción del asesor.

### A. Economía del Juego
*   **XP (Experiencia):** Se gana al realizar cotizaciones exitosas, generar tickets o completar tutoriales.
*   **Niveles:** 1 a 20+. Cada nivel desbloquea títulos ("Novato", "Lobo de Wall St") y cosméticos.
*   **Rachas (Streaks):** Seguimiento de días consecutivos usando la app.

### B. Sistema de Recompensas
Gestionado por `RewardsManager`.
*   **Categorías:**
    *   *Temas de Color:* Paletas completas (Dark Mode, Neon, Pastel).
    *   *Partículas:* Efectos de confeti/explosión al tener éxito (Billetes, Estrellas, Fuego).
    *   *Avatares:* Iconos de perfil desbloqueables.
    *   *Estilos de Tarjeta:* Bordes y sombras para las tarjetas de resultados.
*   **Persistencia:** Estado de desbloqueo guardado en JSON en SharedPreferences.

---

## 6. Generación de Entregables

### A. Ticket Dorado (Imagen PNG)
Genera una imagen profesional para compartir por WhatsApp.
*   **Tecnología:** `android.graphics.Canvas` dibujando sobre un `Bitmap`.
*   **Compatibilidad:**
    *   Android 10+ (API 29): Usa `MediaStore` y `RELATIVE_PATH`.
    *   Android 9 e inferiores: Usa `Environment.getExternalStoragePublicDirectory` y `DATA`.
*   **Diseño:** Header "Slate Blue", tarjeta flotante blanca, monto gigante en azul, badge de "Aprobado" y footer con datos del asesor.

### B. Layout de Captura (Texto)
Genera una cadena de texto formateada lista para pegar en sistemas internos (CRM).
*   Incluye todos los datos del cliente, referencias, y desglose del crédito seleccionado.
*   Copia automática al portapapeles.

---

## 7. Estructura de Datos (Modelos)

*   **`CalculatorUiState`:** Estado global de la UI. Contiene TODO (perfil, input form, resultados, banderas de UI).
*   **`LoanOffer`:** Representa una oferta calculada (Monto, Pago, Plazo, Tasa, CAT).
*   **`ClientData`:** Datos personales del cliente (Nombre, RFC, NSS, Referencias).
*   **`UserProfile`:** Datos del asesor (Nombre, Nivel, XP).
*   **`Debt`:** Modelo de deuda individual para refinanciamiento (ID, Saldo, Pago Mensual).

---
*Documento generado automáticamente por Antigravity AI.*
