package com.example.consucalcu.academy

import androidx.compose.ui.graphics.Color

// Importar colores desde MainActivity
import com.example.consucalcu.JuicyBlue
import com.example.consucalcu.JuicyGreen
import com.example.consucalcu.JuicyPurple
import com.example.consucalcu.JuicyOrange
import com.example.consucalcu.JuicyRed
import com.example.consucalcu.JuicyYellow

/**
 * ACADEMY CONTENT - Contenido educativo basado en las presentaciones
 * Módulos, lecciones y preguntas sobre políticas IMSS, actualización de correo, etc.
 */

object AcademyContent {

    // ========== MÓDULO 1: ACTUALIZACIÓN DE CORREO ==========

    private val emailUpdateQuestions = listOf(
        Question(
            id = "email_q1",
            type = QuestionType.MULTIPLE_CHOICE,
            question = "¿Cuáles son los requisitos para la actualización de correo electrónico?",
            options = listOf(
                "Solo el INE del cliente",
                "Biométrico exitoso, INE de cliente, Formato de actualización y Video de cliente",
                "Únicamente el formato de actualización",
                "Solo el video del cliente"
            ),
            correctAnswer = "Biométrico exitoso, INE de cliente, Formato de actualización y Video de cliente",
            explanation = "Se requieren los 4 elementos para garantizar la seguridad del proceso de actualización.",
            difficulty = LessonDifficulty.BEGINNER,
            xpReward = 10
        ),
        Question(
            id = "email_q2",
            type = QuestionType.TRUE_FALSE,
            question = "¿El formato de actualización debe incluir el nombre completo del cliente?",
            options = listOf("Verdadero", "Falso"),
            correctAnswer = "Verdadero",
            explanation = "El formato debe capturar: 'Hoy (DD/MM/AAAA) POR MEDIO DEL PRESENTE YO (NOMBRE COMPLETO DE CLIENTE) OTORGO MI CONSENTIMIENTO EXPLÍCITO...'",
            difficulty = LessonDifficulty.BEGINNER,
            xpReward = 10
        ),
        Question(
            id = "email_q3",
            type = QuestionType.FILL_BLANK,
            question = "El texto del formato debe decir: 'SOLICITO LA ACTUALIZACIÓN DE MI CORREO ___________'",
            options = listOf("ELECTRÓNICO (CORREO)", "PERSONAL", "EMPRESARIAL", "TEMPORAL"),
            correctAnswer = "ELECTRÓNICO (CORREO)",
            explanation = "El formato oficial especifica 'CORREO ELECTRÓNICO (CORREO)'",
            difficulty = LessonDifficulty.INTERMEDIATE,
            xpReward = 25
        )
    )

    private val moduleEmailUpdate = Module(
        id = "module_email",
        title = "Actualización de Correo",
        description = "Aprende el proceso completo para actualizar el correo electrónico de clientes",
        icon = "📧",
        color = JuicyBlue,
        order = 1,
        isUnlocked = true,
        lessons = listOf(
            Lesson(
                id = "lesson_email_intro",
                title = "Introducción a la Actualización",
                description = "Conoce los requisitos básicos",
                type = LessonType.QUIZ,
                difficulty = LessonDifficulty.BEGINNER,
                moduleId = "module_email",
                order = 1,
                questions = emailUpdateQuestions.take(2),
                xpReward = 20,
                requiredLevel = 1,
                isLocked = false
            ),
            Lesson(
                id = "lesson_email_format",
                title = "Formato de Actualización",
                description = "Domina el texto oficial del formato",
                type = LessonType.BATTLE,
                difficulty = LessonDifficulty.INTERMEDIATE,
                moduleId = "module_email",
                order = 2,
                questions = emailUpdateQuestions,
                xpReward = 50,
                requiredLevel = 2,
                isLocked = true
            ),
            Lesson(
                id = "resource_email_template",
                title = "Plantilla de Correo",
                description = "Texto oficial para copiar y usar",
                type = LessonType.RESOURCE,
                difficulty = LessonDifficulty.BEGINNER,
                moduleId = "module_email",
                order = 3,
                resourceContent = """
                    📧 PLANTILLA OFICIAL - ACTUALIZACIÓN DE CORREO

                    Hoy (DD/MM/AAAA) POR MEDIO DEL PRESENTE YO (NOMBRE COMPLETO DE CLIENTE) OTORGO MI CONSENTIMIENTO EXPLÍCITO A (RAZÓN SOCIAL CORRESPONDIENTE) PARA ACTUALIZAR MIS DATOS PERSONALES. SOLICITO LA ACTUALIZACIÓN DE MI CORREO ELECTRÓNICO (CORREO)

                    ✅ REQUISITOS OBLIGATORIOS:
                    • Biométrico exitoso
                    • INE de cliente
                    • Formato de actualización
                    • Video de cliente

                    ⚠️ IMPORTANTE: Capturar el nombre completo del cliente en el formato
                """.trimIndent(),
                xpReward = 10,
                requiredLevel = 1,
                isLocked = false
            )
        )
    )

