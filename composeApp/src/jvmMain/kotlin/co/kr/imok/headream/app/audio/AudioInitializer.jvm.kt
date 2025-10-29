package co.kr.imokapp.headream.audio

actual fun initializeAudioWithContext(context: Any) {
    // JVM은 Context가 필요하지 않음
    println("🎵 JVM - Context 초기화 불필요")
}
