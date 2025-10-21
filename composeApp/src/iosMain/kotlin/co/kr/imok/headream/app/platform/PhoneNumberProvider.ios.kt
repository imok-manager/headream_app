package co.kr.imok.headream.app.platform

actual class PhoneNumberProvider actual constructor() {
    actual fun getPhoneNumber(): String? {
        // iOS에서는 보안상 앱에서 직접 전화번호를 가져올 수 없음
        println("⚠️ iOS에서는 전화번호를 직접 가져올 수 없습니다")
        return null
    }
}
