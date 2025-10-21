package co.kr.imok.headream.app.audio

actual fun initializeAudioWithContext(context: Any) {
    // WASM은 Context가 필요하지 않음
    println("🎵 WASM - Context 초기화 불필요")
}