    // ========== MÓDULO 2: SUJETOS A CRÉDITO IMSS ==========

    private val creditSubjectsQuestions = listOf(
        Question(
            id = "credit_q1",
            type = QuestionType.MULTIPLE_CHOICE,
            question = "¿Qué tipo de pensionado tiene retiro a los 60-64 años de edad?",
            options = listOf("Invalidez", "Vejez", "Cesantía", "Viudez"),
            correctAnswer = "Cesantía",
            explanation = "Cesantía es el retiro entre 60-64 años de edad",
            difficulty = LessonDifficulty.BEGINNER,
            xpReward = 10
        ),
        Question(
            id = "credit_q2",
            type = QuestionType.MATCH_PAIRS,
            question = "¿Qué significa 'Viudez - Orfandad'?",
            options = listOf(
                "Viude/a o la concubina del trabajador/a",
                "Retiro 65 años",
                "Imposibilidad de realizar labores",
                "Incapacidad permanente total"
            ),
            correctAnswer = "Viude/a o la concubina del trabajador/a",
            explanation = "Viudez-Orfandad aplica a viude/a o concubina del trabajador/a",
            difficulty = LessonDifficulty.INTERMEDIATE,
            xpReward = 25
        ),
        Question(
            id = "credit_q3",
            type = QuestionType.MULTIPLE_CHOICE,
            question = "¿Qué tipo de pensión aplica para 'Imposibilidad de realizar labores remuneradas'?",
            options = listOf("Cesantía", "Vejez", "Invalidez", "Viudez"),
            correctAnswer = "Invalidez",
            explanation = "Invalidez = Imposibilidad de realizar labores remuneradas",
            difficulty = LessonDifficulty.INTERMEDIATE,
            xpReward = 25
        ),
        Question(
            id = "credit_q4",
            type = QuestionType.TRUE_FALSE,
            question = "¿La 'Incapacidad permanente total' es lo mismo que 'Invalidez'?",
            options = listOf("Verdadero", "Falso"),
            correctAnswer = "Falso",
            explanation = "Son diferentes: Invalidez = imposibilidad de labores remuneradas. Incapacidad permanente total = imposibilidad de reincorporarse a su actividad económica",
            difficulty = LessonDifficulty.ADVANCED,
            xpReward = 50
        ),
        Question(
            id = "credit_q5",
            type = QuestionType.MULTIPLE_CHOICE,
            question = "¿Qué tipo de pensión tiene 'Incapacidad Permanente parcial'?",
            options = listOf(
                "Invalidez",
                "Incapacidad permanente parcial - Posibilidad de reincorporarse a su actividad económica",
                "Vejez",
                "Cesantía"
            ),
            correctAnswer = "Incapacidad permanente parcial - Posibilidad de reincorporarse a su actividad económica",
            explanation = "La incapacidad permanente parcial permite reincorporación a la actividad económica",
            difficulty = LessonDifficulty.ADVANCED,
            xpReward = 50
        )
    )

    private val moduleCreditSubjects = Module(
        id = "module_credit_subjects",
        title = "Sujetos a Crédito IMSS",
        description = "Conoce los tipos de pensionados y sus características",
        icon = "👥",
        color = JuicyGreen,
        order = 2,
        isUnlocked = false,
        lessons = listOf(
            Lesson(
                id = "lesson_pension_types",
                title = "Tipos de Pensión",
                description = "Cesantía, Invalidez, Vejez, Viudez",
                type = LessonType.QUIZ,
                difficulty = LessonDifficulty.BEGINNER,
                moduleId = "module_credit_subjects",
                order = 1,
                questions = creditSubjectsQuestions.take(3),
                xpReward = 30,
                requiredLevel = 3,
                isLocked = true
            ),
            Lesson(
                id = "lesson_pension_battle",
                title = "Batalla: Domina las Pensiones",
                description = "Derrota al jefe con tus conocimientos",
                type = LessonType.BATTLE,
                difficulty = LessonDifficulty.ADVANCED,
                moduleId = "module_credit_subjects",
                order = 2,
                questions = creditSubjectsQuestions,
                xpReward = 100,
                requiredLevel = 4,
                isLocked = true
            )
        )
    )

