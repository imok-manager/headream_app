package co.kr.imok.headream.app.platform

import co.kr.imok.headream.app.data.DeviceInfo
import java.util.*

actual class DeviceInfoProvider {
    actual fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            platform = "Desktop",
            version = System.getProperty("os.version") ?: "Unknown",
            model = System.getProperty("os.name") ?: "Unknown",
            appVersion = "1.0.0"
        )
    }
    
    actual fun getOrCreateUUID(): String {
        val preferencesManager = PreferencesManager()
        val savedUUID = preferencesManager.getString(PreferencesKeys.USER_UUID)
        if (!savedUUID.isNullOrEmpty()) {
            println("기존 기기 UUID 사용: $savedUUID")
            return savedUUID
        }
        
        // JVM에서는 시스템 속성들을 조합해서 기기 고유값 생성
        val osName = System.getProperty("os.name") ?: "unknown"
        val osVersion = System.getProperty("os.version") ?: "unknown"
        val userName = System.getProperty("user.name") ?: "unknown"
        val javaVersion = System.getProperty("java.version") ?: "unknown"
        
        val deviceInfo = "$osName-$osVersion-$userName-$javaVersion".hashCode()
        val deviceUUID = "jvm-device-${Math.abs(deviceInfo)}"
        
        preferencesManager.putString(PreferencesKeys.USER_UUID, deviceUUID)
        println("새 기기 UUID 생성 및 저장: $deviceUUID")
        return deviceUUID
    }
    
    actual fun getUserAgent(): String {
        val deviceInfo = getDeviceInfo()
        return "HAEDREAM/${deviceInfo.appVersion} (${deviceInfo.model}; ${deviceInfo.platform} ${deviceInfo.version})"
    }
}
