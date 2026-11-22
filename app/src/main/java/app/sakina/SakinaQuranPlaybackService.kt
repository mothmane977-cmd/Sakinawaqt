package app.sakina

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

/**
 * v31: Jetpack Media3 Background Playback Service for Sakina Quran Reader.
 * - Handles lock-screen & Bluetooth controls automatically via MediaSessionService.
 * - Handles AudioFocus (pauses on phone calls) and noisy audio (headphone unplug).
 * - Exposes instant pause hook for SakinaAdhanForegroundService so Adhan never overlaps recitation.
 */
class SakinaQuranPlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
            .build()

        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()

        activePlayer = player

        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val sessionActivityPi = PendingIntent.getActivity(
            this,
            910,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(sessionActivityPi)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0 || player.playbackState == Player.STATE_ENDED) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        activePlayer = null
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        super.onDestroy()
    }

    companion object {
        @Volatile
        internal var activePlayer: ExoPlayer? = null

        fun pauseImmediately() {
            Handler(Looper.getMainLooper()).post {
                try {
                    activePlayer?.let { p ->
                        if (p.isPlaying) p.pause()
                    }
                } catch (_: Exception) {}
            }
        }
    }
}

data class QuranAudioUiState(
    val isConnected: Boolean = false,
    val isPlaying: Boolean = false,
    val currentSurahNum: Int = 0,
    val currentSurahName: String = "",
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val repeatSurah: Boolean = false,
    val sleepTimerMinutes: Int = 0, // 0 = off, 15/30/60 = minutes, -1 = end of surah
    val sleepRemainingSec: Int = 0
)

/**
 * Unified MediaController & StateFlow facade consumed by SakinaQuranScreen (Compose UI)
 * and SakinaAdhanForegroundService (instant Adhan pause).
 */
object SakinaQuranAudioController {

