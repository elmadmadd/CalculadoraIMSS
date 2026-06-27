package com.example.consucalcu.manager

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.consucalcu.R

class SoundManager(context: Context) {
    private val soundPool: SoundPool
    private val soundMap = mutableMapOf<String, Int>()
    var isSoundEnabled: Boolean = true

    init {
        val attr = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(attr)
            .build()
        try {
            soundMap["pop"] = soundPool.load(context, R.raw.pop, 1)
            soundMap["cash"] = soundPool.load(context, R.raw.cash, 1)
            soundMap["magic"] = soundPool.load(context, R.raw.magic, 1)
            soundMap["wrong"] = soundPool.load(context, R.raw.wrong, 1)
            soundMap["villager"] = soundPool.load(context, R.raw.villager_blip, 1)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun play(name: String) {
        if (!isSoundEnabled) return
        val volume = if (name == "villager") 0.6f else 1f
        val rate = if (name == "villager") (0.9f + Math.random().toFloat() * 0.2f) else 1f
        soundMap[name]?.let { soundPool.play(it, volume, volume, 1, 0, rate) }
    }

    fun release() {
        soundPool.release()
    }
}
