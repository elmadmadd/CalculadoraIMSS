package com.example.consucalcu.academy

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class AcademyProgressRulesTest {

    private val lesson = AcademyContent.getLessonById("lesson_email_intro")
        ?: error("La lección base de Academia debe existir")

    @Test
    fun repeatingLessonImprovesStarsWithoutDuplicatingXp() {
        val firstDay = timestamp(2026, Calendar.AUGUST, 18)
        val firstCompletion = AcademyProgressRules.completeLesson(
            current = AcademyProgress(),
            lesson = lesson,
            stars = 1,
            completedAtMillis = firstDay
        )
        val repeated = AcademyProgressRules.completeLesson(
            current = firstCompletion,
            lesson = lesson,
            stars = 3,
            completedAtMillis = firstDay
        )

        assertEquals(lesson.xpReward, repeated.totalXP)
        assertEquals(3, repeated.lessonStars[lesson.id])
        assertEquals(1, repeated.streak)
    }

    @Test
    fun streakOnlyAdvancesOncePerCalendarDay() {
        val dayOne = timestamp(2026, Calendar.AUGUST, 18)
        val sameDay = timestamp(2026, Calendar.AUGUST, 18, hour = 21)
        val nextDay = timestamp(2026, Calendar.AUGUST, 19)

        val started = AcademyProgressRules.recordStudyActivity(AcademyProgress(), dayOne)
        val unchanged = AcademyProgressRules.recordStudyActivity(started, sameDay)
        val advanced = AcademyProgressRules.recordStudyActivity(unchanged, nextDay)

        assertEquals(1, unchanged.streak)
        assertEquals(2, advanced.streak)
    }

    @Test
    fun exhaustedBattleIsDefeatWhenPassingScoreWasNotReached() {
        var battle = BattleState(totalQuestions = 3)
        repeat(3) { battle = BattleRules.answer(battle, isCorrect = false) }

        assertFalse(battle.isVictory)
        assertTrue(battle.isDefeat)
    }

    @Test
    fun exhaustedBattleIsVictoryWhenPassingScoreWasReached() {
        var battle = BattleState(totalQuestions = 3)
        battle = BattleRules.answer(battle, isCorrect = true)
        battle = BattleRules.answer(battle, isCorrect = true)
        battle = BattleRules.answer(battle, isCorrect = false)

        assertTrue(battle.isVictory)
        assertFalse(battle.isDefeat)
    }

    @Test
    fun completedBattleIgnoresAdditionalAnswers() {
        val completed = BattleState(
            currentQuestionIndex = 2,
            correctAnswers = 2,
            totalQuestions = 2,
            isVictory = true
        )

        assertEquals(completed, BattleRules.answer(completed, isCorrect = false))
    }

    private fun timestamp(
        year: Int,
        month: Int,
        day: Int,
        hour: Int = 12
    ): Long = Calendar.getInstance().apply {
        clear()
        set(year, month, day, hour, 0, 0)
    }.timeInMillis
}
