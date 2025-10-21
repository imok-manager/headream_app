package co.kr.imok.headream.app.phone

import co.kr.imok.headream.app.data.CallStatus
import java.awt.Desktop
import java.net.URI

class PhoneManagerImpl : PhoneManager {
    
    private var currentCallStatus = CallStatus.COMPLETED
    
    override suspend fun makeCall(phoneNumber: String): Result<Unit> {
        return try {
            if (Desktop.isDesktopSupported()) {
                val desktop = Desktop.getDesktop()
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    // tel: 스키마를 지원하는 앱이 있다면 열기 시도
                    desktop.browse(URI("tel:$phoneNumber"))
                    currentCallStatus = CallStatus.DIALING
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("데스크톱에서는 전화 기능이 제한적입니다"))
                }
            } else {
                Result.failure(Exception("데스크톱 환경을 지원하지 않습니다"))
            }
        } catch (e: Exception) {
            currentCallStatus = CallStatus.FAILED
            Result.failure(e)
        }
    }
    
    override suspend fun startRecording(): Result<Unit> {
        return Result.failure(UnsupportedOperationException("데스크톱에서는 통화 녹음이 지원되지 않습니다"))
    }
    
    override suspend fun stopRecording(): Result<ByteArray?> {
        return Result.failure(UnsupportedOperationException("데스크톱에서는 통화 녹음이 지원되지 않습니다"))
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
