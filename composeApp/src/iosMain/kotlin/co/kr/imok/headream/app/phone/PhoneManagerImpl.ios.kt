package co.kr.imokapp.headream.phone

import co.kr.imokapp.headream.data.CallStatus
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
class PhoneManagerImpl : PhoneManager {
    
    private var currentCallStatus = CallStatus.COMPLETED
    
    override suspend fun makeCall(phoneNumber: String): Result<Unit> {
        return try {
            // tel: URL 스킴 생성
            val telUrl = NSURL.URLWithString("tel:$phoneNumber")
            
            println("📱 iOS - 전화 걸기 시도")
            println("- 전화번호: $phoneNumber")
            println("- URL: $telUrl")
            
            if (telUrl == null) {
                println("❌ iOS - URL 생성 실패")
                return Result.failure(Exception("잘못된 전화번호 형식입니다"))
            }
            
            val app = UIApplication.sharedApplication
            val canOpen = app.canOpenURL(telUrl)
            println("- canOpenURL: $canOpen")
            
            if (canOpen) {
                // iOS 10.0 이상: open(_:options:completionHandler:) 메서드 사용
                // Kotlin/Native에서는 openURL로 바인딩됨
                println("📱 iOS - 전화 앱 열기 시작...")
                
                app.openURL(telUrl, mapOf<Any?, Any?>()) { success ->
                    if (success) {
                        println("✅ iOS - 전화 앱 열기 성공!")
                        currentCallStatus = CallStatus.DIALING
                    } else {
                        println("❌ iOS - 전화 앱 열기 실패")
                        currentCallStatus = CallStatus.FAILED
                    }
                }
                
                // 즉시 성공 반환 (비동기 처리)
                currentCallStatus = CallStatus.DIALING
                Result.success(Unit)
            } else {
                println("❌ iOS - tel: URL 스킴을 열 수 없습니다")
                println("  Info.plist에 LSApplicationQueriesSchemes 설정을 확인하세요")
                Result.failure(Exception("전화를 걸 수 없습니다. 시뮬레이터에서는 전화 기능이 지원되지 않습니다."))
            }
        } catch (e: Exception) {
            println("💥 iOS - 전화 걸기 예외 발생: ${e.message}")
            e.printStackTrace()
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