    // ========== MÓDULO 3: POLÍTICAS DE CRÉDITO ==========

    private val creditPolicyQuestions = listOf(
        Question(
            id = "policy_q1",
            type = QuestionType.MULTIPLE_CHOICE,
            question = "¿Cuál es la edad máxima para solicitar un crédito?",
            options = listOf("70 años", "75 años", "80 años", "85 años"),
            correctAnswer = "80 años",
            explanation = "Edad máxima: hasta 80 años, incluyendo plazo del crédito (hasta 85 años si firma el formato de domiciliación por plazo de 1 año)",
            difficulty = LessonDifficulty.BEGINNER,
            xpReward = 10
        ),
        Question(
            id = "policy_q2",
            type = QuestionType.MULTIPLE_CHOICE,
            question = "¿Cuáles son los plazos disponibles para créditos?",
            options = listOf(
                "6, 12, 18 meses",
                "12, 24, 36, 48, 60 meses",
                "Solo 12 y 24 meses",
                "Cualquier plazo"
            ),
            correctAnswer = "12, 24, 36, 48, 60 meses",
            explanation = "Los plazos oficiales son: 12, 24, 36, 48 y 60 meses",
            difficulty = LessonDifficulty.BEGINNER,
            xpReward = 10
        ),
        Question(
            id = "policy_q3",
            type = QuestionType.FILL_BLANK,
            question = "El monto mínimo de crédito es de $",
            options = listOf("$5,000", "$8,000", "$10,000", "$15,000"),
            correctAnswer = "$8,000",
            explanation = "El monto mínimo establecido es de $8,000.00",
            difficulty = LessonDifficulty.BEGINNER,
            xpReward = 10
        )
    )

    private val moduleCreditPolicy = Module(
        id = "module_credit_policy",
        title = "Políticas de Crédito",
        description = "Requisitos, plazos y montos",
        icon = "💳",
        color = JuicyPurple,
        order = 3,
        isUnlocked = false,
        lessons = listOf(
            Lesson(
                id = "lesson_credit_basics",
                title = "Requisitos Básicos",
                description = "Edad, plazos y montos mínimos",
                type = LessonType.QUIZ,
                difficulty = LessonDifficulty.BEGINNER,
                moduleId = "module_credit_policy",
                order = 1,
                questions = creditPolicyQuestions,
                xpReward = 30,
                requiredLevel = 2,
                isLocked = true
            ),
            Lesson(
                id = "resource_credit_summary",
                title = "Resumen de Políticas",
                description = "Guía rápida de consulta",
                type = LessonType.RESOURCE,
                difficulty = LessonDifficulty.BEGINNER,
                moduleId = "module_credit_policy",
                order = 2,
                resourceContent = """
                    💳 POLÍTICAS DE CRÉDITO Y REQUISITOS

                    📊 REQUISITOS PRINCIPALES:

                    01️⃣ EDAD MÁXIMA
                    • Hasta 80 años incluyendo plazo del crédito
                    • Hasta 85 años si firma formato de domiciliación por 1 año

                    02️⃣ PLAZOS DISPONIBLES
                    • 12 meses
                    • 24 meses
                    • 36 meses
                    • 48 meses
                    • 60 meses

                    03️⃣ MONTO MÍNIMO
                    • $8,000.00 MXN

                    ⚠️ IMPORTANTE: Verificar siempre la edad del cliente sumando el plazo del crédito
                """.trimIndent(),
                xpReward = 10,
                requiredLevel = 2,
                isLocked = true
            )
        )
    )

    // ========== MÓDULO 4: ADDENDUM IMSS PENSIONADOS ==========

