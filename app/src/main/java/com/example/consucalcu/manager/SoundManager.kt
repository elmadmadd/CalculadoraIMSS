package com.example.consucalcu.manager

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.SystemClock
import com.example.consucalcu.R

class SoundManager(
    context: Context,
    private val hapticManager: HapticManager? = null,
) {
    private val soundPool: SoundPool
    private val soundMap = mutableMapOf<String, Int>()
    private val sampleNames = mutableMapOf<Int, String>()
    private val loadedSamples = mutableSetOf<Int>()
    private val pendingPlayback = mutableMapOf<String, PendingPlayback>()
    private val lastPlayedAt = mutableMapOf<String, Long>()
    private var activePopStreamId: Int? = null
    var isSoundEnabled: Boolean = true

    private data class PendingPlayback(
        val requestedAt: Long,
        val volume: Float,
        val rate: Float,
    )

    init {
        val attr = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(attr)
            .build()
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status != 0) return@setOnLoadCompleteListener
            synchronized(this@SoundManager) {
                loadedSamples += sampleId
                val name = sampleNames[sampleId] ?: return@synchronized
                val pending = pendingPlayback.remove(name) ?: return@synchronized
                val isStillTimely = SystemClock.elapsedRealtime() - pending.requestedAt <= 350L
                if (isStillTimely) {
                    if (isSoundEnabled) {
                        playLoadedSample(name, sampleId, pending.volume, pending.rate)
                    }
                    hapticManager?.playAudioTimeline(name)
                }
            }
        }
        try {
            load(context, "pop", R.raw.pop)
            load(context, "island_expand", R.raw.island_expand)
            load(context, "island_full_expand", R.raw.island_full_expand)
            load(context, "island_collapse", R.raw.island_collapse)
            load(context, "ui_success", R.raw.ui_success)
            load(context, "ui_error", R.raw.ui_error)
            load(context, "pdf_ready", R.raw.pdf_ready)
            load(context, "copy_confirm", R.raw.copy_confirm)
            load(context, "academy_complete", R.raw.academy_complete)
            load(context, "mascot_appear", R.raw.mascot_appear)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun load(context: Context, name: String, resourceId: Int) {
        val sampleId = soundPool.load(context, resourceId, 1)
        soundMap[name] = sampleId
        sampleNames[sampleId] = name
    }

    private fun playLoadedSample(name: String, sampleId: Int, volume: Float, rate: Float) {
        activePopStreamId?.let(soundPool::stop)
        activePopStreamId = null
        val streamId = soundPool.play(sampleId, volume, volume, 1, 0, rate)
        if (name == "pop" && streamId != 0) activePopStreamId = streamId
    }

    @Synchronized
    fun play(name: String) {
        // History actions used the legacy "delete" cue, but there is no
        // dedicated sample. Reuse the quiet navigation pop instead of failing
        // silently or adding another competing sound.
        val resolvedName = if (name == "delete") "pop" else name
        val now = SystemClock.elapsedRealtime()
        if (now - (lastPlayedAt[resolvedName] ?: 0L) < 90L) return
        lastPlayedAt[resolvedName] = now

        val volume = when (resolvedName) {
            "mascot_appear" -> 0.18f
            // 30 % del nivel anterior (0.24): acompaña el toque sin competir con resultados.
            "pop" -> 0.072f
            "copy_confirm" -> 0.24f
            "ui_error" -> 0.28f
            "island_expand", "island_collapse" -> 0.38f
            "island_full_expand" -> 0.42f
            "pdf_ready" -> 0.32f
            "ui_success" -> 0.34f
            "academy_complete" -> 0.42f
            else -> 0.3f
        }
        val rate = 1f
        if (isSoundEnabled) {
            val sampleId = soundMap[resolvedName]
            if (sampleId != null && sampleId in loadedSamples) {
                playLoadedSample(resolvedName, sampleId, volume, rate)
                hapticManager?.playAudioTimeline(resolvedName)
            } else if (sampleId != null) {
                pendingPlayback[resolvedName] = PendingPlayback(now, volume, rate)
            }
        } else {
            hapticManager?.playAudioTimeline(resolvedName)
        }
    }

    fun release() {
        soundPool.release()
    }
}
