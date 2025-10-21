package co.kr.imok.headream.app.phone

import co.kr.imok.headream.app.data.CallStatus
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

class PhoneManagerImpl : PhoneManager {
    
    private var currentCallStatus = CallStatus.COMPLETED
    
    override suspend fun makeCall(phoneNumber: String): Result<Unit> {
        return try {
            val telUrl = NSURL.URLWithString("tel:$phoneNumber")
            
            if (telUrl != null && UIApplication.sharedApplication.canOpenURL(telUrl)) {
                UIApplication.sharedApplication.openURL(telUrl)
                currentCallStatus = CallStatus.DIALING
                Result.success(Unit)
            } else {
                Result.failure(Exception("전화를 걸 수 없습니다"))
            }
        } catch (e: Exception) {
            currentCallStatus = CallStatus.FAILED
            Result.failure(e)
        }
    }
    
    override suspend fun startRecording(): Result<Unit> {
        // iOS에서는 통화 녹음이 제한적임
        return Result.failure(UnsupportedOperationException("iOS에서는 통화 녹음이 지원되지 않습니다"))
    }
    
    override suspend fun stopRecording(): Result<ByteArray?> {
        return Result.failure(UnsupportedOperationException("iOS에서는 통화 녹음이 지원되지 않습니다"))
    }
    
    override fun isRecordingSupported(): Boolean {
        return false // iOS에서는 통화 녹음 불가
    }
    
    override fun getCurrentCallStatus(): CallStatus {
        return currentCallStatus
    }
}

actual fun createPhoneManager(): PhoneManager {
    return PhoneManagerImpl()
}