    private val addendumQuestions = listOf(
        Question(
            id = "addendum_q1",
            type = QuestionType.MULTIPLE_CHOICE,
            question = "Para clientes HOMBRES con tipo de pensión INVALIDEZ con edad entre 51 y 70 años, ¿cuál es la edad máxima?",
            options = listOf("70 años", "75 años", "80 años", "No aplica"),
            correctAnswer = "75 años",
            explanation = "Según el Addendum: Invalidez con edad entre 51-70 años tiene edad máxima de 75 años incluyendo plazo de crédito",
            difficulty = LessonDifficulty.ADVANCED,
            xpReward = 50
        ),
        Question(
            id = "addendum_q2",
            type = QuestionType.MULTIPLE_CHOICE,
            question = "Para clientes HOMBRES con tipo de pensión VEJEZ con edad entre 51 y 70 años, ¿cuál es la edad máxima?",
            options = listOf("70 años", "75 años", "80 años", "No aplica"),
            correctAnswer = "75 años",
            explanation = "Según el Addendum: Vejez con edad entre 51-70 años tiene edad máxima de 75 años incluyendo plazo de crédito",
            difficulty = LessonDifficulty.ADVANCED,
            xpReward = 50
        ),
        Question(
            id = "addendum_q3",
            type = QuestionType.TRUE_FALSE,
            question = "¿El Addendum aplica para TODO tipo de pensión que exceda los 80 años?",
            options = listOf("Verdadero", "Falso"),
            correctAnswer = "Verdadero",
            explanation = "El Addendum especifica: 'Todo tipo de pensión - Que con el plazo del crédito exceda los 80 años'",
            difficulty = LessonDifficulty.EXPERT,
            xpReward = 100
        )
    )

    private val moduleAddendum = Module(
        id = "module_addendum",
        title = "Addendum IMSS Pensionados",
        description = "Reglas especiales para pensionados",
        icon = "📋",
        color = JuicyOrange,
        order = 4,
        isUnlocked = false,
        lessons = listOf(
            Lesson(
                id = "lesson_addendum_rules",
                title = "Reglas del Addendum",
                description = "Casos especiales de edad y pensión",
                type = LessonType.QUIZ,
                difficulty = LessonDifficulty.ADVANCED,
                moduleId = "module_addendum",
                order = 1,
                questions = addendumQuestions,
                xpReward = 75,
                requiredLevel = 5,
                isLocked = true
            ),
            Lesson(
                id = "lesson_addendum_battle",
                title = "Batalla Final: Addendum",
                description = "Demuestra tu maestría",
                type = LessonType.BATTLE,
                difficulty = LessonDifficulty.EXPERT,
                moduleId = "module_addendum",
                order = 2,
                questions = addendumQuestions,
                xpReward = 150,
                requiredLevel = 6,
                isLocked = true
            ),
            Lesson(
                id = "resource_addendum_guide",
                title = "Guía Completa Addendum",
                description = "Referencia rápida",
                type = LessonType.RESOURCE,
                difficulty = LessonDifficulty.ADVANCED,
                moduleId = "module_addendum",
                order = 3,
                resourceContent = """
                    📋 ADDENDUM IMSS PENSIONADOS

                    ⚠️ PARA QUIÉN APLICA:

                    👨 CLIENTES HOMBRES CON TIPO DE PENSIÓN:

                    🔹 INVALIDEZ
                    • Con edad entre 51 y 70 años
                    • Edad máxima: 75 años incluyendo plazo de crédito

                    🔹 VEJEZ
                    • Con edad entre 51 y 70 años
                    • Edad máxima: 75 años incluyendo plazo de crédito

                    👥 TODO TIPO DE PENSIÓN
                    • Que con el plazo del crédito exceda los 80 años

                    ✅ IMPORTANTE: Verificar siempre el tipo de pensión y la edad del cliente antes de procesar el crédito
                """.trimIndent(),
                xpReward = 25,
                requiredLevel = 5,
                isLocked = true
            )
        )
    )

    // ========== MÓDULO 5: RESTRICCIONES DE INVALIDEZ ==========

