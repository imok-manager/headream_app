package co.kr.imok.headream.app.platform

import android.os.Build
import android.provider.Settings
import co.kr.imok.headream.app.data.DeviceInfo
import java.util.*

actual class DeviceInfoProvider {
    private val preferencesManager = PreferencesManager()
    
    actual fun getDeviceInfo(): DeviceInfo {
        return try {
            DeviceInfo(
                platform = "Android",
                version = Build.VERSION.RELEASE ?: "Unknown",
                model = "${Build.MANUFACTURER ?: "Unknown"} ${Build.MODEL ?: "Unknown"}".trim(),
                appVersion = "1.0.0",
                manufacturer = Build.MANUFACTURER ?: "Unknown"
            )
        } catch (e: Exception) {
            // 권한 오류나 기타 예외 발생 시 기본값 반환
            DeviceInfo(
                platform = "Android",
                version = "Unknown",
                model = "Android Device",
                appVersion = "1.0.0",
                manufacturer = "Unknown"
            )
        }
    }
    
    actual fun getOrCreateUUID(): String {
        return try {
            // 저장된 UUID가 있는지 확인
            val savedUUID = preferencesManager.getString(PreferencesKeys.USER_UUID)
            if (!savedUUID.isNullOrEmpty()) {
                println("기존 기기 UUID 사용: $savedUUID")
                return savedUUID
            }
            
            // Android ID (기기 고유값) 사용
            val context = AndroidContext.context
            val androidId = if (context != null) {
                try {
                    Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                } catch (e: Exception) {
                    null
                }
            } else null
            
            val deviceUUID = if (!androidId.isNullOrEmpty() && androidId != "9774d56d682e549c") {
                // 유효한 Android ID가 있는 경우 (9774d56d682e549c는 에뮬레이터 기본값)
                "android-$androidId"
            } else {
                // Android ID를 얻을 수 없는 경우 기기 정보 기반 UUID 생성
                val deviceInfo = "${Build.MANUFACTURER}-${Build.MODEL}-${Build.SERIAL}".hashCode()
                "android-device-${Math.abs(deviceInfo)}"
            }
            
            // 생성된 기기 UUID 저장
            preferencesManager.putString(PreferencesKeys.USER_UUID, deviceUUID)
            println("새 기기 UUID 생성 및 저장: $deviceUUID")
            deviceUUID
        } catch (e: Exception) {
            // 모든 방법이 실패한 경우 대체 방법
            val fallbackUUID = "android-fallback-${System.currentTimeMillis().hashCode()}"
            println("기기 UUID 생성 실패, 대체 UUID 사용: $fallbackUUID")
            fallbackUUID
        }
    }
    
    actual fun getUserAgent(): String {
        return try {
            val deviceInfo = getDeviceInfo()
            "HAEDREAM/${deviceInfo.appVersion} (${deviceInfo.model}; Android ${deviceInfo.version})"
        } catch (e: Exception) {
            "HAEDREAM/1.0.0 (Android Device; Android Unknown)"
        }
    }
}
