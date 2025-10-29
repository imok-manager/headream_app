package co.kr.imokapp.headream.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import co.kr.imokapp.headream.Platform
import co.kr.imokapp.headream.network.HaedreamApiClient
import co.kr.imokapp.headream.platform.DeviceInfoProvider
import kotlin.random.Random

class UserManager(
    private val apiClient: HaedreamApiClient,
    private val platform: Platform
) {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    private val _userUuid = MutableStateFlow<String?>(null)
    val userUuid: StateFlow<String?> = _userUuid.asStateFlow()
    
    suspend fun loginOrRegister(pushToken: String? = null): Result<User> {
        return try {
            // DeviceInfoProvider를 사용해서 올바른 기기 정보 수집
            val deviceInfoProvider = DeviceInfoProvider()
            val uuid = deviceInfoProvider.getOrCreateUUID()
            val deviceInfo = deviceInfoProvider.getDeviceInfo()
            val userAgent = deviceInfoProvider.getUserAgent()
            
            println("=== UserManager 로그인 ===")
            println("UUID: $uuid")
            println("DeviceInfo: platform=${deviceInfo.platform}, version=${deviceInfo.version}, model=${deviceInfo.model}")
            println("UserAgent: $userAgent")
            println("=========================")
            
            val loginRequest = LoginRequest(
                uuid = uuid,
                deviceInfo = deviceInfo,
                pushToken = pushToken,
                userAgent = userAgent
            )
            
            val result = apiClient.login(loginRequest)
            result.onSuccess { loginResponse ->
                // UUID 추출 (여러 소스에서 시도)
                val extractedUuid = loginResponse.userUuid 
                    ?: loginResponse.uuid 
                    ?: loginResponse.user?.uuid 
                    ?: loginResponse.user?.userUuid 
                    ?: uuid
                
                println("🔍 로그인 응답에서 UUID 추출:")
                println("- loginResponse.userUuid: ${loginResponse.userUuid}")
                println("- loginResponse.uuid: ${loginResponse.uuid}")
                println("- loginResponse.user?.uuid: ${loginResponse.user?.uuid}")
                println("- loginResponse.user?.userUuid: ${loginResponse.user?.userUuid}")
                println("- 최종 UUID: $extractedUuid")
                
                _userUuid.value = extractedUuid
                // User 객체는 별도로 생성
                _currentUser.value = User(
                    uuid = extractedUuid,
                    lastLoginTime = "",
                    createdAt = "",
                    isActive = true
                )
            }
            
            result.map { 
                User(
                    uuid = it.userUuid ?: uuid,
                    lastLoginTime = "",
                    createdAt = "",
                    isActive = true
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updatePushToken(pushToken: String): Result<Unit> {
        val uuid = _userUuid.value ?: return Result.failure(Exception("사용자가 로그인되지 않았습니다"))
        
        val request = PushTokenUpdateRequest(
            uuid = uuid,
            pushToken = pushToken
        )
        
        return apiClient.updatePushToken(request)
    }
    
    fun logout() {
        _currentUser.value = null
        _userUuid.value = null
    }
    
    fun isLoggedIn(): Boolean {
        return _currentUser.value != null && _userUuid.value != null
    }
    
    private fun generateSimpleUuid(): String {
        // 간단한 UUID 형태 생성 (실제 프로덕션에서는 더 정교한 UUID 생성 필요)
        val chars = "0123456789abcdef"
        val random = Random.Default
        return buildString {
            repeat(8) { append(chars[random.nextInt(16)]) }
            append("-")
            repeat(4) { append(chars[random.nextInt(16)]) }
            append("-")
            append("4") // UUID version 4
            repeat(3) { append(chars[random.nextInt(16)]) }
            append("-")
            append(chars[8 + random.nextInt(4)]) // variant bits
            repeat(3) { append(chars[random.nextInt(16)]) }
            append("-")
            repeat(12) { append(chars[random.nextInt(16)]) }
        }
    }
}
