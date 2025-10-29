package co.kr.imokapp.headream.platform

import co.kr.imokapp.headream.data.DeviceInfo
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
        val device = UIDevice.currentDevice
        val vendorId = device.identifierForVendor?.UUIDString()
        
        // identifierForVendor를 주 식별자로 사용
        // 이것은 앱 업데이트 시에는 유지되지만, 같은 vendor의 모든 앱을 삭제하면 변경됩니다
        val deviceUUID = if (!vendorId.isNullOrEmpty()) {
            val uuid = "ios-$vendorId"
            println("📱 iOS - identifierForVendor 기반 UUID 사용: $uuid")
            uuid
        } else {
            // identifierForVendor를 얻을 수 없는 경우 (매우 드문 경우)
            // UserDefaults에서 이전에 생성한 UUID 확인
            val preferencesManager = PreferencesManager()
            val savedUUID = preferencesManager.getString(PreferencesKeys.USER_UUID)
            
            if (!savedUUID.isNullOrEmpty()) {
                println("📦 UserDefaults에서 기존 UUID 사용: $savedUUID")
                savedUUID
            } else {
                // 새로운 랜덤 UUID 생성 및 저장
                val randomUUID = NSUUID.UUID().UUIDString()
                val newUUID = "ios-random-$randomUUID"
                preferencesManager.putString(PreferencesKeys.USER_UUID, newUUID)
                println("🆕 새 랜덤 UUID 생성 및 저장: $newUUID")
                newUUID
            }
        }
        
        return deviceUUID
    }
    
    actual fun getUserAgent(): String {
        val deviceInfo = getDeviceInfo()
        return "HAEDREAM/${deviceInfo.appVersion} (${deviceInfo.model}; iOS ${deviceInfo.version})"
    }
}

/**
 * iOS UUID 전략:
 * 
 * 1. identifierForVendor를 주 식별자로 사용
 *    - 장점: 앱 업데이트 시 유지됨, Apple 공식 권장
 *    - 단점: 같은 vendor의 모든 앱을 삭제하면 변경됨
 * 
 * 2. 앱 재설치 시 UUID가 변경되는 것은 iOS의 정상 동작입니다.
 *    Apple은 개인정보 보호를 위해 의도적으로 이렇게 설계했습니다.
 * 
 * 3. 완전한 영구 식별이 필요하다면:
 *    - 서버에서 전화번호나 이메일 등으로 사용자 식별
 *    - iCloud 계정 연동
 *    - Keychain에 저장 (Swift wrapper 필요)
 */
