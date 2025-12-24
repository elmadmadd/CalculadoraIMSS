# 🎓 SISTEMA DE ACADEMIA - DOCUMENTACIÓN COMPLETA

## 📋 Resumen General

Se ha implementado un **sistema de aprendizaje gamificado tipo Duolingo/Candy Crush** para capacitar a los asesores en políticas IMSS, actualización de correo y procedimientos importantes.

---

## 🏗️ Arquitectura del Sistema

### Módulos Creados

```
academy/
├── AcademyModels.kt      - Modelos de datos (Lesson, Module, Question, etc.)
├── AcademyContent.kt     - Contenido educativo basado en presentaciones
├── AcademyViewModel.kt   - Lógica de negocio y gestión de estado
├── AcademyUI.kt          - Pantalla principal con camino de aprendizaje
├── BattleScreen.kt       - Sistema de batallas RPG
└── QuizScreen.kt         - Cuestionarios estilo Duolingo
```

---

## 🎮 Características Principales

### 1. **Sistema de Niveles y XP**
- **Nivel inicial**: 1
- **XP por lección**: Variable según dificultad
  - Principiante: 10 XP
  - Intermedio: 25 XP
  - Avanzado: 50 XP
  - Experto: 100 XP
- **Fórmula de XP**: `100 * nivel^1.5` (curva exponencial suave)
- **Progreso persistente**: Guardado en SharedPreferences

### 2. **Sistema de Estrellas** ⭐
- **3 estrellas**: 100% de aciertos
- **2 estrellas**: 80-99% de aciertos
- **1 estrella**: 60-79% de aciertos
- **0 estrellas**: <60% (no pasa la lección)

### 3. **Sistema de Racha** 🔥
- Contador de días consecutivos estudiando
- Se reinicia si pasa más de 1 día sin actividad
- Visible en el header de la Academia

### 4. **Camino de Aprendizaje** 🛤️
- Diseño tipo Duolingo/Candy Crush
- Lecciones conectadas visualmente
- Desbloqueo progresivo:
  - Primer módulo desbloqueado por defecto
  - Siguientes módulos se desbloquean al completar el anterior
  - Lecciones se desbloquean secuencialmente

---

## 📚 Módulos Educativos

### **Módulo 1: Actualización de Correo** 📧
**Color**: Azul (`JuicyBlue`)
**Estado**: Desbloqueado por defecto

**Lecciones**:
1. **Introducción a la Actualización** (Quiz - Principiante)
   - Requisitos básicos
   - 2 preguntas
   - 20 XP

2. **Formato de Actualización** (Batalla RPG - Intermedio)
   - Texto oficial del formato
   - 3 preguntas
   - 50 XP

3. **Plantilla de Correo** (Recurso)
   - Texto oficial para copiar
   - 10 XP

### **Módulo 2: Sujetos a Crédito IMSS** 👥
**Color**: Verde (`JuicyGreen`)
**Estado**: Se desbloquea al completar Módulo 1

**Lecciones**:
1. **Tipos de Pensión** (Quiz - Principiante)
   - Cesantía, Invalidez, Vejez, Viudez
   - 3 preguntas
   - 30 XP

2. **Batalla: Domina las Pensiones** (Batalla RPG - Avanzado)
   - 5 preguntas
   - 100 XP

### **Módulo 3: Políticas de Crédito** 💳
**Color**: Morado (`JuicyPurple`)
**Requisito**: Nivel 2

**Lecciones**:
1. **Requisitos Básicos** (Quiz - Principiante)
   - Edad, plazos, montos
   - 3 preguntas
   - 30 XP

2. **Resumen de Políticas** (Recurso)
   - Guía rápida de consulta
   - 10 XP

### **Módulo 4: Addendum IMSS Pensionados** 📋
**Color**: Naranja (`JuicyOrange`)
**Requisito**: Nivel 5

**Lecciones**:
1. **Reglas del Addendum** (Quiz - Avanzado)
   - Casos especiales de edad y pensión
   - 3 preguntas
   - 75 XP

2. **Batalla Final: Addendum** (Batalla RPG - Experto)
   - 3 preguntas
   - 150 XP

