package co.kr.imok.headream.app.phone

import co.kr.imok.headream.app.data.CallStatus

class PhoneManagerImpl : PhoneManager {
    
    private var currentCallStatus = CallStatus.COMPLETED
    
    override suspend fun makeCall(phoneNumber: String): Result<Unit> {
        return try {
            // WASM에서는 제한적인 기능만 제공
            // 실제로는 JavaScript interop을 통해 구현해야 함
            currentCallStatus = CallStatus.DIALING
            Result.success(Unit)
        } catch (e: Exception) {
            currentCallStatus = CallStatus.FAILED
            Result.failure(e)
        }
    }
    
    override suspend fun startRecording(): Result<Unit> {
        return Result.failure(UnsupportedOperationException("WASM에서는 통화 녹음이 지원되지 않습니다"))
    }
    
    override suspend fun stopRecording(): Result<ByteArray?> {
        return Result.failure(UnsupportedOperationException("WASM에서는 통화 녹음이 지원되지 않습니다"))
    }
    
    override fun isRecordingSupported(): Boolean {
        return false
    }
    
    override fun getCurrentCallStatus(): CallStatus {
        return currentCallStatus
    }
}

actual fun createPhoneManager(): PhoneManager {
    return PhoneManagerImpl()
}
