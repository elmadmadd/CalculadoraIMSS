package com.example.consucalcu.academy

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * ACADEMY VIEWMODEL
 * Gestiona el estado y lógica de negocio de la Academia
 */
class AcademyViewModel(private val context: Context) : ViewModel() {
    
    private val prefs = context.getSharedPreferences("academy_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    // ========== STATE FLOWS ==========
    
    private val _progress = MutableStateFlow(loadProgress())
    val progress: StateFlow<AcademyProgress> = _progress.asStateFlow()
    
    private val _currentBattle = MutableStateFlow<BattleState?>(null)
    val currentBattle: StateFlow<BattleState?> = _currentBattle.asStateFlow()
    
    private val _modules = MutableStateFlow(loadModulesWithProgress())
    val modules: StateFlow<List<Module>> = _modules.asStateFlow()
    
    // ========== INITIALIZATION ==========
    
    init {
        checkAndUpdateStreak()
    }
    
    // ========== PROGRESS MANAGEMENT ==========
    
    /**
     * Carga el progreso guardado o crea uno nuevo
     */
    private fun loadProgress(): AcademyProgress {
        val json = prefs.getString("progress", null)
        return if (json != null) {
            try {
                gson.fromJson(json, AcademyProgress::class.java)
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
        prefs.edit().putString("progress", json).apply()
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
            val newTotalXP = current.totalXP + lesson.xpReward
            val newLevel = LevelSystem.calculateLevel(newTotalXP)
            val xpToNext = LevelSystem.xpToNextLevel(newTotalXP)
            
            // Desbloquear siguiente módulo si completó todas las lecciones del actual
            val currentModule = AcademyContent.getModuleById(lesson.moduleId)
            val newUnlockedModules = if (currentModule != null) {
                val allLessonsCompleted = currentModule.lessons.all { 
                    current.completedLessons.contains(it.id) || it.id == lessonId
                }
                
                if (allLessonsCompleted) {
                    val nextModule = AcademyContent.ALL_MODULES.find { it.order == currentModule.order + 1 }
                    if (nextModule != null) {
                        current.unlockedModules + nextModule.id
                    } else {
                        current.unlockedModules
                    }
                } else {
                    current.unlockedModules
                }
            } else {
                current.unlockedModules
            }
            
            current.copy(
                currentLevel = newLevel,
                currentXP = newTotalXP - (1 until newLevel).sumOf { LevelSystem.xpForLevel(it) },
                xpToNextLevel = xpToNext,
                totalXP = newTotalXP,
                completedLessons = current.completedLessons + lessonId,
                unlockedModules = newUnlockedModules,
                lessonStars = current.lessonStars + (lessonId to maxOf(stars, current.lessonStars[lessonId] ?: 0))
            )
        }
    }
    
    // ========== BATTLE SYSTEM ==========
    
    /**
     * Inicia una batalla RPG
     */
    fun startBattle(lessonId: String) {
        val lesson = AcademyContent.getLessonById(lessonId) ?: return
        
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
        val lesson = AcademyContent.getLessonById(lessonId) ?: return
        val battle = _currentBattle.value ?: return
        
        val damageToEnemy = if (isCorrect) 25 else 0
        val damageToPlayer = if (!isCorrect) 20 else 0
        
        val newEnemyHP = (battle.enemyHP - damageToEnemy).coerceAtLeast(0)
        val newPlayerHP = (battle.playerHP - damageToPlayer).coerceAtLeast(0)
        val newCorrectAnswers = if (isCorrect) battle.correctAnswers + 1 else battle.correctAnswers
        val newQuestionIndex = battle.currentQuestionIndex + 1
        
        val isVictory = newEnemyHP <= 0 || newQuestionIndex >= battle.totalQuestions
        val isDefeat = newPlayerHP <= 0
        
        _currentBattle.value = battle.copy(
            currentQuestionIndex = newQuestionIndex,
            playerHP = newPlayerHP,
            enemyHP = newEnemyHP,
            correctAnswers = newCorrectAnswers,
            isVictory = isVictory,
            isDefeat = isDefeat
        )
        
        // Si terminó la batalla, completar lección
        if (isVictory && !isDefeat) {
            completeLesson(lessonId, newCorrectAnswers, battle.totalQuestions)
        }
    }
    
    /**
     * Finaliza la batalla actual
     */
    fun endBattle() {
        _currentBattle.value = null
    }
    
    // ========== STREAK SYSTEM ==========
    
    /**
     * Verifica y actualiza la racha de estudio
     */
    private fun checkAndUpdateStreak() {
        viewModelScope.launch {
            updateProgress { current ->
                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                
                val lastStudy = Calendar.getInstance().apply {
                    timeInMillis = current.lastStudyDate
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                
                val daysDiff = ((today - lastStudy) / (1000 * 60 * 60 * 24)).toInt()
                
                val newStreak = when {
                    daysDiff == 0 -> current.streak // Mismo día
                    daysDiff == 1 -> current.streak + 1 // Día consecutivo
                    else -> 1 // Racha rota, reiniciar
                }
                
                current.copy(
                    streak = newStreak,
                    lastStudyDate = System.currentTimeMillis()
                )
            }
        }
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