3. **Guía Completa Addendum** (Recurso)
   - Referencia rápida
   - 25 XP

### **Módulo 5: Restricciones de Invalidez** 🛡️
**Color**: Rojo (`JuicyRed`)
**Requisito**: Nivel 7

**Lecciones**:
1. **Reglas de Invalidez** (Quiz - Experto)
   - Restricciones de edad y antigüedad
   - 3 preguntas
   - 100 XP

2. **JEFE FINAL: Maestro de Invalidez** (Batalla RPG - Experto)
   - 3 preguntas
   - 200 XP

3. **Diagrama de Flujo Invalidez** (Recurso)
   - Guía visual de decisiones
   - 50 XP

---

## ⚔️ Sistema de Batallas RPG

### Mecánica de Combate
- **HP Inicial**: Jugador 100 HP | Enemigo 100 HP
- **Daño por respuesta correcta**: -25 HP al enemigo
- **Daño por respuesta incorrecta**: -20 HP al jugador
- **Victoria**: Derrotar al enemigo o completar todas las preguntas
- **Derrota**: HP del jugador llega a 0

### Características Visuales
- Barras de HP animadas con colores dinámicos:
  - Verde: >60 HP
  - Amarillo: 30-60 HP
  - Rojo: <30 HP
- Animación de shake cuando recibe daño
- Emojis animados (👹 para enemigos, 🛡️ para jugador)
- Pantalla de resultados con confetti (si gana)

---

## 📝 Tipos de Preguntas

### 1. **Opción Múltiple** (MULTIPLE_CHOICE)
- 4 opciones
- 1 respuesta correcta
- Icono: ✓

### 2. **Verdadero/Falso** (TRUE_FALSE)
- 2 opciones
- Icono: ?

### 3. **Completar** (FILL_BLANK)
- Llenar espacios en blanco
- Icono: ✏️

### 4. **Emparejar** (MATCH_PAIRS)
- Relacionar conceptos
- Icono: 🔗

---

## 🎨 Diseño Visual

### Paleta de Colores por Módulo
- **Módulo 1**: Azul (`#0A84FF`)
- **Módulo 2**: Verde (`#00E676`)
- **Módulo 3**: Morado (`#7C4DFF`)
- **Módulo 4**: Naranja (`#FF6D00`)
- **Módulo 5**: Rojo (`#FF1744`)

### Animaciones
- Entrada de lecciones con stagger
- Transiciones de pestañas suaves
- Escalado en botones (bouncy)
- Shake en batallas
- Confetti en victorias

---

## 📊 Contenido Educativo Extraído

### De las Presentaciones Proporcionadas:

#### **Actualización de Correo**
✅ Requisitos:
- Biométrico exitoso
- INE de cliente
- Formato de actualización
- Video de cliente

✅ Texto oficial:
```
Hoy (DD/MM/AAAA) POR MEDIO DEL PRESENTE YO (NOMBRE COMPLETO DE CLIENTE) 
OTORGO MI CONSENTIMIENTO EXPLÍCITO A (RAZÓN SOCIAL CORRESPONDIENTE) 
PARA ACTUALIZAR MIS DATOS PERSONALES. SOLICITO LA ACTUALIZACIÓN DE MI 
CORREO ELECTRÓNICO (CORREO)
```

#### **Sujetos a Crédito**
- **Cesantía**: Retiro 60-64 años
- **Invalidez**: Imposibilidad de realizar labores remuneradas
- **Vejez**: Retiro 65 años
- **Viudez-Orfandad**: Viude/a o concubina del trabajador/a
- **Incapacidad permanente total**: Imposibilidad de reincorporarse
- **Incapacidad permanente parcial**: Posibilidad de reincorporarse

#### **Políticas de Crédito**
- **Edad máxima**: 80 años (85 con domiciliación)
- **Plazos**: 12, 24, 36, 48, 60 meses
- **Monto mínimo**: $8,000.00

#### **Addendum IMSS**
- Hombres con Invalidez/Vejez (51-70 años): máx 75 años
- Todo tipo de pensión que exceda 80 años

#### **Restricciones de Invalidez**
- Dentro de 51-70 años + nuevo/antigüedad ≤24 meses: pago seguro 1 año
- Fuera de restricción + excede 80 años: pago 1 año de plazo

