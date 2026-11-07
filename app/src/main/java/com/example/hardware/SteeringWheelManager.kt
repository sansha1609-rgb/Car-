package com.example.hardware

import android.view.KeyEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

enum class SteeringWheelAction(val label: String, val defaultKeyCode: Int) {
    PLAY_PAUSE("Play / Pause Media", KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE),
    NEXT_TRACK("Next Track / Channel", KeyEvent.KEYCODE_MEDIA_NEXT),
    PREV_TRACK("Previous Track / Channel", KeyEvent.KEYCODE_MEDIA_PREVIOUS),
    VOLUME_UP("Volume Up", KeyEvent.KEYCODE_VOLUME_UP),
    VOLUME_DOWN("Volume Down", KeyEvent.KEYCODE_VOLUME_DOWN),
    MUTE("Mute Audio", KeyEvent.KEYCODE_VOLUME_MUTE),
    VOICE_COMMAND("Activate Voice Command", KeyEvent.KEYCODE_VOICE_ASSIST),
    TOGGLE_HUD("Toggle HUD Mode", KeyEvent.KEYCODE_NAVIGATE_NEXT),
    EMERGENCY_SOS("Emergency SOS Assistance", KeyEvent.KEYCODE_CALL)
}

data class SteeringWheelEvent(
    val action: SteeringWheelAction,
    val keyCode: Int,
    val timestamp: Long = System.currentTimeMillis()
)

class SteeringWheelManager {
    // User configurable mapping: KeyCode -> SteeringWheelAction
    private val keyMapping = mutableMapOf<Int, SteeringWheelAction>()

    private val _lastEvent = MutableSharedFlow<SteeringWheelEvent>(extraBufferCapacity = 8)
    val lastEvent: SharedFlow<SteeringWheelEvent> = _lastEvent.asSharedFlow()

    private val _isLearningMode = MutableStateFlow<SteeringWheelAction?>(null)
    val isLearningMode: StateFlow<SteeringWheelAction?> = _isLearningMode.asStateFlow()

    init {
        // Initialize default key codes common on car head units
        SteeringWheelAction.values().forEach { action ->
            keyMapping[action.defaultKeyCode] = action
        }
        // Additional common Android automotive head unit key codes
        keyMapping[KeyEvent.KEYCODE_HEADSETHOOK] = SteeringWheelAction.PLAY_PAUSE
        keyMapping[KeyEvent.KEYCODE_MEDIA_PLAY] = SteeringWheelAction.PLAY_PAUSE
        keyMapping[KeyEvent.KEYCODE_MEDIA_PAUSE] = SteeringWheelAction.PLAY_PAUSE
        keyMapping[KeyEvent.KEYCODE_SEARCH] = SteeringWheelAction.VOICE_COMMAND
    }

    /**
     * Intercepts Android key event from head unit hardware buttons or steering wheel CAN-bus adapter.
     * Returns true if consumed.
     */
    fun onKeyEvent(keyCode: Int, event: KeyEvent): Boolean {
        if (event.action != KeyEvent.ACTION_DOWN) return false

        // Check if user is currently mapping a button in settings
        val learningAction = _isLearningMode.value
        if (learningAction != null) {
            keyMapping[keyCode] = learningAction
            _isLearningMode.value = null
            _lastEvent.tryEmit(SteeringWheelEvent(learningAction, keyCode))
            return true
        }

        val mappedAction = keyMapping[keyCode]
        return if (mappedAction != null) {
            _lastEvent.tryEmit(SteeringWheelEvent(mappedAction, keyCode))
            true
        } else {
            false
        }
    }

    fun startLearning(action: SteeringWheelAction) {
        _isLearningMode.value = action
    }

    fun cancelLearning() {
        _isLearningMode.value = null
    }

    fun getMappedKeyName(action: SteeringWheelAction): String {
        val entry = keyMapping.entries.firstOrNull { it.value == action }
        return if (entry != null) {
            KeyEvent.keyCodeToString(entry.key).replace("KEYCODE_", "")
        } else {
            "Not Assigned"
        }
    }

    fun simulateAction(action: SteeringWheelAction) {
        _lastEvent.tryEmit(SteeringWheelEvent(action, action.defaultKeyCode))
    }
}
