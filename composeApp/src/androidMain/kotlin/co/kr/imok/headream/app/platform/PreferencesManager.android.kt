package co.kr.imok.headream.app.platform

import android.content.Context
import android.content.SharedPreferences

// 전역 Context를 저장하기 위한 object
object AndroidContext {
    var context: Context? = null
}

actual class PreferencesManager actual constructor() {
    private val prefs: SharedPreferences? by lazy {
        AndroidContext.context?.getSharedPreferences("haedream_prefs", Context.MODE_PRIVATE)
    }
    
    actual fun getString(key: String, defaultValue: String?): String? {
        return prefs?.getString(key, defaultValue) ?: defaultValue
    }
    
    actual fun putString(key: String, value: String) {
        prefs?.edit()?.putString(key, value)?.apply()
    }
    
    actual fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return prefs?.getBoolean(key, defaultValue) ?: defaultValue
    }
    
    actual fun putBoolean(key: String, value: Boolean) {
        prefs?.edit()?.putBoolean(key, value)?.apply()
    }
    
    actual fun clear() {
        prefs?.edit()?.clear()?.apply()
    }
}