    // Offline Surahs bundled in res/raw in v31: Al-Fatiha (1) + Ad-Duha (93) .. An-Nas (114)
    val OFFLINE_BUNDLED_SURAHS: Set<Int> = setOf(1) + (93..114).toSet()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _uiState = MutableStateFlow(QuranAudioUiState())
    val uiState: StateFlow<QuranAudioUiState> = _uiState.asStateFlow()

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null
    private var tickerJob: Job? = null
    private var sleepEndAtMs: Long = 0L

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _uiState.update { it.copy(isPlaying = isPlaying) }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            val c = controller ?: return
            val dur = c.duration.coerceAtLeast(0L)
            if (playbackState == Player.STATE_ENDED && _uiState.value.sleepTimerMinutes == -1) {
                clearSleepTimer()
            }
            _uiState.update {
                it.copy(
                    isPlaying = c.isPlaying,
                    durationMs = dur,
                    positionMs = c.currentPosition.coerceAtLeast(0L)
                )
            }
        }
    }

    fun resolveSurahUri(context: Context, surahNum: Int): Uri? {
        val padded = surahNum.toString().padStart(3, '0')
        // 1. Check future on-demand downloaded file (v33 ready)
        val localFile = File(File(context.filesDir, "quran_audio/afasy"), "s$padded.ogg")
        if (localFile.exists() && localFile.length() > 4096L) {
            return Uri.fromFile(localFile)
        }
        // 2. Check bundled res/raw/quran_sXXX.ogg (v31 offline)
        val resId = context.resources.getIdentifier("quran_s$padded", "raw", context.packageName)
        if (resId != 0) {
            return Uri.parse("android.resource://${context.packageName}/$resId")
        }
        return null
    }

    fun isSurahAvailableOffline(context: Context, surahNum: Int): Boolean =
        resolveSurahUri(context, surahNum) != null

    fun connect(context: Context) {
        if (controller != null || controllerFuture != null) return
        val appCtx = context.applicationContext
        val token = SessionToken(appCtx, ComponentName(appCtx, SakinaQuranPlaybackService::class.java))
        val future = MediaController.Builder(appCtx, token).buildAsync()
        controllerFuture = future
        future.addListener(
            {
                try {
                    val c = future.get()
                    controller = c
                    c.addListener(playerListener)
                    _uiState.update {
                        it.copy(
                            isConnected = true,
                            isPlaying = c.isPlaying,
                            durationMs = c.duration.coerceAtLeast(0L),
                            positionMs = c.currentPosition.coerceAtLeast(0L)
                        )
                    }
                    startTicker()
                } catch (_: Exception) {
                    controllerFuture = null
                }
            },
            ContextCompat.getMainExecutor(appCtx)
        )
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (true) {
                val c = controller
                if (c != null) {
                    val pos = c.currentPosition.coerceAtLeast(0L)
                    val dur = c.duration.coerceAtLeast(0L)
                    val st = _uiState.value

                    // Sleep timer countdown with smooth 10-second fade-out
                    var remSec = 0
                    if (st.sleepTimerMinutes > 0 && sleepEndAtMs > 0L) {
                        val remMs = sleepEndAtMs - System.currentTimeMillis()
                        if (remMs <= 0L) {
                            c.pause()
                            c.volume = 1.0f
                            clearSleepTimer()
                        } else {
                            remSec = (remMs / 1000L).toInt()
                            if (remMs <= 10_000L) {
                                c.volume = (remMs / 10_000f).coerceIn(0.05f, 1.0f)
                            }
                        }
                    }

                    _uiState.update {
                        it.copy(
                            isPlaying = c.isPlaying,
                            positionMs = pos,
                            durationMs = dur,
                            sleepRemainingSec = remSec
                        )
                    }
                }
                delay(250L)
            }
        }
    }

    fun playSurah(context: Context, surahNum: Int, surahNameAr: String) {
        val uri = resolveSurahUri(context, surahNum) ?: return
        val appCtx = context.applicationContext
        val startBlock: (MediaController) -> Unit = { c ->
            val metadata = MediaMetadata.Builder()
                .setTitle("سورة $surahNameAr")
                .setArtist("مشاري راشد العفاسي · سَكِينة")
                .build()
            val item = MediaItem.Builder()
                .setMediaId("surah_$surahNum")
                .setUri(uri)
                .setMediaMetadata(metadata)
                .build()
            val st = _uiState.value
            c.volume = 1.0f
            c.setMediaItem(item)
            c.repeatMode = if (st.repeatSurah) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
            c.setPlaybackParameters(PlaybackParameters(st.playbackSpeed, 1.0f))
            c.prepare()
            c.play()
            _uiState.update {
                it.copy(
                    currentSurahNum = surahNum,
                    currentSurahName = surahNameAr,
                    isPlaying = true,
                    positionMs = 0L
                )
            }
        }

        val c = controller
        if (c != null) {
            startBlock(c)
        } else {
            connect(appCtx)
            scope.launch {
                repeat(20) {
                    delay(100L)
                    controller?.let { ready ->
                        startBlock(ready)
                        return@launch
                    }
                }
            }
        }
    }

    fun togglePlayPause(context: Context, fallbackSurahNum: Int, fallbackSurahNameAr: String) {
        val c = controller
        val st = _uiState.value
        if (c == null || st.currentSurahNum == 0 || (st.currentSurahNum != fallbackSurahNum && !c.isPlaying && isSurahAvailableOffline(context, fallbackSurahNum))) {
            playSurah(context, fallbackSurahNum, fallbackSurahNameAr)
            return
        }
        if (c.isPlaying) {
            c.pause()
        } else {
            c.volume = 1.0f
            if (c.playbackState == Player.STATE_ENDED) {
                c.seekTo(0L)
            }
            c.play()
        }
    }

    fun seekTo(ms: Long) {
        val c = controller ?: return
        val dur = c.duration.coerceAtLeast(0L)
        val clamped = if (dur > 0L) ms.coerceIn(0L, dur) else ms.coerceAtLeast(0L)
        c.seekTo(clamped)
        _uiState.update { it.copy(positionMs = clamped) }
    }

    fun seekBy(deltaMs: Long) {
        val c = controller ?: return
        seekTo(c.currentPosition + deltaMs)
    }

    fun cycleSpeed() {
        val next = when (_uiState.value.playbackSpeed) {
            0.75f -> 1.0f
            1.0f -> 1.25f
            1.25f -> 1.5f
            else -> 0.75f
        }
        controller?.setPlaybackParameters(PlaybackParameters(next, 1.0f))
        _uiState.update { it.copy(playbackSpeed = next) }
    }

    fun toggleRepeat() {
        val next = !_uiState.value.repeatSurah
        controller?.repeatMode = if (next) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
        _uiState.update { it.copy(repeatSurah = next) }
    }

    fun cycleSleepTimer() {
        val nextMin = when (_uiState.value.sleepTimerMinutes) {
            0 -> 15
            15 -> 30
            30 -> 60
            60 -> -1
            else -> 0
        }
        controller?.volume = 1.0f
        if (nextMin > 0) {
            sleepEndAtMs = System.currentTimeMillis() + nextMin * 60_000L
            _uiState.update { it.copy(sleepTimerMinutes = nextMin, sleepRemainingSec = nextMin * 60) }
        } else {
            sleepEndAtMs = 0L
            _uiState.update { it.copy(sleepTimerMinutes = nextMin, sleepRemainingSec = 0) }
        }
    }

    private fun clearSleepTimer() {
        sleepEndAtMs = 0L
        _uiState.update { it.copy(sleepTimerMinutes = 0, sleepRemainingSec = 0) }
    }

    fun stop() {
        try {
            controller?.stop()
            controller?.clearMediaItems()
        } catch (_: Exception) {}
        clearSleepTimer()
        _uiState.update {
            it.copy(isPlaying = false, currentSurahNum = 0, currentSurahName = "", positionMs = 0L, durationMs = 0L)
        }
    }

    fun pauseForAdhan() {
        SakinaQuranPlaybackService.pauseImmediately()
        Handler(Looper.getMainLooper()).post {
            try {
                controller?.let { if (it.isPlaying) it.pause() }
            } catch (_: Exception) {}
        }
    }
}
