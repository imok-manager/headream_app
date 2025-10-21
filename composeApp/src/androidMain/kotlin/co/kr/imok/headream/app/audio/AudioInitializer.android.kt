package co.kr.imok.headream.app.audio

import android.content.Context

actual fun initializeAudioWithContext(context: Any) {
    if (context is Context) {
        (AudioPlaybackManager as AudioPlaybackManager).initializeWithContext(context)
    }
}
