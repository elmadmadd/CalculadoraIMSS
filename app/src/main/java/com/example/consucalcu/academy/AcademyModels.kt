package com.example.consucalcu.academy

import androidx.compose.ui.graphics.Color
import com.example.consucalcu.*

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
