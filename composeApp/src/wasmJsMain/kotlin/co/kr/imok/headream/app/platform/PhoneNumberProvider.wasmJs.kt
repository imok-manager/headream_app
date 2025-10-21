package co.kr.imok.headream.app.platform

actual class PhoneNumberProvider actual constructor() {
    actual fun getPhoneNumber(): String? {
        // WASM 환경에서는 전화번호를 가져올 수 없음
        println("⚠️ WASM 환경에서는 전화번호를 가져올 수 없습니다")
        return null
    }
}
