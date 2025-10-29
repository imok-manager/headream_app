package co.kr.imokapp.headream.audio

expect object AudioPlaybackManager {
    fun initialize()
    fun play(url: String)
    fun pause()
    fun resume() // 일시정지된 재생 재개
    fun stop()
    fun isPlaying(): Boolean
    fun release()
    fun getCurrentPosition(): Long // 현재 재생 위치 (밀리초)
    fun getDuration(): Long // 총 재생 시간 (밀리초)
    fun seekTo(position: Long) // 특정 위치로 이동 (밀리초)
}
