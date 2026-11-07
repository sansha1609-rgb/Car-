package com.example.hardware

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

sealed class VoiceCommandResult {
    data class Success(val action: String, val message: String) : VoiceCommandResult()
    data class Unrecognized(val query: String) : VoiceCommandResult()
}

class VoiceCommandManager(
    private val context: Context,
    private val onCommandRecognized: (VoiceCommandResult) -> Unit
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _lastQuery = MutableStateFlow("")
    val lastQuery: StateFlow<String> = _lastQuery.asStateFlow()

    init {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                    }

                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        _isListening.value = false
                    }

                    override fun onError(error: Int) {
                        _isListening.value = false
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            val text = matches[0]
                            _lastQuery.value = text
                            processCommand(text)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        }
    }

    fun startListening() {
        if (speechRecognizer != null) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toString())
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            speechRecognizer?.startListening(intent)
            _isListening.value = true
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        _isListening.value = false
    }

    fun processCommand(query: String): VoiceCommandResult {
        val q = query.lowercase().trim()
        val result: VoiceCommandResult = when {
            q.contains("play") && !q.contains("pause") -> {
                VoiceCommandResult.Success("PLAY", "Playing music")
            }
            q.contains("pause") || q.contains("stop music") -> {
                VoiceCommandResult.Success("PAUSE", "Music paused")
            }
            q.contains("next") || q.contains("skip") -> {
                VoiceCommandResult.Success("NEXT_TRACK", "Skipping to next track")
            }
            q.contains("previous") || q.contains("back") && q.contains("track") -> {
                VoiceCommandResult.Success("PREV_TRACK", "Playing previous track")
            }
            q.contains("navigate") && (q.contains("home") || q.contains("house")) -> {
                VoiceCommandResult.Success("NAV_HOME", "Starting route to Home Residence")
            }
            q.contains("navigate") && (q.contains("highway") || q.contains("jaipur") || q.contains("nh48")) -> {
                VoiceCommandResult.Success("NAV_HIGHWAY", "Starting route to NH-48 Express Corridor")
            }
            q.contains("fuel") || q.contains("gas") || q.contains("petrol") -> {
                VoiceCommandResult.Success("NAV_FUEL", "Routing to nearest 24/7 Fuel Station")
            }
            q.contains("service") || q.contains("mechanic") || q.contains("repair") -> {
                VoiceCommandResult.Success("NAV_SERVICE", "Routing to Maruti Suzuki Authorized Service Care")
            }
            q.contains("stop nav") || q.contains("cancel nav") || q.contains("exit nav") -> {
                VoiceCommandResult.Success("STOP_NAV", "Navigation guidance cancelled")
            }
            q.contains("hud") || q.contains("heads up") -> {
                VoiceCommandResult.Success("TOGGLE_HUD", "Toggling Heads-Up Display mode")
            }
            q.contains("night") || q.contains("dark") -> {
                VoiceCommandResult.Success("THEME_NIGHT", "Switching to Night Vision cockpit theme")
            }
            q.contains("day") || q.contains("light") -> {
                VoiceCommandResult.Success("THEME_DAY", "Switching to Day mode")
            }
            q.contains("check engine") || q.contains("diagnostic") || q.contains("scan") || q.contains("obd") -> {
                VoiceCommandResult.Success("DIAGNOSTICS", "Opening OBD-II engine diagnostics scanner")
            }
            q.contains("emergency") || q.contains("sos") || q.contains("help") || q.contains("accident") -> {
                VoiceCommandResult.Success("SOS", "Triggering Emergency SOS Roadside Assistance")
            }
            q.contains("fm") || q.contains("radio") || q.contains("transmitter") -> {
                VoiceCommandResult.Success("FM_RADIO", "Opening FM audio transmitter configuration")
            }
            q.contains("mute") -> {
                VoiceCommandResult.Success("MUTE", "Audio muted")
            }
            else -> {
                VoiceCommandResult.Unrecognized(query)
            }
        }
        onCommandRecognized(result)
        return result
    }

    fun release() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}
