package co.kr.imok.headream.app.platform

expect class PhoneNumberProvider() {
    fun getPhoneNumber(): String?
}
