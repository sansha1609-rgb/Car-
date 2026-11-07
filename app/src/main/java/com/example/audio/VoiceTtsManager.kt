package com.example.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class VoiceTtsManager(
    private val context: Context,
    private val onSpeechStarted: () -> Unit = {},
    private val onSpeechFinished: () -> Unit = {}
) {
    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false

    init {
        textToSpeech = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale.US)
                if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                    isInitialized = true
                    textToSpeech?.setSpeechRate(1.05f)
                    textToSpeech?.setPitch(1.0f)
                }
            } else {
                Log.w("VoiceTtsManager", "TTS initialization failed status: $status")
            }
        }

        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                onSpeechStarted()
            }

            override fun onDone(utteranceId: String?) {
                onSpeechFinished()
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                onSpeechFinished()
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                onSpeechFinished()
            }
        })
    }

    fun speak(text: String, isPriority: Boolean = false) {
        if (!isInitialized) return
        onSpeechStarted()
        val queueMode = if (isPriority) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        textToSpeech?.speak(
            text,
            queueMode,
            null,
            "car_tts_${System.currentTimeMillis()}"
        )
    }

    fun stop() {
        textToSpeech?.stop()
        onSpeechFinished()
    }

    fun release() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        isInitialized = false
    }
}
