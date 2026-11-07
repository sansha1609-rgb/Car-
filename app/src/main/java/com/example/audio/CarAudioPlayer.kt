package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AudioTrack(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val uri: String? = null,
    val genre: String = "Driving Beats"
)

data class FmTransmitterState(
    val isEnabled: Boolean = true,
    val frequencyMhz: Float = 88.5f,
    val isStereo: Boolean = true,
    val powerBoostDb: Int = 3
)

data class BluetoothAudioState(
    val isConnected: Boolean = true,
    val deviceName: String = "Maruti Suzuki Celerio SmartPlay",
    val codec: String = "aptX HD / SBC",
    val batteryPercent: Int = 92
)

data class PlaybackState(
    val currentTrack: AudioTrack,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0,
    val durationMs: Long = 210000,
    val isShuffle: Boolean = false,
    val isRepeat: Boolean = false,
    val volume: Float = 0.85f,
    val isDucked: Boolean = false
)

class CarAudioPlayer(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null
    private var audioFocusRequest: AudioFocusRequest? = null

    // Default curated driving tracks for offline head unit
    val defaultPlaylist = listOf(
        AudioTrack("1", "Highway Cruising Neon", "Synthwave Drive", "Night City Beats", 215000, genre = "Electronic"),
        AudioTrack("2", "Celerio K10 Sunset", "Acoustic Horizon", "Roadtrip Vibes", 184000, genre = "Acoustic"),
        AudioTrack("3", "Grand Trunk Express", "Sublime Echoes", "Monsoon Chill", 242000, genre = "Ambient"),
        AudioTrack("4", "Mountain Pass Rhythm", "Velocity Duo", "Turbo Drive", 198000, genre = "Synthwave"),
        AudioTrack("5", "Midnight Delhi Ring", "Bassline Drivers", "Night Drive", 226000, genre = "Lo-Fi Beats")
    )

    private var currentPlaylist: List<AudioTrack> = defaultPlaylist
    private var currentIndex = 0

    private val _playbackState = MutableStateFlow(
        PlaybackState(currentTrack = defaultPlaylist[0], durationMs = defaultPlaylist[0].durationMs)
    )
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _fmState = MutableStateFlow(FmTransmitterState())
    val fmState: StateFlow<FmTransmitterState> = _fmState.asStateFlow()

    private val _bluetoothState = MutableStateFlow(BluetoothAudioState())
    val bluetoothState: StateFlow<BluetoothAudioState> = _bluetoothState.asStateFlow()

    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                duckVolume(true)
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                duckVolume(false)
                if (_playbackState.value.isPlaying) {
                    // Resume volume smoothly
                }
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                pause()
            }
        }
    }

    init {
        requestAudioFocus()
    }

    private fun requestAudioFocus(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val playbackAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(audioFocusChangeListener)
                .build()
            audioFocusRequest = focusRequest
            audioManager.requestAudioFocus(focusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    fun play() {
        requestAudioFocus()
        _playbackState.value = _playbackState.value.copy(isPlaying = true)
        startProgressSimulation()
    }

    fun pause() {
        _playbackState.value = _playbackState.value.copy(isPlaying = false)
        progressJob?.cancel()
    }

    fun togglePlayPause() {
        if (_playbackState.value.isPlaying) {
            pause()
        } else {
            play()
        }
    }

    fun nextTrack() {
        if (currentPlaylist.isEmpty()) return
        currentIndex = (currentIndex + 1) % currentPlaylist.size
        loadTrack(currentPlaylist[currentIndex])
    }

    fun previousTrack() {
        if (currentPlaylist.isEmpty()) return
        currentIndex = if (currentIndex - 1 < 0) currentPlaylist.size - 1 else currentIndex - 1
        loadTrack(currentPlaylist[currentIndex])
    }

    fun selectTrack(track: AudioTrack) {
        val index = currentPlaylist.indexOfFirst { it.id == track.id }
        if (index != -1) {
            currentIndex = index
            loadTrack(track)
        }
    }

    private fun loadTrack(track: AudioTrack) {
        _playbackState.value = _playbackState.value.copy(
            currentTrack = track,
            currentPositionMs = 0,
            durationMs = track.durationMs,
            isPlaying = true
        )
        startProgressSimulation()
    }

    fun seekTo(positionMs: Long) {
        val safePos = positionMs.coerceIn(0L, _playbackState.value.durationMs)
        _playbackState.value = _playbackState.value.copy(currentPositionMs = safePos)
    }

    fun toggleShuffle() {
        _playbackState.value = _playbackState.value.copy(isShuffle = !_playbackState.value.isShuffle)
    }

    fun toggleRepeat() {
        _playbackState.value = _playbackState.value.copy(isRepeat = !_playbackState.value.isRepeat)
    }

    /**
     * Duck volume during navigation turn-by-turn prompts or voice assistant.
     * Smoothly scales volume down to 25% for high speech intelligibility in noisy vehicle cabins.
     */
    fun duckVolume(duck: Boolean) {
        _playbackState.value = _playbackState.value.copy(
            isDucked = duck,
            volume = if (duck) 0.22f else 0.85f
        )
    }

    fun setFmFrequency(freq: Float) {
        val clamped = (freq * 10f).toInt() / 10f
        _fmState.value = _fmState.value.copy(frequencyMhz = clamped.coerceIn(87.5f, 108.0f))
    }

    fun toggleFmTransmitter() {
        _fmState.value = _fmState.value.copy(isEnabled = !_fmState.value.isEnabled)
    }

    fun toggleBluetooth() {
        _bluetoothState.value = _bluetoothState.value.copy(
            isConnected = !_bluetoothState.value.isConnected
        )
    }

    private fun startProgressSimulation() {
        progressJob?.cancel()
        progressJob = scope.launch(Dispatchers.Default) {
            while (_playbackState.value.isPlaying) {
                delay(1000L)
                val current = _playbackState.value.currentPositionMs + 1000L
                val max = _playbackState.value.durationMs
                if (current >= max) {
                    if (_playbackState.value.isRepeat) {
                        _playbackState.value = _playbackState.value.copy(currentPositionMs = 0)
                    } else {
                        nextTrack()
                    }
                } else {
                    _playbackState.value = _playbackState.value.copy(currentPositionMs = current)
                }
            }
        }
    }

    fun release() {
        progressJob?.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(audioFocusChangeListener)
        }
    }
}
