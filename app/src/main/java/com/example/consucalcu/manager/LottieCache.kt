package com.example.consucalcu.manager

import android.content.Context
import androidx.annotation.RawRes
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieCompositionFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

object LottieCache {
    private val cache = ConcurrentHashMap<Int, LottieComposition>()

    suspend fun preload(context: Context, @RawRes resIds: List<Int>) {
        val appContext = context.applicationContext
        withContext(Dispatchers.IO) {
            resIds.forEach { id ->
                if (!cache.containsKey(id)) {
                    val result = LottieCompositionFactory.fromRawResSync(appContext, id)
                    result.value?.let { composition ->
                        cache[id] = composition
                    }
                }
            }
        }
    }

    fun get(id: Int): LottieComposition? = cache[id]
}
