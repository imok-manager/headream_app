package co.kr.imokapp.headream.platform

expect class PreferencesManager() {
    fun getString(key: String, defaultValue: String? = null): String?
    fun putString(key: String, value: String)
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean
    fun putBoolean(key: String, value: Boolean)
    fun clear()
}

object PreferencesKeys {
    const val USER_UUID = "user_uuid"
    const val IS_FIRST_LAUNCH = "is_first_launch"
    const val LAST_LOGIN_TIME = "last_login_time"
}
