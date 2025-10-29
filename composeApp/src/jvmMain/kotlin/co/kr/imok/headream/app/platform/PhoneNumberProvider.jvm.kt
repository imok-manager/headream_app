package co.kr.imokapp.headream.platform

actual class PhoneNumberProvider actual constructor() {
    actual fun getPhoneNumber(): String? {
        // Desktop에서는 전화번호가 없음
        println("⚠️ Desktop 환경에서는 전화번호가 없습니다")
        return null
    }
}
