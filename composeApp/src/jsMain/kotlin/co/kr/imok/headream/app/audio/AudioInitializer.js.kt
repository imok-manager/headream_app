package co.kr.imokapp.headream.audio

actual fun initializeAudioWithContext(context: Any) {
    // JS는 Context가 필요하지 않음
    println("🎵 JS - Context 초기화 불필요")
}
