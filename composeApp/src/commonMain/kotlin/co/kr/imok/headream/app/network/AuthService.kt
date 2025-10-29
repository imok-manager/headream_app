package co.kr.imokapp.headream.network

import co.kr.imokapp.headream.data.LoginRequest
import co.kr.imokapp.headream.data.LoginResponse
import co.kr.imokapp.headream.platform.DeviceInfoProvider
import co.kr.imokapp.headream.platform.PhoneNumberProvider
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock

class AuthService(
    private val httpClient: HttpClient,
    private val deviceInfoProvider: DeviceInfoProvider
) {
    private val baseUrl = "https://apia.im-ok.co.kr" // 실제 API 서버 URL
    
    suspend fun loginOrRegister(): Result<LoginResponse> {
        return try {
            val uuid = deviceInfoProvider.getOrCreateUUID()
            val deviceInfo = deviceInfoProvider.getDeviceInfo()
            val userAgent = deviceInfoProvider.getUserAgent()
            
            // 기기의 전화번호 가져오기
            val phoneNumberProvider = PhoneNumberProvider()
            val phoneNumber = phoneNumberProvider.getPhoneNumber()
            
            println("디바이스 정보 수집 완료:")
            println("- UUID: $uuid")
            println("- Platform: ${deviceInfo.platform}")
            println("- Version: ${deviceInfo.version}")
            println("- Model: ${deviceInfo.model}")
            println("- UserAgent: $userAgent")
            println("- PhoneNumber: ${phoneNumber ?: "없음"}")
            
            val request = LoginRequest(
                uuid = uuid,
                deviceInfo = deviceInfo,
                pushToken = null, // FCM 토큰은 나중에 구현
                userAgent = userAgent,
                phoneNumber = phoneNumber
            )
            
            val response = httpClient.post("$baseUrl/app/auth/login") {
                contentType(ContentType.Application.Json)
                header("origin", "https://admin.im-ok.co.kr")
                setBody(request)
            }
            
            println("📡 서버 응답 상태: ${response.status}")
            
            if (response.status == HttpStatusCode.OK) {
                try {
                    // 먼저 raw JSON 응답을 확인
                    val rawResponse = response.body<String>()
                    println("📄 서버 응답 JSON: $rawResponse")
                    
                    // JSON 파싱 시도 (관대한 설정)
                    val json = kotlinx.serialization.json.Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        coerceInputValues = true
                    }
                    val loginResponse: LoginResponse = json.decodeFromString(rawResponse)
                    println("✅ 서버 로그인 성공: ${loginResponse.message}")
                    Result.success(loginResponse)
                } catch (e: Exception) {
                    println("❌ JSON 파싱 실패: ${e.message}")
                    Result.failure(Exception("JSON parsing failed: ${e.message}"))
                }
            } else {
                println("❌ 서버 로그인 실패: ${response.status}")
                Result.failure(Exception("Login failed with status: ${response.status}"))
            }
        } catch (e: Exception) {
            println("로그인 API 호출 실패: ${e.message}")
            // API 서버가 없을 경우 목업 응답 반환
            delay(1000) // 네트워크 지연 시뮬레이션
            
            val mockUuid = try {
                deviceInfoProvider.getOrCreateUUID()
            } catch (ex: Exception) {
                "mock-uuid-${Clock.System.now().toEpochMilliseconds()}"
            }
            
            Result.success(
                LoginResponse(
                    success = true,
                    message = "로그인 성공 (목업 - ${e.message})",
                    userUuid = mockUuid,
                    isNewUser = true,
                    accessToken = "mock_token_${Clock.System.now().toEpochMilliseconds()}"
                )
            )
        }
    }
}
