package co.kr.imok.headream.app.platform

import java.util.prefs.Preferences

actual class PreferencesManager actual constructor() {
    private val prefs = Preferences.userNodeForPackage(PreferencesManager::class.java)
    
    actual fun getString(key: String, defaultValue: String?): String? {
        return prefs.get(key, defaultValue)
    }
    
    actual fun putString(key: String, value: String) {
        prefs.put(key, value)
        prefs.flush()
    }
    
    actual fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return prefs.getBoolean(key, defaultValue)
    }
    
    actual fun putBoolean(key: String, value: Boolean) {
        prefs.putBoolean(key, value)
        prefs.flush()
    }
    
    actual fun clear() {
        prefs.clear()
        prefs.flush()
    }
}
