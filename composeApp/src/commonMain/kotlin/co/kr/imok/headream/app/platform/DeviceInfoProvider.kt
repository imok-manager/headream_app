package co.kr.imok.headream.app.platform

import co.kr.imok.headream.app.data.DeviceInfo

expect class DeviceInfoProvider() {
    fun getDeviceInfo(): DeviceInfo
    fun getOrCreateUUID(): String
    fun getUserAgent(): String
}
