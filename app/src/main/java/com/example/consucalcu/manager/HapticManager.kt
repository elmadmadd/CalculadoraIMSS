package com.example.consucalcu.manager

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class HapticManager(context: Context) {
    var isHapticEnabled: Boolean = true

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private data class AudioCue(
        val atMs: Long,
        val durationMs: Long,
        val amplitude: Int,
    )

    /**
     * Marcas extraídas de la envolvente RMS de cada WAV optimizado (ventanas de 10 ms).
     * La vibración arranca junto con SoundPool y cada pulso sigue un ataque real del audio.
     */
    private val audioCueSheets = mapOf(
        "island_expand" to listOf(
            AudioCue(30, 18, 120),
        ),
        "island_full_expand" to listOf(
            AudioCue(440, 14, 95),
            AudioCue(570, 22, 205),
            AudioCue(650, 18, 145),
        ),
        "island_collapse" to listOf(
            AudioCue(100, 16, 150),
            AudioCue(200, 18, 190),
            AudioCue(380, 16, 125),
        ),
        "ui_success" to listOf(
            AudioCue(60, 18, 145),
            AudioCue(170, 16, 105),
            AudioCue(290, 22, 180),
        ),
        "ui_error" to listOf(
            AudioCue(30, 24, 175),
            AudioCue(110, 22, 145),
            AudioCue(190, 20, 105),
        ),
        "pdf_ready" to listOf(
            AudioCue(30, 14, 85),
            AudioCue(130, 20, 175),
            AudioCue(260, 18, 130),
            AudioCue(490, 14, 75),
        ),
        "copy_confirm" to listOf(
            AudioCue(20, 16, 155),
            AudioCue(180, 14, 95),
        ),
        "academy_complete" to listOf(
            AudioCue(20, 24, 210),
            AudioCue(300, 18, 110),
        ),
    )

    fun playAudioTimeline(soundName: String) {
        if (!isHapticEnabled) return
        val activeVibrator = vibrator?.takeIf { it.hasVibrator() } ?: return
        val cues = audioCueSheets[soundName] ?: return

        val timings = mutableListOf<Long>()
        val amplitudes = mutableListOf<Int>()
        var cursorMs = 0L
        cues.forEach { cue ->
            val gapMs = (cue.atMs - cursorMs).coerceAtLeast(0L)
            timings += gapMs
            amplitudes += 0
            timings += cue.durationMs
            amplitudes += cue.amplitude.coerceIn(1, 255)
            cursorMs = cue.atMs + cue.durationMs
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val outputAmplitudes = if (activeVibrator.hasAmplitudeControl()) {
                amplitudes.toIntArray()
            } else {
                amplitudes.map { if (it == 0) 0 else VibrationEffect.DEFAULT_AMPLITUDE }.toIntArray()
            }
            activeVibrator.vibrate(
                VibrationEffect.createWaveform(timings.toLongArray(), outputAmplitudes, -1)
            )
        } else {
            @Suppress("DEPRECATION")
            activeVibrator.vibrate(timings.toLongArray(), -1)
        }
    }

    /**
     * Light haptic - Para hover, focus, interacciones sutiles
     */
    fun light() {
        if (!isHapticEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(12, 70))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(10)
        }
    }

    /**
     * Medium haptic - Para clicks, toggles, acciones estándar
     */
    fun medium() {
        if (!isHapticEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(25, 120))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(20)
        }
    }

    /**
     * Heavy haptic - Para confirmaciones importantes, errores
     */
    fun heavy() {
        if (!isHapticEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(45, 180))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(40)
        }
    }

    /**
     * Success haptic - Patrón de doble pulse para acciones exitosas
     */
    fun success() {
        if (!isHapticEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pattern = longArrayOf(0, 20, 50, 20)
            val amplitudes = intArrayOf(0, 100, 0, 150)
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
        }
    }

    /**
     * Error haptic - Patrón de triple pulse para errores
     */
    fun error() {
        if (!isHapticEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pattern = longArrayOf(0, 15, 30, 15, 30, 20)
            val amplitudes = intArrayOf(0, 80, 0, 80, 0, 120)
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
        }
    }

    /**
     * Tick haptic - Para sliders, incrementos numéricos
     */
    fun tick() {
        if (!isHapticEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(5, 40))
        }
    }
}
