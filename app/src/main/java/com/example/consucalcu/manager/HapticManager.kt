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
