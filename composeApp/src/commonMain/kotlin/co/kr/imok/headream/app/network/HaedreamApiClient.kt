package co.kr.imok.headream.app.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import co.kr.imok.headream.app.data.*

class HaedreamApiClient {
    private val baseUrl = "https://apia.im-ok.co.kr/app"
    
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }
    
    // 사용자 로그인/등록
    suspend fun login(request: LoginRequest): Result<LoginResponse> {
        return try {
            val response: ApiResponse<LoginResponse> = httpClient.post("$baseUrl/auth/login") {
                contentType(ContentType.Application.Json)
                header("origin", "https://admin.im-ok.co.kr")
                setBody(request)
            }.body()
            
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 푸시 토큰 업데이트
    suspend fun updatePushToken(request: PushTokenUpdateRequest): Result<Unit> {
        return try {
            val response: ApiResponse<Unit> = httpClient.put("$baseUrl/auth/push-token") {
                contentType(ContentType.Application.Json)
                header("origin", "https://admin.im-ok.co.kr")
                setBody(request)
            }.body()
            
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 통화 기록 목록 조회
    suspend fun getCallRecords(
        userUuid: String,
        page: Int = 1,
        limit: Int = 20,
        callType: String? = null,
        isImportant: Boolean? = null,
        startDate: String? = null,
        endDate: String? = null,
        search: String? = null
    ): Result<CallRecordsResponse> {
        return try {
            println("📋 통화 기록 조회 API 호출:")
            println("- userUuid: $userUuid")
            println("- page: $page, limit: $limit")
            println("- callType: $callType")
            println("- isImportant: $isImportant")
            println("- URL: $baseUrl/calls/$userUuid")
            
            val httpResponse = httpClient.get("$baseUrl/calls/$userUuid") {
                header("origin", "https://admin.im-ok.co.kr")
                parameter("page", page)
                parameter("limit", limit)
                callType?.let { parameter("callType", it) }
                isImportant?.let { parameter("isImportant", it) }
                startDate?.let { parameter("startDate", it) }
                endDate?.let { parameter("endDate", it) }
                search?.let { parameter("search", it) }
            }
            
            println("📡 통화 기록 API 응답 상태: ${httpResponse.status}")
            
            if (httpResponse.status == HttpStatusCode.OK) {
                try {
                    // 먼저 raw JSON 응답을 확인
                    val rawResponse = httpResponse.body<String>()
                    println("📄 통화 기록 API 응답 JSON: $rawResponse")
                    
                    // 관대한 JSON 파싱
                    val json = kotlinx.serialization.json.Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        coerceInputValues = true
                    }
                    
                    val apiResponse: ApiResponse<CallRecordsResponse> = json.decodeFromString(rawResponse)
                    
                    if (apiResponse.success && apiResponse.data != null) {
                        println("✅ 통화 기록 API 파싱 성공:")
                        println("- 총 기록 수: ${apiResponse.data.records.size}")
                        println("- 페이지 정보: ${apiResponse.data.pagination}")
                        Result.success(apiResponse.data)
                    } else {
                        println("❌ 통화 기록 API 실패: ${apiResponse.message}")
                        Result.failure(Exception(apiResponse.message))
                    }
                } catch (e: Exception) {
                    println("❌ 통화 기록 API JSON 파싱 실패: ${e.message}")
                    Result.failure(Exception("JSON parsing failed: ${e.message}"))
                }
            } else {
                println("❌ 통화 기록 API 실패: ${httpResponse.status}")
                Result.failure(Exception("API failed with status: ${httpResponse.status}"))
            }
        } catch (e: Exception) {
            println("💥 통화 기록 API 오류: ${e.message}")
            Result.failure(e)
        }
    }
    
    // 통화 기록 상세 조회
    suspend fun getCallDetail(callId: Int): Result<CallDetailResponse> {
        return try {
            println("📋 통화 상세 조회 API 호출:")
            println("- callId: $callId")
            println("- URL: $baseUrl/calls/detail/$callId")
            
            val httpResponse = httpClient.get("$baseUrl/calls/detail/$callId") {
                header("origin", "https://admin.im-ok.co.kr")
            }
            
            println("📡 통화 상세 API 응답 상태: ${httpResponse.status}")
            
            if (httpResponse.status == HttpStatusCode.OK) {
                try {
                    // 먼저 raw JSON 응답을 확인
                    val rawResponse = httpResponse.body<String>()
                    println("📄 통화 상세 API 응답 JSON: $rawResponse")
                    
                    // 관대한 JSON 파싱
                    val json = kotlinx.serialization.json.Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        coerceInputValues = true
                    }
                    
                    val apiResponse: ApiResponse<CallDetailResponse> = json.decodeFromString(rawResponse)
                    
                    if (apiResponse.success && apiResponse.data != null) {
                        println("✅ 통화 상세 API 파싱 성공")
                        println("📋 API 응답 데이터 상세:")
                        println("- id: ${apiResponse.data.id}")
                        println("- callSummary: ${apiResponse.data.callSummary}")
                        println("- callContent: ${apiResponse.data.callContent}")
                        println("- audioFilePath: ${apiResponse.data.audioFilePath}")
                        println("- audioFileName: ${apiResponse.data.audioFileName}")
                        Result.success(apiResponse.data)
                    } else {
                        println("❌ 통화 상세 API 실패: ${apiResponse.message}")
                        Result.failure(Exception(apiResponse.message))
                    }
                } catch (e: Exception) {
                    println("❌ 통화 상세 API JSON 파싱 실패: ${e.message}")
                    Result.failure(Exception("JSON parsing failed: ${e.message}"))
                }
            } else {
                println("❌ 통화 상세 API 실패: ${httpResponse.status}")
                Result.failure(Exception("API failed with status: ${httpResponse.status}"))
            }
        } catch (e: Exception) {
            println("💥 통화 상세 API 오류: ${e.message}")
            Result.failure(e)
        }
    }
    
    // 통화 시작 API
    suspend fun startCall(request: CallRequest): Result<CallResponse> {
        return try {
            val httpResponse = httpClient.post("$baseUrl/calls") {
                contentType(ContentType.Application.Json)
                header("origin", "https://admin.im-ok.co.kr")
                setBody(request)
            }
            
            println("📡 통화 시작 API 응답 상태: ${httpResponse.status}")
            
            if (httpResponse.status == HttpStatusCode.OK) {
                try {
                    // 먼저 raw JSON 응답을 확인
                    val rawResponse = httpResponse.body<String>()
                    println("📄 통화 시작 API 응답 JSON: $rawResponse")
                    
                    // 관대한 JSON 파싱
                    val json = kotlinx.serialization.json.Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        coerceInputValues = true
                    }
                    
                    val apiResponse: ApiResponse<CallResponse> = json.decodeFromString(rawResponse)
                    
                    if (apiResponse.success && apiResponse.data != null) {
                        // Call ID 추출 (서버에서 id를 숫자로 반환)
                        val callId = apiResponse.data.callId ?: apiResponse.data.id?.toString()
                        val finalResponse = apiResponse.data.copy(callId = callId)
                        
                        println("✅ 통화 시작 API 파싱 성공:")
                        println("- callId: $callId")
                        println("- userUuid: ${apiResponse.data.userUuid}")
                        println("- callType: ${apiResponse.data.callType}")
                        println("- phoneNumber: ${apiResponse.data.phoneNumber}")
                        println("- callStartTime: ${apiResponse.data.callStartTime}")
                        println("- isImportant: ${apiResponse.data.isImportant}")
                        println("- tags: ${apiResponse.data.tags}")
                        println("- createdAt: ${apiResponse.data.createdAt}")
                        
                        Result.success(finalResponse)
                    } else {
                        Result.failure(Exception(apiResponse.message))
                    }
                } catch (e: Exception) {
                    println("❌ 통화 시작 API JSON 파싱 실패: ${e.message}")
                    Result.failure(Exception("JSON parsing failed: ${e.message}"))
                }
            } else {
                println("❌ 통화 시작 API 실패: ${httpResponse.status}")
                Result.failure(Exception("API failed with status: ${httpResponse.status}"))
            }
        } catch (e: Exception) {
            println("💥 통화 시작 API 오류: ${e.message}")
            Result.failure(e)
        }
    }
    
    // 통화 종료 API
    suspend fun endCall(callId: String, request: CallEndRequest): Result<CallEndResponse> {
        return try {
            val httpResponse = httpClient.put("$baseUrl/calls/$callId") {
                contentType(ContentType.Application.Json)
                header("origin", "https://admin.im-ok.co.kr")
                setBody(request)
            }
            
            println("📡 통화 종료 API 응답 상태: ${httpResponse.status}")
            
            if (httpResponse.status == HttpStatusCode.OK) {
                try {
                    // 먼저 raw JSON 응답을 확인
                    val rawResponse = httpResponse.body<String>()
                    println("📄 통화 종료 API 응답 JSON: $rawResponse")
                    
                    // 관대한 JSON 파싱
                    val json = kotlinx.serialization.json.Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        coerceInputValues = true
                    }
                    
                    val apiResponse: ApiResponse<CallEndResponse> = json.decodeFromString(rawResponse)
                    
                    if (apiResponse.success && apiResponse.data != null) {
                        println("✅ 통화 종료 API 파싱 성공")
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse.message))
                    }
                } catch (e: Exception) {
                    println("❌ 통화 종료 API JSON 파싱 실패: ${e.message}")
                    Result.failure(Exception("JSON parsing failed: ${e.message}"))
                }
            } else {
                println("❌ 통화 종료 API 실패: ${httpResponse.status}")
                Result.failure(Exception("API failed with status: ${httpResponse.status}"))
            }
        } catch (e: Exception) {
            println("💥 통화 종료 API 오류: ${e.message}")
            
            // 네트워크 오류인 경우 상세 정보 출력
            if (e.message?.contains("Unable to resolve host") == true) {
                println("🌐 DNS 해결 실패 - 네트워크 연결을 확인하세요")
                println("🔗 시도한 URL: $baseUrl/calls/$callId")
            } else if (e.message?.contains("timeout") == true) {
                println("⏰ 네트워크 타임아웃 - 서버 응답이 느립니다")
            } else if (e.message?.contains("Connection") == true) {
                println("🔌 연결 실패 - 인터넷 연결을 확인하세요")
            }
            
            Result.failure(e)
        }
    }
    
    // 네트워크 연결 테스트
    suspend fun testConnection(): Result<Unit> {
        return try {
            println("🔍 연결 테스트: $baseUrl")
            val response = httpClient.get("$baseUrl/health") {
                header("origin", "https://admin.im-ok.co.kr")
            }
            println("📡 연결 테스트 응답: ${response.status}")
            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ 연결 테스트 실패: ${e.message}")
            Result.failure(e)
        }
    }
    
    fun close() {
        httpClient.close()
    }
}
