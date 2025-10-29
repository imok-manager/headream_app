package co.kr.imokapp.headream.platform

import platform.Foundation.NSUserDefaults

actual class PreferencesManager actual constructor() {
    private val userDefaults = NSUserDefaults.standardUserDefaults
    
    actual fun getString(key: String, defaultValue: String?): String? {
        return userDefaults.stringForKey(key) ?: defaultValue
    }
    
    actual fun putString(key: String, value: String) {
        userDefaults.setObject(value, key)
        userDefaults.synchronize()
    }
    
    actual fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return if (userDefaults.objectForKey(key) != null) {
            userDefaults.boolForKey(key)
        } else {
            defaultValue
        }
    }
    
    actual fun putBoolean(key: String, value: Boolean) {
        userDefaults.setBool(value, key)
        userDefaults.synchronize()
    }
    
    actual fun clear() {
        // iOS에서는 간단하게 모든 키를 제거
        val keys = listOf("user_uuid", "user_token", "user_name", "is_logged_in")
        keys.forEach { key ->
            userDefaults.removeObjectForKey(key)
        }
        userDefaults.synchronize()
    }
}
