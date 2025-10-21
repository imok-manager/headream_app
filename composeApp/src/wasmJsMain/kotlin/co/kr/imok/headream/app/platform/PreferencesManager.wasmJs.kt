package co.kr.imok.headream.app.platform

// WASM에서는 간단한 메모리 기반 저장소 사용
actual class PreferencesManager actual constructor() {
    companion object {
        private val storage = mutableMapOf<String, String>()
    }
    
    actual fun getString(key: String, defaultValue: String?): String? {
        return storage[key] ?: defaultValue
    }
    
    actual fun putString(key: String, value: String) {
        storage[key] = value
    }
    
    actual fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        val value = storage[key]
        return value?.toBoolean() ?: defaultValue
    }
    
    actual fun putBoolean(key: String, value: Boolean) {
        storage[key] = value.toString()
    }
    
    actual fun clear() {
        storage.clear()
    }
}
