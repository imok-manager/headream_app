package co.kr.imokapp.headream.audio

import android.content.Context

actual object AudioPlaybackManager {
    private var audioManager: AndroidAudioManager? = null
    private var isInitialized = false
    
    actual fun initialize() {
        println("🎵 AudioPlaybackManager - Android 초기화")
        // Android에서는 Context가 필요하므로 play() 호출 시 초기화
    }
    
    fun initializeWithContext(context: Context) {
        if (!isInitialized) {
            audioManager = AndroidAudioManager.getInstance().apply {
                initialize(context)
            }
            isInitialized = true
            println("🎵 AudioPlaybackManager - Context와 함께 초기화 완료")
        }
    }
    
    private fun ensureInitialized() {
        if (!isInitialized) {
            println("⚠️ AudioManager가 초기화되지 않았습니다. Context가 필요합니다.")
        }
    }
    
    actual fun play(url: String) {
        println("🎵 AudioPlaybackManager - Android 재생 시작: $url")
        ensureInitialized()
        audioManager?.play(url) ?: run {
            println("❌ AudioManager가 초기화되지 않았습니다")
        }
    }
    
    actual fun pause() {
        println("⏸️ AudioPlaybackManager - Android 일시정지")
        audioManager?.pause()
    }
    
    actual fun resume() {
        println("▶️ AudioPlaybackManager - Android 재생 재개")
        audioManager?.resume()
    }
    
    actual fun stop() {
        println("⏹️ AudioPlaybackManager - Android 정지")
        audioManager?.stop()
    }
    
    actual fun isPlaying(): Boolean {
        return audioManager?.isPlaying() ?: false
    }
    
    actual fun release() {
        println("🗑️ AudioPlaybackManager - Android 리소스 해제")
        audioManager?.release()
        audioManager = null
    }
    
    actual fun getCurrentPosition(): Long {
        return audioManager?.getCurrentPosition() ?: 0L
    }
    
    actual fun getDuration(): Long {
        return audioManager?.getDuration() ?: 0L
    }
    
    actual fun seekTo(position: Long) {
        println("🎯 AudioPlaybackManager - 위치 이동: ${position}ms")
        audioManager?.seekTo(position)
    }
}
