package co.kr.imokapp.headream.audio

actual fun initializeAudioWithContext(context: Any) {
    // iOS는 Context가 필요하지 않음
    println("🎵 iOS - Context 초기화 불필요")
}
