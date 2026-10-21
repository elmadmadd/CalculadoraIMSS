package com.example.consucalcu.academy

import androidx.compose.ui.graphics.Color
import com.example.consucalcu.*
import java.util.Calendar

/**
 * ACADEMY SYSTEM - Data Models
 * Sistema de aprendizaje gamificado tipo Duolingo/RPG para asesores
 */

// ========== ENUMS ==========

enum class LessonType {
    QUIZ,           // Cuestionario múltiple opción
    BATTLE,         // Batalla RPG con preguntas
    PRACTICE,       // Práctica/Repaso
    RESOURCE        // Recurso educativo (plantillas, textos)
}

enum class LessonDifficulty {
    BEGINNER,       // Principiante (10 XP)
    INTERMEDIATE,   // Intermedio (25 XP)
    ADVANCED,       // Avanzado (50 XP)
    EXPERT          // Experto (100 XP)
}

enum class QuestionType {
    MULTIPLE_CHOICE,    // Opción múltiple
    TRUE_FALSE,         // Verdadero/Falso
    FILL_BLANK,         // Llenar espacios
    MATCH_PAIRS         // Emparejar conceptos
}

// ========== DATA CLASSES ==========

/**
 * Representa una pregunta individual en un cuestionario o batalla
 */
data class Question(
    val id: String,
    val type: QuestionType,
    val question: String,
    val options: List<String> = emptyList(),
    val correctAnswer: String,
    val explanation: String = "",
    val difficulty: LessonDifficulty = LessonDifficulty.BEGINNER,
    val xpReward: Int = 10
)

/**
 * Representa una lección completa en la academia
 */
data class Lesson(
    val id: String,
    val title: String,
    val description: String,
    val type: LessonType,
    val difficulty: LessonDifficulty,
    val moduleId: String,
    val order: Int,
    val questions: List<Question> = emptyList(),
    val resourceContent: String = "",
    val xpReward: Int = when(difficulty) {
        LessonDifficulty.BEGINNER -> 10
        LessonDifficulty.INTERMEDIATE -> 25
        LessonDifficulty.ADVANCED -> 50
        LessonDifficulty.EXPERT -> 100
    },
    val requiredLevel: Int = 1,
    val isLocked: Boolean = true,
    val isCompleted: Boolean = false,
    val stars: Int = 0  // 0-3 estrellas según desempeño
)

/**
 * Representa un módulo temático (ej: "Actualización de Correo", "Políticas IMSS")
 */
data class Module(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,  // Emoji o identificador de icono
    val color: Color,
    val lessons: List<Lesson>,
    val order: Int,
    val isUnlocked: Boolean = false,
    val completedLessons: Int = 0,
    val totalLessons: Int = lessons.size
)

/**
 * Progreso del usuario en la academia
 */
data class AcademyProgress(
    val currentLevel: Int = 1,
    val currentXP: Int = 0,
    val xpToNextLevel: Int = 100,
    val totalXP: Int = 0,
    val completedLessons: Set<String> = emptySet(),
    val unlockedModules: Set<String> = setOf("module_email"),  // Primer módulo desbloqueado
    val lessonStars: Map<String, Int> = emptyMap(),  // lessonId -> stars (0-3)
    val streak: Int = 0,  // Días consecutivos estudiando
    val lastStudyDate: Long = 0L
)

/**
 * Estado de una batalla RPG en progreso
 */
data class BattleState(
    val currentQuestionIndex: Int = 0,
    val playerHP: Int = 100,
    val enemyHP: Int = 100,
    val correctAnswers: Int = 0,
    val totalQuestions: Int = 0,
    val isVictory: Boolean = false,
    val isDefeat: Boolean = false
)

/**
 * Recurso educativo (plantillas, textos, etc.)
 */
data class Resource(
    val id: String,
    val title: String,
    val category: String,  // "Plantillas", "Textos", "Guías"
    val content: String,
    val isCopyable: Boolean = true,
    val moduleId: String
)

// ========== LEVEL SYSTEM ==========

object LevelSystem {
    /**
     * Calcula el XP necesario para alcanzar el siguiente nivel
     * Fórmula: 100 * nivel^1.5 (curva exponencial suave)
     */
    fun xpForLevel(level: Int): Int {
        return (100 * Math.pow(level.toDouble(), 1.5)).toInt()
    }

    /**
     * Calcula el nivel actual basado en XP total
     */
    fun calculateLevel(totalXP: Int): Int {
        var level = 1
        var xpRequired = xpForLevel(level)
        var accumulatedXP = 0

        while (accumulatedXP + xpRequired <= totalXP) {
            accumulatedXP += xpRequired
            level++
            xpRequired = xpForLevel(level)
        }

        return level
    }

    /**
     * Calcula el XP restante para el siguiente nivel
     */
    fun xpToNextLevel(totalXP: Int): Int {
        val currentLevel = calculateLevel(totalXP)
        val xpForCurrentLevel = (1 until currentLevel).sumOf { xpForLevel(it) }
        val xpForNextLevel = xpForLevel(currentLevel)

        return xpForNextLevel - (totalXP - xpForCurrentLevel)
    }
}