---

## 🔧 Integración con MainActivity

### Cambios Realizados:
1. **Imports agregados**:
   - `import com.example.consucalcu.academy.*`
   - Iconos: `School`, `ContentCopy`, `CheckCircle`, `Cancel`, `Edit`, `Link`, `Lock`, `StarOutline`

2. **Navegación**:
   - Nueva pestaña "Academia" (índice 3)
   - Icono: `Icons.Rounded.School`
   - Color: `JuicyOrange`

3. **Pager actualizado**:
   - `pageCount = { 4 }` (antes era 3)
   - Caso agregado: `3 -> AcademyScreen(soundManager = soundManager)`

---

## 🚀 Uso del Sistema

### Para el Usuario (Asesor):
1. Abrir la app y navegar a la pestaña "Academia" 🎓
2. Ver el header con nivel, XP y racha
3. Seleccionar un módulo desbloqueado
4. Completar lecciones en orden:
   - **Quiz**: Responder preguntas, obtener feedback inmediato
   - **Batalla**: Combate RPG contra un jefe
   - **Recurso**: Leer y copiar plantillas/textos

5. Ganar XP y estrellas
6. Desbloquear nuevos módulos al completar el anterior
7. Mantener la racha estudiando diariamente

### Para el Desarrollador:
- **Agregar nuevo módulo**: Editar `AcademyContent.kt`
- **Agregar preguntas**: Crear objetos `Question` en el módulo correspondiente
- **Modificar XP**: Ajustar `LevelSystem.xpForLevel()`
- **Cambiar criterios de estrellas**: Editar `StarRating.calculateStars()`

---

## 📱 Persistencia de Datos

### SharedPreferences:
- **Clave**: `"academy_prefs"`
- **Datos guardados**:
  - Nivel actual
  - XP total y actual
  - Lecciones completadas
  - Módulos desbloqueados
  - Estrellas por lección
  - Racha de días
  - Última fecha de estudio

### Formato JSON:
```json
{
  "currentLevel": 3,
  "currentXP": 150,
  "xpToNextLevel": 200,
  "totalXP": 450,
  "completedLessons": ["lesson_email_intro", "lesson_email_format"],
  "unlockedModules": ["module_email", "module_credit_subjects"],
  "lessonStars": {
    "lesson_email_intro": 3,
    "lesson_email_format": 2
  },
  "streak": 5,
  "lastStudyDate": 1703376000000
}
```

---

## ✅ Checklist de Implementación

- [x] Modelos de datos (Lesson, Module, Question, etc.)
- [x] Contenido educativo basado en presentaciones
- [x] ViewModel con lógica de negocio
- [x] Sistema de niveles y XP
- [x] Sistema de estrellas
- [x] Sistema de racha
- [x] Pantalla principal con camino de aprendizaje
- [x] Pantalla de batallas RPG
- [x] Pantalla de cuestionarios
- [x] Diálogo de recursos
- [x] Persistencia de datos
- [x] Integración con MainActivity
- [x] Navegación con 4 pestañas
- [x] Animaciones y transiciones

---

## 🎯 Próximos Pasos Sugeridos

1. **Testing**: Probar todas las lecciones y batallas
2. **Agregar más contenido**: Más módulos según necesidades
3. **Estadísticas**: Panel de progreso global
4. **Logros**: Sistema de achievements
5. **Leaderboard**: Ranking entre asesores (opcional)
6. **Notificaciones**: Recordatorios para mantener racha
7. **Modo offline**: Sincronización cuando hay conexión

---

## 📞 Soporte

Para agregar más contenido educativo:
1. Editar `AcademyContent.kt`
2. Crear nuevas preguntas en el formato:
```kotlin
Question(
    id = "unique_id",
    type = QuestionType.MULTIPLE_CHOICE,
    question = "¿Tu pregunta?",
    options = listOf("A", "B", "C", "D"),
    correctAnswer = "A",
    explanation = "Explicación detallada",
    difficulty = LessonDifficulty.BEGINNER,
    xpReward = 10
)
```

---

**¡Sistema de Academia listo para usar! 🎉**
