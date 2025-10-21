package co.kr.imok.headream.app.audio

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

actual class AudioPlayer {
    private val _isPlaying = MutableStateFlow(false)
    actual val isPlaying: StateFlow<Boolean> = _isPlaying
    
    private val _currentPosition = MutableStateFlow(0L)
    actual val currentPosition: StateFlow<Long> = _currentPosition
    
    private val _duration = MutableStateFlow(0L)
    actual val duration: StateFlow<Long> = _duration
    
    actual fun play(url: String) {
        println("🎵 WASM - 오디오 재생 (구현 필요): $url")
        // TODO: HTML5 Audio API 구현
        _isPlaying.value = true
    }
    
    actual fun pause() {
        println("⏸️ WASM - 일시정지")
        _isPlaying.value = false
    }
    
    actual fun stop() {
        println("⏹️ WASM - 정지")
        _isPlaying.value = false
    }
    
    actual fun release() {
        println("🗑️ WASM - 리소스 해제")
        _isPlaying.value = false
    }
    
    actual fun seekTo(position: Long) {
        println("⏭️ WASM - 위치 이동: $position")
    }
}
