package co.kr.imokapp.headream.platform

import co.kr.imokapp.headream.data.DeviceInfo

expect class DeviceInfoProvider() {
    fun getDeviceInfo(): DeviceInfo
    fun getOrCreateUUID(): String
    fun getUserAgent(): String
}