    private val invalidezQuestions = listOf(
        Question(
            id = "invalidez_q1",
            type = QuestionType.TRUE_FALSE,
            question = "¿Un cliente con Invalidez de 51-70 años que es nuevo o tiene antigüedad <24 meses debe pagar seguro por un año?",
            options = listOf("Verdadero", "Falso"),
            correctAnswer = "Verdadero",
            explanation = "Si se encuentra dentro de la restricción de 51-70 años, aplica pago del seguro por un año siempre y cuando sea cliente nuevo o cuente con antigüedad de 24 meses o menos",
            difficulty = LessonDifficulty.EXPERT,
            xpReward = 100
        ),
        Question(
            id = "invalidez_q2",
            type = QuestionType.MULTIPLE_CHOICE,
            question = "Si el cliente con Invalidez está FUERA de la restricción de 51-70 años, ¿aplica pago del seguro?",
            options = listOf(
                "Sí, siempre",
                "No aplica pago del seguro",
                "Solo si es cliente nuevo",
                "Depende del monto"
            ),
            correctAnswer = "No aplica pago del seguro",
            explanation = "Cuando el cliente se encuentra FUERA de la restricción, no aplica pago del seguro siempre y cuando no exceda los 80 años",
            difficulty = LessonDifficulty.EXPERT,
            xpReward = 100
        ),
        Question(
            id = "invalidez_q3",
            type = QuestionType.TRUE_FALSE,
            question = "¿Si el cliente con Invalidez excede los 80 años, debe realizar el pago de un año de plazo del crédito?",
            options = listOf("Verdadero", "Falso"),
            correctAnswer = "Verdadero",
            explanation = "Cuando el cliente se encuentra fuera de la restricción, no aplica pago del seguro siempre y cuando no exceda los 80 años. Si excede, deberá realizar el pago de un año de plazo del crédito",
            difficulty = LessonDifficulty.EXPERT,
            xpReward = 100
        )
    )

    private val moduleInvalidez = Module(
        id = "module_invalidez",
        title = "Restricciones de Invalidez",
        description = "Casos especiales de pago de seguro",
        icon = "🛡️",
        color = JuicyRed,
        order = 5,
        isUnlocked = false,
        lessons = listOf(
            Lesson(
                id = "lesson_invalidez_rules",
                title = "Reglas de Invalidez",
                description = "Restricciones de edad y antigüedad",
                type = LessonType.QUIZ,
                difficulty = LessonDifficulty.EXPERT,
                moduleId = "module_invalidez",
                order = 1,
                questions = invalidezQuestions,
                xpReward = 100,
                requiredLevel = 7,
                isLocked = true
            ),
            Lesson(
                id = "lesson_invalidez_final_boss",
                title = "JEFE FINAL: Maestro de Invalidez",
                description = "La batalla definitiva",
                type = LessonType.BATTLE,
                difficulty = LessonDifficulty.EXPERT,
                moduleId = "module_invalidez",
                order = 2,
                questions = invalidezQuestions,
                xpReward = 200,
                requiredLevel = 8,
                isLocked = true
            ),
            Lesson(
                id = "resource_invalidez_flowchart",
                title = "Diagrama de Flujo Invalidez",
                description = "Guía visual de decisiones",
                type = LessonType.RESOURCE,
                difficulty = LessonDifficulty.EXPERT,
                moduleId = "module_invalidez",
                order = 3,
                resourceContent = """
                    🛡️ RESTRICCIONES DE INVALIDEZ - DIAGRAMA DE FLUJO

                    ❓ ¿El cliente tiene pensión por INVALIDEZ?

                    ✅ SÍ → Continuar evaluación

                    ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

                    📊 CASO 1: DENTRO DE RESTRICCIÓN (51-70 años)

                    ❓ ¿Cliente NUEVO o antigüedad ≤24 meses?
                    ✅ SÍ → ⚠️ APLICA PAGO DEL SEGURO POR 1 AÑO
                    ❌ NO → ✅ NO APLICA PAGO DEL SEGURO

                    ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

                    📊 CASO 2: FUERA DE RESTRICCIÓN (edad <51 o >70)

                    ❓ ¿Excede los 80 años con plazo del crédito?
                    ✅ SÍ → ⚠️ DEBE PAGAR 1 AÑO DE PLAZO DEL CRÉDITO
                    ❌ NO → ✅ NO APLICA PAGO DEL SEGURO

                    ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

                    💡 TIPS CLAVE:
                    • Siempre verificar la edad del cliente
                    • Revisar antigüedad en el sistema
                    • Calcular edad final (edad actual + plazo en años)
                    • Documentar el caso en el expediente
                """.trimIndent(),
                xpReward = 50,
                requiredLevel = 7,
                isLocked = true
            )
        )
    )

    // ========== MÓDULO 6: REFINANCIAMIENTOS (CNCA vs LCOM) ==========

