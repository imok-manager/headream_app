package co.kr.imokapp.headream.platform

import co.kr.imokapp.headream.data.DeviceInfo
import kotlin.js.Date
import kotlin.random.Random

actual class DeviceInfoProvider {
    actual fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            platform = "Web",
            version = "Browser",
            model = "Web Browser",
            appVersion = "1.0.0"
        )
    }
    
    actual fun getOrCreateUUID(): String {
        val preferencesManager = PreferencesManager()
        val savedUUID = preferencesManager.getString(PreferencesKeys.USER_UUID)
        if (!savedUUID.isNullOrEmpty()) {
            return savedUUID
        }
        
        // Simple UUID generation for JS
        val chars = "0123456789abcdef"
        val newUUID = buildString {
            repeat(32) { i ->
                if (i == 8 || i == 12 || i == 16 || i == 20) append('-')
                append(chars[Random.nextInt(16)])
            }
        }
        preferencesManager.putString(PreferencesKeys.USER_UUID, newUUID)
        return newUUID
    }
    
    actual fun getUserAgent(): String {
        val deviceInfo = getDeviceInfo()
        return "HAEDREAM/${deviceInfo.appVersion} (${deviceInfo.model}; ${deviceInfo.platform})"
    }
}
