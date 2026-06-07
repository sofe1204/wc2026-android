package com.techmomentum.wc2026.data.slot

import android.media.AudioManager
import android.media.ToneGenerator
import com.techmomentum.wc2026.data.local.AppPreferences
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Lightweight slot sound effects using the system [ToneGenerator] — no audio assets required.
 * All playback respects the user's "Sound effects" preference.
 */
@Singleton
class SlotSoundPlayer @Inject constructor(
    private val preferences: AppPreferences,
) {
    @Volatile
    private var generator: ToneGenerator? = null

    private fun generator(): ToneGenerator? {
        if (generator == null) {
            generator = runCatching { ToneGenerator(AudioManager.STREAM_MUSIC, VOLUME) }.getOrNull()
        }
        return generator
    }

    /** Short rising blip when a spin begins. */
    fun playSpin() = tone(ToneGenerator.TONE_PROP_BEEP2, durationMs = 160)

    /** Click as each reel locks into place. */
    fun playReelStop() = tone(ToneGenerator.TONE_PROP_ACK, durationMs = 90)

    /** Celebratory rising arpeggio on a win. */
    suspend fun playWin() {
        if (!preferences.soundEnabled) return
        val g = generator() ?: return
        val arpeggio = listOf(
            ToneGenerator.TONE_PROP_BEEP,
            ToneGenerator.TONE_PROP_BEEP2,
            ToneGenerator.TONE_PROP_PROMPT,
        )
        arpeggio.forEach { t ->
            g.startTone(t, 150)
            delay(170)
        }
    }

    private fun tone(type: Int, durationMs: Int) {
        if (!preferences.soundEnabled) return
        generator()?.startTone(type, durationMs)
    }

    companion object {
        private const val VOLUME = 80
    }
}
