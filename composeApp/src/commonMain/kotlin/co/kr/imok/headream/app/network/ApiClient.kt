package co.kr.imokapp.headream.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import co.kr.imokapp.headream.data.*

class ApiClient {
    val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }
    
    private val baseUrl = "https://apia.im-ok.co.kr" // 실제 API 서버 URL
    
    suspend fun uploadCallRecording(request: UploadCallRequest): Result<CallRecord> {
        return try {
            val response = httpClient.post("$baseUrl/calls/upload") {
                contentType(ContentType.Application.Json)
                header("origin", "https://admin.im-ok.co.kr")
                setBody(request)
            }
            
            if (response.status.isSuccess()) {
                val apiResponse: ApiResponse<CallRecord> = response.body()
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(Exception(apiResponse.message ?: "Upload failed"))
                }
            } else {
                Result.failure(Exception("HTTP ${response.status.value}: ${response.status.description}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getCallHistory(phoneNumber: String): Result<CallHistory> {
        return try {
            val response = httpClient.get("$baseUrl/calls/history") {
                header("origin", "https://admin.im-ok.co.kr")
                parameter("phoneNumber", phoneNumber)
            }
            
            if (response.status.isSuccess()) {
                val apiResponse: ApiResponse<CallHistory> = response.body()
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(Exception(apiResponse.message ?: "Failed to get history"))
                }
            } else {
                Result.failure(Exception("HTTP ${response.status.value}: ${response.status.description}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAllCallHistory(): Result<List<CallHistory>> {
        return try {
            val response = httpClient.get("$baseUrl/calls/history/all") {
                header("origin", "https://admin.im-ok.co.kr")
            }
            
            if (response.status.isSuccess()) {
                val apiResponse: ApiResponse<List<CallHistory>> = response.body()
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(Exception(apiResponse.message ?: "Failed to get all history"))
                }
            } else {
                Result.failure(Exception("HTTP ${response.status.value}: ${response.status.description}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun close() {
        httpClient.close()
    }
}
