package co.kr.imokapp.headream.audio

actual object AudioPlaybackManager {
    actual fun initialize() {
        println("🎵 AudioPlaybackManager - JS 초기화")
    }
    
    actual fun play(url: String) {
        println("🎵 AudioPlaybackManager - JS 재생: $url")
        // TODO: HTML5 Audio API 구현
    }
    
    actual fun pause() {
        println("⏸️ AudioPlaybackManager - JS 일시정지")
    }
    
    actual fun resume() {
        println("▶️ AudioPlaybackManager - JS 재생 재개")
        // TODO: HTML5 Audio API 구현
    }
    
    actual fun stop() {
        println("⏹️ AudioPlaybackManager - JS 정지")
    }
    
    actual fun isPlaying(): Boolean {
        return false
    }
    
    actual fun release() {
        println("🗑️ AudioPlaybackManager - JS 리소스 해제")
    }
    
    actual fun getCurrentPosition(): Long {
        return 0L // TODO: HTML5 Audio API 구현
    }
    
    actual fun getDuration(): Long {
        return 0L // TODO: HTML5 Audio API 구현
    }
    
    actual fun seekTo(position: Long) {
        println("🎯 AudioPlaybackManager - JS 위치 이동: ${position}ms")
        // TODO: HTML5 Audio API 구현
    }
}
