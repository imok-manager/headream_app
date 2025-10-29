package co.kr.imokapp.headream.audio

import kotlinx.coroutines.flow.StateFlow

expect class AudioPlayer {
    val isPlaying: StateFlow<Boolean>
    val currentPosition: StateFlow<Long>
    val duration: StateFlow<Long>
    
    fun play(url: String)
    fun pause()
    fun stop()
    fun release()
    fun seekTo(position: Long)
}
