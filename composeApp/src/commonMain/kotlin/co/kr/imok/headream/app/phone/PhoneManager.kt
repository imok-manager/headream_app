package co.kr.imokapp.headream.phone

import co.kr.imokapp.headream.data.CallStatus

interface PhoneManager {
    suspend fun makeCall(phoneNumber: String): Result<Unit>
    suspend fun startRecording(): Result<Unit>
    suspend fun stopRecording(): Result<ByteArray?>
    fun isRecordingSupported(): Boolean
    fun getCurrentCallStatus(): CallStatus
}

expect fun createPhoneManager(): PhoneManager
