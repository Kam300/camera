package com.example.exempl

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.*

class VoiceGuidanceManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isGuidanceEnabled = true
    private var lastGuidanceTime = 0L
    private val guidanceInterval = 3000L // 3 секунды между подсказками

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
        }
    }

    fun speakGuidance(guidance: String) {
        if (!isGuidanceEnabled || guidance.isEmpty()) return

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastGuidanceTime < guidanceInterval) return

        lastGuidanceTime = currentTime
        tts?.speak(guidance, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun toggleGuidance() {
        isGuidanceEnabled = !isGuidanceEnabled
    }

    fun shutdown() {
        tts?.shutdown()
    }
}
