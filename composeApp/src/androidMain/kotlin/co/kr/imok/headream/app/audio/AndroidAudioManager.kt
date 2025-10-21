package co.kr.imok.headream.app.audio

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

class AndroidAudioManager private constructor() {
    private var exoPlayer: ExoPlayer? = null
    private var context: Context? = null
    
    companion object {
        @Volatile
        private var INSTANCE: AndroidAudioManager? = null
        
        fun getInstance(): AndroidAudioManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AndroidAudioManager().also { INSTANCE = it }
            }
        }
    }
    
    fun initialize(context: Context) {
        this.context = context
        initializePlayer()
    }
    
    private fun ensureContext(): Context? {
        return context
    }
    
    private fun initializePlayer() {
        val ctx = ensureContext() ?: return
        
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(ctx).build().apply {
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_READY -> {
                                println("🎵 ExoPlayer - 재생 준비 완료")
                            }
                            Player.STATE_BUFFERING -> {
                                println("🎵 ExoPlayer - 버퍼링 중")
                            }
                            Player.STATE_ENDED -> {
                                println("🎵 ExoPlayer - 재생 완료")
                            }
                        }
                    }
                    
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        println("🎵 ExoPlayer - 재생 상태 변경: $isPlaying")
                    }
                })
            }
        }
    }
    
    fun play(url: String) {
        println("🎵 AndroidAudioManager - 재생 시작: $url")
        
        // ExoPlayer가 초기화되지 않았다면 다시 시도
        if (exoPlayer == null) {
            println("🔄 ExoPlayer 재초기화 시도")
            initializePlayer()
        }
        
        try {
            val mediaItem = MediaItem.fromUri(url)
            exoPlayer?.apply {
                setMediaItem(mediaItem)
                prepare()
                playWhenReady = true
                println("🎵 ExoPlayer - 미디어 아이템 설정 완료")
            } ?: run {
                println("❌ ExoPlayer가 초기화되지 않았습니다")
            }
        } catch (e: Exception) {
            println("❌ AndroidAudioManager - 재생 실패: ${e.message}")
            e.printStackTrace()
        }
    }
    
    fun pause() {
        println("⏸️ AndroidAudioManager - 일시정지")
        exoPlayer?.playWhenReady = false
    }
    
    fun resume() {
        println("▶️ AndroidAudioManager - 재생 재개")
        exoPlayer?.playWhenReady = true
    }
    
    fun stop() {
        println("⏹️ AndroidAudioManager - 정지")
        exoPlayer?.stop()
    }
    
    fun release() {
        println("🗑️ AndroidAudioManager - 리소스 해제")
        exoPlayer?.release()
        exoPlayer = null
    }
    
    fun isPlaying(): Boolean {
        return exoPlayer?.isPlaying == true
    }
    
    fun getCurrentPosition(): Long {
        return exoPlayer?.currentPosition ?: 0L
    }
    
    fun getDuration(): Long {
        return exoPlayer?.duration ?: 0L
    }
    
    fun seekTo(position: Long) {
        println("🎯 AndroidAudioManager - ExoPlayer 위치 이동: ${position}ms")
        exoPlayer?.seekTo(position)
    }
}
