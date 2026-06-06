package com.example.nebulastrike.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.sin

class SoundManager(context: Context) {
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val prefs = context.getSharedPreferences("nebula_strike_settings", Context.MODE_PRIVATE)

    var isSoundEnabled: Boolean
        get() = prefs.getBoolean("sound_enabled", true)
        set(value) = prefs.edit().putBoolean("sound_enabled", value).apply()

    fun playLaser() {
        if (!isSoundEnabled) return
        executor.execute {
            generateSweep(1200f, 400f, 120, Waveform.SINE)
        }
    }

    fun playExplosion() {
        if (!isSoundEnabled) return
        executor.execute {
            generateExplosionNoise(250)
        }
    }

    fun playPlayerHit() {
        if (!isSoundEnabled) return
        executor.execute {
            generateSweep(300f, 100f, 180, Waveform.SAWTOOTH)
        }
    }

    fun playPowerUp() {
        if (!isSoundEnabled) return
        executor.execute {
            generateArpeggio(listOf(440f, 554f, 659f, 880f), 80)
        }
    }

    fun playGameOver() {
        if (!isSoundEnabled) return
        executor.execute {
            generateSweep(600f, 150f, 600, Waveform.TRIANGLE)
        }
    }

    private enum class Waveform {
        SINE, SQUARE, SAWTOOTH, TRIANGLE
    }

    private fun generateSweep(startFreq: Float, endFreq: Float, durationMs: Int, waveform: Waveform) {
        val sampleRate = 22050
        val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
        val buffer = ShortArray(numSamples)
        var phase = 0.0

        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val currentFreq = startFreq + (endFreq - startFreq) * progress
            val phaseIncrement = (2.0 * Math.PI * currentFreq) / sampleRate
            phase += phaseIncrement

            // Waveform math
            val sampleVal = when (waveform) {
                Waveform.SINE -> sin(phase)
                Waveform.SQUARE -> if (sin(phase) >= 0) 1.0 else -1.0
                Waveform.SAWTOOTH -> {
                    val p = phase % (2.0 * Math.PI)
                    (p / Math.PI) - 1.0
                }
                Waveform.TRIANGLE -> {
                    val p = phase % (2.0 * Math.PI)
                    val norm = p / (2.0 * Math.PI)
                    if (norm < 0.25) {
                        norm * 4.0
                    } else if (norm < 0.75) {
                        2.0 - (norm * 4.0)
                    } else {
                        (norm * 4.0) - 4.0
                    }
                }
            }

            // Envelope: Fade out at the end
            val fadeOut = if (progress > 0.8f) (1.0f - progress) / 0.2f else 1.0f
            buffer[i] = (sampleVal * 32767.0 * 0.4 * fadeOut).toInt().coerceIn(-32768, 32767).toShort()
        }

        playBuffer(buffer, sampleRate)
    }

    private fun generateExplosionNoise(durationMs: Int) {
        val sampleRate = 22050
        val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
        val buffer = ShortArray(numSamples)
        var lastRandom = 0.0

        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            
            // Generate low-frequency filtered white noise
            val whiteNoise = Math.random() * 2.0 - 1.0
            // Low pass filter
            val filterAlpha = 0.15f + 0.85f * (1.0f - progress) // filter gets lower over time
            val filteredNoise = lastRandom + filterAlpha * (whiteNoise - lastRandom)
            lastRandom = filteredNoise

            // Amplitude envelope
            val amplitude = if (progress < 0.1f) {
                progress / 0.1f // attack
            } else {
                (1.0f - progress) // decay
            }

            buffer[i] = (filteredNoise * 32767.0 * 0.5 * amplitude).toInt().coerceIn(-32768, 32767).toShort()
        }

        playBuffer(buffer, sampleRate)
    }

    private fun generateArpeggio(frequencies: List<Float>, noteDurationMs: Int) {
        val sampleRate = 22050
        val totalSamples = (sampleRate * (noteDurationMs * frequencies.size / 1000f)).toInt()
        val buffer = ShortArray(totalSamples)

        val samplesPerNote = totalSamples / frequencies.size
        for (noteIdx in frequencies.indices) {
            val freq = frequencies[noteIdx]
            var phase = 0.0
            for (i in 0 until samplesPerNote) {
                val idx = noteIdx * samplesPerNote + i
                if (idx >= totalSamples) break

                val progress = i.toFloat() / samplesPerNote
                val phaseIncrement = (2.0 * Math.PI * freq) / sampleRate
                phase += phaseIncrement

                val sampleVal = sin(phase) // Sine wave
                val amplitude = if (progress > 0.8f) (1.0f - progress) / 0.2f else 0.4f
                buffer[idx] = (sampleVal * 32767.0 * amplitude).toInt().coerceIn(-32768, 32767).toShort()
            }
        }

        playBuffer(buffer, sampleRate)
    }

    private fun playBuffer(buffer: ShortArray, sampleRate: Int) {
        var audioTrack: AudioTrack? = null
        try {
            val bufferSize = buffer.size * 2 // short is 2 bytes
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            
            // Since it is playing asynchronously in STATIC mode, we should wait until it finishes playing 
            // before releasing the resources.
            val durationMs = (buffer.size * 1000L) / sampleRate
            Thread.sleep(durationMs + 50)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                audioTrack?.stop()
                audioTrack?.release()
            } catch (_: Exception) {}
        }
    }
    
    fun shutdown() {
        executor.shutdown()
    }
}
