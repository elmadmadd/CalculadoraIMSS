package com.example.consucalcu.academy

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ACADEMY VIEWMODEL
 * Gestiona el estado y lógica de negocio de la Academia
 */
class AcademyViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("academy_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    // ========== STATE FLOWS ==========

    private val _progress = MutableStateFlow(loadProgress())
    val progress: StateFlow<AcademyProgress> = _progress.asStateFlow()

    private val _currentBattle = MutableStateFlow<BattleState?>(null)
    val currentBattle: StateFlow<BattleState?> = _currentBattle.asStateFlow()

    private val _modules = MutableStateFlow(loadModulesWithProgress())
    val modules: StateFlow<List<Module>> = _modules.asStateFlow()

    // ========== PROGRESS MANAGEMENT ==========

    /**
     * Carga el progreso guardado o crea uno nuevo
     */
    private fun loadProgress(): AcademyProgress {
        val json = prefs.getString("progress", null)
        return if (json != null) {
            try {
                gson.fromJson(json, AcademyProgress::class.java) ?: AcademyProgress()
            } catch (e: Exception) {
                AcademyProgress()
            }
        } else {
            AcademyProgress()
        }
    }

    /**
     * Guarda el progreso en SharedPreferences
     */
    private fun saveProgress(progress: AcademyProgress) {
        val json = gson.toJson(progress)
        prefs.edit { putString("progress", json) }
    }

    /**
     * Carga los módulos y actualiza su estado según el progreso
     */
    private fun loadModulesWithProgress(): List<Module> {
        val currentProgress = _progress.value

        return AcademyContent.ALL_MODULES.map { module ->
            val isUnlocked = currentProgress.unlockedModules.contains(module.id)
            val completedLessons = module.lessons.count { lesson ->
                currentProgress.completedLessons.contains(lesson.id)
            }

            val updatedLessons = module.lessons.map { lesson ->
                val isCompleted = currentProgress.completedLessons.contains(lesson.id)
                val stars = currentProgress.lessonStars[lesson.id] ?: 0

                // Desbloquear lección si:
                // 1. Es la primera lección del módulo desbloqueado
                // 2. La lección anterior está completada
                val isLocked = if (!isUnlocked) {
                    true
                } else {
                    if (lesson.order == 1) {
                        false
                    } else {
                        val previousLesson = module.lessons.find { it.order == lesson.order - 1 }
                        previousLesson?.let { !currentProgress.completedLessons.contains(it.id) } ?: true
                    }
                }

                lesson.copy(
                    isLocked = isLocked,
                    isCompleted = isCompleted,
                    stars = stars
                )
            }

            module.copy(
                isUnlocked = isUnlocked,
                completedLessons = completedLessons,
                lessons = updatedLessons
            )
        }
    }

    /**
     * Actualiza el progreso y recalcula módulos
     */
    private fun updateProgress(updater: (AcademyProgress) -> AcademyProgress) {
        _progress.update { current ->
            val updated = updater(current)
            saveProgress(updated)
            updated
        }
        _modules.value = loadModulesWithProgress()
    }

    // ========== LESSON COMPLETION ==========

    /**
     * Completa una lección y otorga XP
     */
    fun completeLesson(lessonId: String, correctAnswers: Int, totalQuestions: Int) {
        val lesson = AcademyContent.getLessonById(lessonId) ?: return
        val stars = StarRating.calculateStars(correctAnswers, totalQuestions)

        // No otorgar XP si no pasó (0 estrellas)
        if (stars == 0) return

        updateProgress { current ->
            AcademyProgressRules.completeLesson(
                current = current,
                lesson = lesson,
                stars = stars,
                completedAtMillis = System.currentTimeMillis()
            )
        }
    }

    // ========== BATTLE SYSTEM ==========

    /**
     * Inicia una batalla RPG
     */
    fun startBattle(lessonId: String) {
        val lesson = AcademyContent.getLessonById(lessonId) ?: return
        if (lesson.questions.isEmpty()) return

        _currentBattle.value = BattleState(
            currentQuestionIndex = 0,
            playerHP = 100,
            enemyHP = 100,
            correctAnswers = 0,
            totalQuestions = lesson.questions.size,
            isVictory = false,
            isDefeat = false
        )
    }

    /**
     * Procesa una respuesta en la batalla
     */
    fun answerBattleQuestion(lessonId: String, isCorrect: Boolean) {
        AcademyContent.getLessonById(lessonId) ?: return
        val battle = _currentBattle.value ?: return
        val result = BattleRules.answer(battle, isCorrect)
        _currentBattle.value = result

        // Si terminó la batalla, completar lección
        if (!battle.isVictory && result.isVictory) {
            completeLesson(lessonId, result.correctAnswers, result.totalQuestions)
        }
    }

    /**
     * Finaliza la batalla actual
     */
    fun endBattle() {
        _currentBattle.value = null
    }

    // ========== UTILITY FUNCTIONS ==========

    /**
     * Obtiene el progreso de un módulo específico
     */
    fun getModuleProgress(moduleId: String): Float {
        val module = _modules.value.find { it.id == moduleId } ?: return 0f
        if (module.totalLessons == 0) return 0f
        return module.completedLessons.toFloat() / module.totalLessons.toFloat()
    }

    /**
     * Obtiene el total de estrellas obtenidas
     */
    fun getTotalStars(): Int {
        return _progress.value.lessonStars.values.sum()
    }

    /**
     * Obtiene el total de lecciones completadas
     */
    fun getTotalCompletedLessons(): Int {
        return _progress.value.completedLessons.size
    }

    /**
     * Reinicia todo el progreso (para testing)
     */
    fun resetProgress() {
        updateProgress { AcademyProgress() }
    }
}