    private val refinanceQuestions = listOf(
        // NIVEL 1: Conceptos Básicos (CNCA vs LCOM)
        Question(
            id = "refi_q1",
            type = QuestionType.MULTIPLE_CHOICE,
            question = "¿Qué cotizador nuevo no tiene restricciones de CNCA por ser el producto con la tasa más alta?",
            options = listOf(
                "OPC",
                "OPC MAX",
                "CSB",
                "CSB VIP"
            ),
            correctAnswer = "OPC",
            explanation = "OPC se autoriza sin restricciones de tasa en CNCA porque es el producto con la tasa más alta.",
            difficulty = LessonDifficulty.BEGINNER,
            xpReward = 10
        ),
        Question(
            id = "refi_q2",
            type = QuestionType.MULTIPLE_CHOICE,
            question = "Si queremos comprar una deuda de la competencia (LCOM), ¿cuántos pagos debe tener el cliente?",
            options = listOf(
                "16 pagos",
                "24 pagos",
                "12 pagos",
                "6 pagos"
            ),
            correctAnswer = "24 pagos",
            explanation = "El lineamiento LCOM especifica claramente: 'Tener al menos 24 pagos aplicados'.",
            difficulty = LessonDifficulty.BEGINNER,
            xpReward = 10
        ),
        Question(
            id = "refi_q3",
            type = QuestionType.MULTIPLE_CHOICE,
            question = "¿Cuál es la edad máxima permitida para un pensionado (plazo incluido) sin condiciones especiales?",
            options = listOf(
                "75 años",
                "80 años",
                "85 años",
                "70 años"
            ),
            correctAnswer = "80 años",
            explanation = "La política general indica 'Edad de hasta 80 años incluyendo plazo del crédito'.",
            difficulty = LessonDifficulty.BEGINNER,
            xpReward = 10
        ),
        // NIVEL 2: Las Compuertas Lógicas (Validación)
        Question(
            id = "refi_q4",
            type = QuestionType.MULTIPLE_CHOICE,
            question = "¿Cuánto puede bajar la tasa de CSB sin exigir aumento de plazo ni de descuento?",
            options = listOf(
                "Hasta 10 pb",
                "Hasta 25 pb",
                "Hasta 50 pb",
                "No puede bajar"
            ),
            correctAnswer = "Hasta 25 pb",
            explanation = "La nueva regla acepta subir o mantener la tasa, y también bajarla hasta 25 puntos base sin condición adicional.",
            difficulty = LessonDifficulty.INTERMEDIATE,
            xpReward = 25
        ),
        Question(
            id = "refi_q5",
            type = QuestionType.MULTIPLE_CHOICE,
            question = "Si la tasa baja entre 26 y 50 pb, ¿cuánto debe aumentar como mínimo el descuento mensual para cumplir esa alternativa?",
            options = listOf(
                "$500 pesos",
                "$750 pesos",
                "$1,000 pesos",
                "$1,500 pesos"
            ),
            correctAnswer = "$500 pesos",
            explanation = "La nueva compuerta permite bajar hasta 50 pb cuando el descuento aumenta $500 o más.",
            difficulty = LessonDifficulty.INTERMEDIATE,
            xpReward = 25
        ),
        Question(
            id = "refi_q6",
            type = QuestionType.MULTIPLE_CHOICE,
            question = "Si un cliente tiene 82 años (edad + plazo), ¿puede obtener el crédito?",
            options = listOf(
                "No, se rechaza automáticamente",
                "Sí, pero solo si paga el seguro por 1 año",
                "Sí, sin condiciones",
                "Solo si es cliente antiguo"
            ),
            correctAnswer = "Sí, pero solo si paga el seguro por 1 año",
            explanation = "El Addendum permite hasta 85 años 'si firma el formato de domiciliación por plazo de 1 año' (Seguro).",
            difficulty = LessonDifficulty.INTERMEDIATE,
            xpReward = 25
        ),
        // NIVEL 3: Situaciones Expertas (Casos de Uso)
        Question(
            id = "refi_q7",
            type = QuestionType.MULTIPLE_CHOICE,
            question = "Tienes un cliente de Invalidez de 55 años. Es Cliente Nuevo. ¿Cómo aplica su seguro?",
            options = listOf(
                "No aplica seguro",
                "Aplica seguro por 1 año",
                "Aplica seguro por todo el plazo",
                "Aplica solo si excede 80 años"
            ),
            correctAnswer = "Aplica seguro por 1 año",
            explanation = "Regla de Invalidez: 'Si se encuentra dentro de la restricción de 51-70 años aplica pago de seguro por un año si es cliente nuevo'.",
            difficulty = LessonDifficulty.ADVANCED,
            xpReward = 50
        ),
        Question(
            id = "refi_q8",
            type = QuestionType.MULTIPLE_CHOICE,
            question = "Si se liquidan varios créditos, ¿cuál se usa para validar la regla de tasa y plazo?",
            options = listOf(
                "El de mayor saldo",
                "El de menor saldo",
                "El de mayor tasa",
                "Todos deben cumplir por separado"
            ),
            correctAnswer = "El de mayor saldo",
            explanation = "Cuando se liquidan varios créditos, la regla se valida contra el crédito de mayor monto.",
            difficulty = LessonDifficulty.ADVANCED,
            xpReward = 50
        ),
        Question(
            id = "refi_q9",
            type = QuestionType.MULTIPLE_CHOICE,
            question = "Con varios créditos, ¿contra qué importe se valida el aumento de descuento de $500?",
            options = listOf(
                "Contra el descuento del crédito mayor",
                "Contra la suma de todos los descuentos",
                "Contra el descuento del crédito menor",
                "Contra el remanente"
            ),
            correctAnswer = "Contra la suma de todos los descuentos",
            explanation = "La mensualidad nueva debe superar por $500 o más la suma de los descuentos de todos los créditos a liquidar.",
            difficulty = LessonDifficulty.EXPERT,
            xpReward = 100
        ),
        Question(
            id = "refi_q10",
            type = QuestionType.MULTIPLE_CHOICE,
            question = "¿Cuál es el cálculo correcto de la 'Liquidez Real' para el cliente en un refinanciamiento?",
            options = listOf(
                "El Monto del nuevo préstamo",
                "Monto del nuevo préstamo MENOS saldo de la deuda anterior",
                "El saldo de la deuda anterior más intereses",
                "El descuento mensual multiplicado por el plazo"
            ),
            correctAnswer = "Monto del nuevo préstamo MENOS saldo de la deuda anterior",
            explanation = "Se demuestra en el ejemplo de libranza LCOM: Monto Solicitado ($64k) - Saldo Capital ($44k) = Importe a recibir ($19k).",
            difficulty = LessonDifficulty.EXPERT,
            xpReward = 100
        )
    )

