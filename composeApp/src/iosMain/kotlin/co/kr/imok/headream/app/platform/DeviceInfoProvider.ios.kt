package co.kr.imok.headream.app.platform

import co.kr.imok.headream.app.data.DeviceInfo
import platform.Foundation.NSUUID
import platform.UIKit.UIDevice
import platform.Foundation.NSBundle

actual class DeviceInfoProvider {
    actual fun getDeviceInfo(): DeviceInfo {
        val device = UIDevice.currentDevice
        return DeviceInfo(
            platform = "iOS",
            version = device.systemVersion,
            model = device.model,
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
        
        // iOS에서는 identifierForVendor 사용 (앱 재설치 시에도 동일)
        val device = UIDevice.currentDevice
        val vendorId = device.identifierForVendor?.UUIDString()
        
        val deviceUUID = if (!vendorId.isNullOrEmpty()) {
            "ios-$vendorId"
        } else {
            // identifierForVendor를 얻을 수 없는 경우 기기 정보 기반
            val deviceInfo = "${device.model}-${device.systemVersion}".hashCode()
            "ios-device-${kotlin.math.abs(deviceInfo)}"
        }
        
        preferencesManager.putString(PreferencesKeys.USER_UUID, deviceUUID)
        println("새 기기 UUID 생성 및 저장: $deviceUUID")
        return deviceUUID
    }
    
    actual fun getUserAgent(): String {
        val deviceInfo = getDeviceInfo()
        return "HAEDREAM/${deviceInfo.appVersion} (${deviceInfo.model}; iOS ${deviceInfo.version})"
    }
}
