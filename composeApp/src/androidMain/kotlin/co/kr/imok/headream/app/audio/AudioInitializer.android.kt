package co.kr.imokapp.headream.audio

import android.content.Context

actual fun initializeAudioWithContext(context: Any) {
    if (context is Context) {
        (AudioPlaybackManager as AudioPlaybackManager).initializeWithContext(context)
    }
}