    private val moduleRefinance = Module(
        id = "module_refinance",
        title = "Refinanciamientos",
        description = "CNCA vs LCOM - Compuertas Lógicas",
        icon = "🔄",
        color = JuicyBlue,
        order = 6,
        isUnlocked = false,
        lessons = listOf(
            Lesson(
                id = "lesson_refi_basics",
                title = "CNCA vs LCOM",
                description = "Diferencias entre negocio interno y competencia",
                type = LessonType.QUIZ,
                difficulty = LessonDifficulty.BEGINNER,
                moduleId = "module_refinance",
                order = 1,
                questions = refinanceQuestions.take(3),  // q1, q2, q3
                xpReward = 30,
                requiredLevel = 3,
                isLocked = true
            ),
            Lesson(
                id = "lesson_refi_cases",
                title = "Compuertas Lógicas",
                description = "Validaciones de tasa, liquidez y edad",
                type = LessonType.QUIZ,
                difficulty = LessonDifficulty.INTERMEDIATE,
                moduleId = "module_refinance",
                order = 2,
                questions = refinanceQuestions.slice(3..5),  // q4, q5, q6
                xpReward = 75,
                requiredLevel = 4,
                isLocked = true
            ),
            Lesson(
                id = "lesson_refi_logic_gates",
                title = "Casos Expertos",
                description = "Invalidez, plazos y liquidez real",
                type = LessonType.QUIZ,
                difficulty = LessonDifficulty.ADVANCED,
                moduleId = "module_refinance",
                order = 3,
                questions = refinanceQuestions.slice(6..9),  // q7, q8, q9, q10
                xpReward = 300,
                requiredLevel = 5,
                isLocked = true
            ),
            Lesson(
                id = "lesson_refi_battle",
                title = "Batalla: Maestro del Refinanciamiento",
                description = "Domina CNCA y LCOM",
                type = LessonType.BATTLE,
                difficulty = LessonDifficulty.EXPERT,
                moduleId = "module_refinance",
                order = 4,
                questions = refinanceQuestions,  // Todas las 10 preguntas
                xpReward = 500,
                requiredLevel = 6,
                isLocked = true
            ),
            Lesson(
                id = "resource_refi_guide",
                title = "Guía CNCA vs LCOM",
                description = "Las dos dimensiones del refinanciamiento",
                type = LessonType.RESOURCE,
                difficulty = LessonDifficulty.INTERMEDIATE,
                moduleId = "module_refinance",
                order = 5,
                resourceContent = """
                    🔄 GUÍA COMPLETA: CNCA vs LCOM

                    📘 LAS DOS DIMENSIONES DEL REFINANCIAMIENTO

                    ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

                    1️⃣ CNCA INTERNO (Negocio de Grupo)

                    🎯 OBJETIVO:
                    Mantener al cliente con los nuevos cotizadores OPC y CSB

                    📊 LÓGICA:
                    Comparar la tasa nueva contra la deuda de mayor saldo

                    ✅ REGLAS NUEVAS:
                    • OPC base: sin restricciones
                    • Subir o mantener tasa: permitido
                    • Bajar hasta 25 pb: permitido
                    • Bajar hasta 50 pb: aumentar plazo 6 meses o más
                    • Alternativa: bajar hasta 50 pb aumentando descuento $500 o más

                    📚 SI HAY VARIOS CRÉDITOS:
                    • Tasa y plazo: validar el crédito de mayor saldo
                    • Aumento de descuento: comparar contra la suma de descuentos

                    ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

                    2️⃣ LCOM (Compra de Competencia / Conquista)

                    🎯 OBJETIVO:
                    Traer clientes de afuera (Profuturo, Dimex, Exitus, etc.)

                    📊 LÓGICA:
                    SÍ buscamos mejorar la tasa

                    ✅ REGLA DE ORO:
                    Nuestra tasa debe ser MENOR a la del competidor

                    ⏱️ ANTIGÜEDAD MÍNIMA:
                    24 pagos aplicados (más estricta)

                    💰 LIQUIDEZ REAL:
                    Monto Nuevo - Saldo Capital = Dinero que recibe

                    ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

                    📋 COMPUERTAS LÓGICAS GENERALES:

                    🔹 EDAD MÁXIMA:
                    • Hasta 80 años (plazo incluido)
                    • Hasta 85 años con domiciliación por 1 año

                    🔹 INVALIDEZ (51-70 años):
                    • Cliente Nuevo o antigüedad ≤24 meses
                    • Aplica seguro por 1 año

                    🔹 ADDENDUM:
                    • Hombre + Invalidez/Vejez + 51-70 años
                    • Edad máx 75 años

                    🔹 COMPUERTA DE PLAZO CNCA:
                    • Para una baja de 26 a 50 pb
                    • El plazo nuevo aumenta al menos 6 meses

                    ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

                    💡 EJEMPLO LCOM (Libranza):

                    Monto Solicitado: $64,000
                    Saldo Capital: $44,000
                    ═══════════════
                    Importe a Recibir: $19,000 ✅

                    ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

                    📅 NUEVOS COTIZADORES:

                    • OPC: 12, 24, 36, 48, 54 y 60 meses
                    • OPC MAX: 60 meses, mínimo $50,000
                    • OPC ESP: 60 meses, mínimo $75,000
                    • CSB: 12, 24, 36, 48, 54 y 60 meses
                    • CSB ESP: 60 meses, mínimo $100,000
                    • CSB VIP: 60 meses, mínimo $150,000

                    ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

                    ⚠️ ERRORES COMUNES:

                    ❌ Confundir CNCA con LCOM
                    ✅ CNCA = reglas de tasa, plazo y descuento
                    ✅ LCOM = Competencia (24 pagos)

                    ❌ Validar todos los créditos contra la regla de tasa
                    ✅ Usar el de mayor saldo

                    ❌ No calcular la liquidez real
                    ✅ Siempre restar el saldo capital

                    ❌ Ofrecer refinanciamiento sin liquidez
                    ✅ Asegurar beneficio real para el cliente
                """.trimIndent(),
                xpReward = 50,
                requiredLevel = 3,
                isLocked = true
            )
        )
    )

    // ========== LISTA COMPLETA DE MÓDULOS ==========

    val ALL_MODULES = listOf(
        moduleEmailUpdate,
        moduleCreditSubjects,
        moduleCreditPolicy,
        moduleAddendum,
        moduleInvalidez,
        moduleRefinance
    )

    /**
     * Obtiene un módulo por su ID
     */
    fun getModuleById(id: String): Module? {
        return ALL_MODULES.find { it.id == id }
    }

    /**
     * Obtiene una lección por su ID
     */
    fun getLessonById(lessonId: String): Lesson? {
        return ALL_MODULES
            .flatMap { it.lessons }
            .find { it.id == lessonId }
    }

    /**
     * Obtiene todas las lecciones de un módulo
     */
    fun getLessonsForModule(moduleId: String): List<Lesson> {
        return getModuleById(moduleId)?.lessons ?: emptyList()
    }
}
