package com.example.exempl

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.*

class VoiceGuidanceManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isGuidanceEnabled = true
    private var lastSpeechTime = 0L
    private val speechInterval = 4000L // 4 секунды между голосовыми подсказками

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("ru", "RU"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback на английский
                tts?.setLanguage(Locale.US)
            }
            // Настройка скорости речи
            tts?.setSpeechRate(0.9f)
            tts?.setPitch(1.0f)
        }
    }

    fun speakGuidance(guidance: String) {
        if (!isGuidanceEnabled || guidance.isEmpty()) return

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastSpeechTime < speechInterval) return

        lastSpeechTime = currentTime
        tts?.speak(guidance, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun toggleGuidance() {
        isGuidanceEnabled = !isGuidanceEnabled
        if (!isGuidanceEnabled) {
            tts?.stop()
        }
    }

    fun setEnabled(enabled: Boolean) {
        isGuidanceEnabled = enabled
        if (!enabled) {
            tts?.stop()
        }
    }

    fun shutdown() {
        tts?.shutdown()
    }
}
