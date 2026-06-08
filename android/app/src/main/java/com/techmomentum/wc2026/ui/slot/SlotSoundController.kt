package com.techmomentum.wc2026.ui.slot

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.techmomentum.wc2026.R
private const val TAG = "SlotSound"
private const val PREFS_NAME = "world_cup_2026_prefs"
private const val KEY_SOUND = "sound_enabled"

class SlotSoundController(
    context: Context,
    private val isSoundEnabled: () -> Boolean,
) {
    private val appContext = context.applicationContext

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(6)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()

    private val readySounds = mutableSetOf<Int>()

    private var spinClickSound = 0
    private var spinLoopSound = 0
    private var columnLandSound = 0
    private var winSound = 0

    private var spinLoopStreamId: Int? = null

    init {
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                readySounds += sampleId
                Log.d(TAG, "Loaded sound sampleId=$sampleId name=${soundName(sampleId)}")
            } else {
                Log.e(TAG, "FAILED to load sound sampleId=$sampleId name=${soundName(sampleId)} status=$status")
            }
        }

        spinClickSound = soundPool.load(appContext, R.raw.slot_spin_click, 1)
        spinLoopSound = soundPool.load(appContext, R.raw.slot_spin_loop, 1)
        columnLandSound = soundPool.load(appContext, R.raw.slot_column_land, 1)
        winSound = soundPool.load(appContext, R.raw.slot_win, 1)
    }

    private fun soundName(sampleId: Int): String = when (sampleId) {
        spinClickSound -> "slot_spin_click"
        spinLoopSound -> "slot_spin_loop"
        columnLandSound -> "slot_column_land"
        winSound -> "slot_win"
        else -> "unknown"
    }

    private fun isReady(soundId: Int, label: String): Boolean {
        if (soundId == 0) {
            Log.e(TAG, "Invalid soundId=0 for $label")
            return false
        }
        val ready = readySounds.contains(soundId)
        if (!ready) {
            Log.w(TAG, "Sound not ready or failed: $label soundId=$soundId")
        }
        return ready
    }

    private fun playSound(soundId: Int, label: String, volume: Float, priority: Int, loop: Int, rate: Float) {
        if (!isSoundEnabled()) return
        if (!isReady(soundId, label)) return

        val streamId = soundPool.play(soundId, volume, volume, priority, loop, rate)
        if (streamId == 0) {
            Log.e(TAG, "soundPool.play failed for $label soundId=$soundId")
        }
    }

    fun playSpinClick() {
        playSound(spinClickSound, "slot_spin_click", volume = 0.65f, priority = 1, loop = 0, rate = 1f)
    }

    fun startSpinLoop() {
        if (!isSoundEnabled()) return
        if (spinLoopStreamId != null) return
        if (!isReady(spinLoopSound, "slot_spin_loop")) return

        val streamId = soundPool.play(spinLoopSound, 0.28f, 0.28f, 1, -1, 1f)
        if (streamId == 0) {
            Log.e(TAG, "soundPool.play failed for slot_spin_loop soundId=$spinLoopSound")
            return
        }
        spinLoopStreamId = streamId
    }

    fun stopSpinLoop() {
        spinLoopStreamId?.let { streamId ->
            soundPool.stop(streamId)
        }
        spinLoopStreamId = null
    }

    fun playColumnLand(columnIndex: Int) {
        val pitch = when (columnIndex) {
            0 -> 0.95f
            1 -> 1.0f
            else -> 1.06f
        }
        playSound(columnLandSound, "slot_column_land", volume = 1f, priority = 2, loop = 0, rate = pitch)
    }

    fun playWin() {
        playSound(winSound, "slot_win", volume = 1f, priority = 3, loop = 0, rate = 1f)
    }

    fun release() {
        stopSpinLoop()
        soundPool.release()
    }
}

@Composable
fun rememberSlotSoundController(): SlotSoundController {
    val context = LocalContext.current
    val prefs = remember {
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    val controller = remember {
        SlotSoundController(context) {
            prefs.getBoolean(KEY_SOUND, true)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            controller.release()
        }
    }

    return controller
}
