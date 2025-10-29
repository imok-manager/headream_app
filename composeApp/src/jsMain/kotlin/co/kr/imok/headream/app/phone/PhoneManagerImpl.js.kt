package co.kr.imokapp.headream.phone

import co.kr.imokapp.headream.data.CallStatus
import kotlinx.browser.window

class PhoneManagerImpl : PhoneManager {
    
    private var currentCallStatus = CallStatus.COMPLETED
    
    override suspend fun makeCall(phoneNumber: String): Result<Unit> {
        return try {
            // 웹에서는 tel: 링크로 전화 앱 열기
            window.open("tel:$phoneNumber", "_self")
            currentCallStatus = CallStatus.DIALING
            Result.success(Unit)
        } catch (e: Exception) {
            currentCallStatus = CallStatus.FAILED
            Result.failure(e)
        }
    }
    
    override suspend fun startRecording(): Result<Unit> {
        return Result.failure(UnsupportedOperationException("웹에서는 통화 녹음이 지원되지 않습니다"))
    }
    
    override suspend fun stopRecording(): Result<ByteArray?> {
        return Result.failure(UnsupportedOperationException("웹에서는 통화 녹음이 지원되지 않습니다"))
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
