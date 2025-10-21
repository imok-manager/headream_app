package co.kr.imok.headream.app.audio

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

actual class AudioPlayer {
    private var context: Context? = null
    private var exoPlayer: ExoPlayer? = null
    
    private val _isPlaying = MutableStateFlow(false)
    actual val isPlaying: StateFlow<Boolean> = _isPlaying
    
    private val _currentPosition = MutableStateFlow(0L)
    actual val currentPosition: StateFlow<Long> = _currentPosition
    
    private val _duration = MutableStateFlow(0L)
    actual val duration: StateFlow<Long> = _duration
    
    fun initialize(context: Context) {
        this.context = context
        initializePlayer()
    }
    
    private fun initializePlayer() {
        val ctx = context ?: return
        exoPlayer = ExoPlayer.Builder(ctx).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    _isPlaying.value = playbackState == Player.STATE_READY && playWhenReady
                }
                
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                }
            })
        }
    }
    
    actual fun play(url: String) {
        println("🎵 Android ExoPlayer - 재생 시작: $url")
        try {
            val mediaItem = MediaItem.fromUri(url)
            exoPlayer?.apply {
                setMediaItem(mediaItem)
                prepare()
                playWhenReady = true
            }
        } catch (e: Exception) {
            println("❌ Android ExoPlayer - 재생 실패: ${e.message}")
        }
    }
    
    actual fun pause() {
        println("⏸️ Android ExoPlayer - 일시정지")
        exoPlayer?.playWhenReady = false
    }
    
    actual fun stop() {
        println("⏹️ Android ExoPlayer - 정지")
        exoPlayer?.stop()
    }
    
    actual fun release() {
        println("🗑️ Android ExoPlayer - 리소스 해제")
        exoPlayer?.release()
        exoPlayer = null
    }
    
    actual fun seekTo(position: Long) {
        exoPlayer?.seekTo(position)
    }
}
