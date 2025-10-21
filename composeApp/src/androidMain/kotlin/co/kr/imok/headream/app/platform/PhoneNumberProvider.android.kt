package co.kr.imok.headream.app.platform

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat

actual class PhoneNumberProvider actual constructor() {
    actual fun getPhoneNumber(): String? {
        return try {
            val context = AndroidContext.context ?: return null
            
            // 필요한 권한들 확인
            val hasReadPhoneState = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
            val hasReadPhoneNumbers = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_NUMBERS) == PackageManager.PERMISSION_GRANTED
            val hasReadSms = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
            
            println("📋 권한 상태:")
            println("- READ_PHONE_STATE: $hasReadPhoneState")
            println("- READ_PHONE_NUMBERS: $hasReadPhoneNumbers")
            println("- READ_SMS: $hasReadSms")
            
            if (!hasReadPhoneState && !hasReadPhoneNumbers && !hasReadSms) {
                println("❌ 전화번호를 읽을 수 있는 권한이 없습니다")
                return null
            }
            
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            var phoneNumber: String? = null
            
            // 방법 1: line1Number 시도 (READ_PHONE_NUMBERS 권한 필요)
            if (hasReadPhoneNumbers) {
                try {
                    phoneNumber = telephonyManager?.line1Number
                    println("📱 방법1 (line1Number): ${phoneNumber ?: "없음"}")
                } catch (e: Exception) {
                    println("⚠️ 방법1 실패: ${e.message}")
                }
            }
            
            // 방법 2: getLine1NumberForDisplay 시도 (Android 5.1+)
            if (phoneNumber.isNullOrEmpty() && hasReadPhoneState) {
                try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                        phoneNumber = telephonyManager?.line1Number
                        println("📱 방법2 (getLine1NumberForDisplay): ${phoneNumber ?: "없음"}")
                    }
                } catch (e: Exception) {
                    println("⚠️ 방법2 실패: ${e.message}")
                }
            }
            
            // 전화번호가 비어있거나 null인 경우 처리
            if (phoneNumber.isNullOrEmpty()) {
                println("⚠️ 모든 방법으로 전화번호를 읽을 수 없습니다")
                println("💡 사용자가 SIM 카드에 전화번호를 저장하지 않았거나, 통신사에서 제공하지 않을 수 있습니다")
                return null
            }
            
            // 국가 코드 처리 (한국 +82)
            val cleanNumber = phoneNumber.replace("+82", "0").replace("-", "").replace(" ", "")
            println("📞 정리된 전화번호: $cleanNumber")
            
            cleanNumber
        } catch (e: Exception) {
            println("❌ 전화번호 가져오기 실패: ${e.message}")
            null
        }
    }
}