// ========== STAR RATING SYSTEM ==========

object StarRating {
    /**
     * Calcula las estrellas obtenidas según el porcentaje de aciertos
     * 100% = 3 estrellas
     * 80-99% = 2 estrellas
     * 60-79% = 1 estrella
     * <60% = 0 estrellas (no pasa)
     */
    fun calculateStars(correctAnswers: Int, totalQuestions: Int): Int {
        if (totalQuestions == 0) return 0

        val percentage = (correctAnswers.toFloat() / totalQuestions) * 100

        return when {
            percentage >= 100f -> 3
            percentage >= 80f -> 2
            percentage >= 60f -> 1
            else -> 0
        }
    }
}

/**
 * Reglas puras de progreso. Mantenerlas fuera del ViewModel permite probarlas sin
 * Android y evita que una recomposición o reapertura otorgue recompensas dobles.
 */
object AcademyProgressRules {
    fun completeLesson(
        current: AcademyProgress,
        lesson: Lesson,
        stars: Int,
        completedAtMillis: Long
    ): AcademyProgress {
        if (stars <= 0) return current

        val isFirstCompletion = lesson.id !in current.completedLessons
        val newTotalXP = current.totalXP + if (isFirstCompletion) lesson.xpReward else 0
        val newLevel = LevelSystem.calculateLevel(newTotalXP)
        val currentModule = AcademyContent.getModuleById(lesson.moduleId)
        val completedLessons = current.completedLessons + lesson.id
        val unlockedModules = if (
            currentModule != null &&
            currentModule.lessons.all { it.id in completedLessons }
        ) {
            AcademyContent.ALL_MODULES
                .firstOrNull { it.order == currentModule.order + 1 }
                ?.let { current.unlockedModules + it.id }
                ?: current.unlockedModules
        } else {
            current.unlockedModules
        }

        val updated = current.copy(
            currentLevel = newLevel,
            currentXP = newTotalXP - (1 until newLevel).sumOf(LevelSystem::xpForLevel),
            xpToNextLevel = LevelSystem.xpToNextLevel(newTotalXP),
            totalXP = newTotalXP,
            completedLessons = completedLessons,
            unlockedModules = unlockedModules,
            lessonStars = current.lessonStars + (
                lesson.id to maxOf(stars, current.lessonStars[lesson.id] ?: 0)
            )
        )

        return recordStudyActivity(updated, completedAtMillis)
    }

    fun recordStudyActivity(
        current: AcademyProgress,
        activityAtMillis: Long
    ): AcademyProgress {
        val activityDay = calendarDay(activityAtMillis)
        val lastStudyDay = current.lastStudyDate
            .takeIf { it > 0L }
            ?.let(::calendarDay)

        val yesterday = Calendar.getInstance().apply {
            timeInMillis = activityAtMillis
            set(Calendar.HOUR_OF_DAY, 12)
            add(Calendar.DAY_OF_YEAR, -1)
        }.let { it.get(Calendar.YEAR) to it.get(Calendar.DAY_OF_YEAR) }

        val newStreak = when (lastStudyDay) {
            activityDay -> current.streak
            yesterday -> current.streak.coerceAtLeast(0) + 1
            else -> 1
        }

        return current.copy(
            streak = newStreak,
            lastStudyDate = activityAtMillis
        )
    }

    private fun calendarDay(timestamp: Long): Pair<Int, Int> =
        Calendar.getInstance().apply { timeInMillis = timestamp }.let {
            it.get(Calendar.YEAR) to it.get(Calendar.DAY_OF_YEAR)
        }
}

/** Reglas deterministas de combate para evitar resultados ambiguos. */
object BattleRules {
    fun answer(current: BattleState, isCorrect: Boolean): BattleState {
        if (current.isVictory || current.isDefeat || current.totalQuestions <= 0) return current

        val enemyHP = (current.enemyHP - if (isCorrect) 25 else 0).coerceAtLeast(0)
        val playerHP = (current.playerHP - if (isCorrect) 0 else 20).coerceAtLeast(0)
        val correctAnswers = current.correctAnswers + if (isCorrect) 1 else 0
        val questionIndex = (current.currentQuestionIndex + 1).coerceAtMost(current.totalQuestions)
        val questionsFinished = questionIndex >= current.totalQuestions
        val passed = StarRating.calculateStars(correctAnswers, current.totalQuestions) > 0
        val isVictory = playerHP > 0 && (enemyHP <= 0 || (questionsFinished && passed))
        val isDefeat = !isVictory && (playerHP <= 0 || questionsFinished)

        return current.copy(
            currentQuestionIndex = questionIndex,
            playerHP = playerHP,
            enemyHP = enemyHP,
            correctAnswers = correctAnswers,
            isVictory = isVictory,
            isDefeat = isDefeat
        )
    }
}
