package co.kr.imokapp.headream.data

import kotlinx.datetime.Instant
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

// 공통 API 응답 형식
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String = "",
    val timestamp: String
)

@Serializable
data class DeviceInfo(
    val platform: String, // "iOS" | "Android"
    val version: String,
    val model: String,
    val appVersion: String,
    val manufacturer: String? = null
)

@Serializable
data class User(
    val uuid: String,
    val lastLoginTime: String,
    val createdAt: String,
    val isActive: Boolean
)

@Serializable
data class PushTokenUpdateRequest(
    val uuid: String,
    val pushToken: String
)

// 통화 기록 관련
@Serializable
data class CallRecordsResponse(
    val records: List<CallRecordApi>,
    val pagination: Pagination
)

@Serializable
data class CallRecordApi(
    val id: Int,
    val call_type: String, // "incoming", "outgoing", "missed"
    val phone_number: String,
    val contact_name: String? = null,
    val call_duration: Int, // 초 단위
    val call_start_time: String,
    val call_end_time: String? = null,
    val call_summary: String? = null,
    val audio_file_path: String? = null, // 녹음 파일 경로
    val is_important: Int = 0, // 서버에서 0/1로 반환
    val tags: List<String>? = null, // null 허용
    val created_at: String,
    val updated_at: String
) {
    // UI에서 사용할 수 있도록 변환
    fun toCallRecord(): CallRecord {
        return CallRecord(
            id = id.toString(),
            phoneNumber = phone_number,
            counselorName = contact_name ?: "상담사",
            duration = call_duration.toLong(),
            timestamp = parseTimestamp(call_start_time),
            audioFileUrl = audio_file_path,
            status = when (call_type) {
                "missed" -> "missed"
                else -> "completed"
            },
            summary = call_summary,
            isImportant = is_important == 1,
            tags = tags ?: emptyList()
        )
    }
    
    private fun parseTimestamp(timeString: String): Instant {
        return try {
            Instant.parse(timeString)
        } catch (e: Exception) {
            Instant.fromEpochMilliseconds(Clock.System.now().toEpochMilliseconds())
        }
    }
}

@Serializable
data class CallDetailResponse(
    val id: Int,
    val userUuid: String,
    val callType: String,
    val phoneNumber: String,
    val contactName: String? = null,
    val callDuration: Int,
    val callStartTime: String,
    val callEndTime: String? = null,
    val callSummary: String? = null,
    val callContent: String? = null,
    val audioFilePath: String? = null, // 녹음 파일 경로
    val audioFileName: String? = null, // 녹음 파일 이름
    val isImportant: Int = 0, // 서버에서 0/1로 반환
    val tags: List<String>? = null, // null 허용
    val createdAt: String,
    val updatedAt: String
) {
    fun toCallRecord(): CallRecord {
        println("🔄 CallDetailResponse -> CallRecord 변환:")
        println("- id: $id")
        println("- callSummary: $callSummary")
        println("- callContent: $callContent")
        println("- audioFilePath: $audioFilePath")
        println("- audioFileName: $audioFileName")
        
        return CallRecord(
            id = id.toString(),
            phoneNumber = phoneNumber,
            counselorName = contactName ?: "상담사",
            duration = callDuration.toLong(),
            timestamp = parseTimestamp(callStartTime),
            audioFileUrl = audioFilePath,
            status = when (callType) {
                "missed" -> "missed"
                else -> "completed"
            },
            summary = callSummary,
            transcription = callContent,
            callContent = callContent,
            isImportant = isImportant == 1,
            tags = tags ?: emptyList()
        )
    }
    
    private fun parseTimestamp(timeString: String): Instant {
        return try {
            Instant.parse(timeString)
        } catch (e: Exception) {
            Instant.fromEpochMilliseconds(Clock.System.now().toEpochMilliseconds())
        }
    }
}

@Serializable
data class Pagination(
    val currentPage: Int,
    val totalPages: Int,
    val totalRecords: Int,
    val limit: Int
)

@Serializable
data class LoginRequest(
    val uuid: String,
    val deviceInfo: DeviceInfo,
    val pushToken: String?,
    val userAgent: String,
    val phoneNumber: String? = null // 기기의 전화번호
)

@Serializable
data class UserInfo(
    val uuid: String? = null,
    val userUuid: String? = null,
    val id: String? = null
)

@Serializable
data class LoginResponse(
    val success: Boolean = true,
    val message: String = "",
    val userUuid: String? = null,
    val isNewUser: Boolean = false,
    val accessToken: String? = null,
    // 서버에서 다른 필드명을 사용할 수 있으므로 추가
    val uuid: String? = null,
    val token: String? = null,
    val user: UserInfo? = null,  // 객체로 변경
    val data: String? = null
)

@Serializable
data class CallRequest(
    val userUuid: String,
    val callType: String, // "outgoing", "incoming"
    val phoneNumber: String,
    val callStartTime: String, // ISO 8601 format
    val callEndTime: String, // ISO 8601 format
    val callDuration: Int, // seconds
    val contactName: String? = null,
    val callSummary: String? = null,
    val callContent: String? = null,
    val isImportant: Boolean = false,
    val tags: List<String> = emptyList()
)

@Serializable
data class CallResponse(
    val success: Boolean = true,
    val message: String = "",
    val callId: String? = null,
    // 서버 응답에 맞는 실제 필드들
    val id: Int? = null,
    val userUuid: String? = null,
    val callType: String? = null,
    val phoneNumber: String? = null,
    val contactName: String? = null,
    val callDuration: Int? = null,
    val callStartTime: String? = null,
    val callEndTime: String? = null,
    val callSummary: String? = null,
    val isImportant: Int? = null, // 서버에서 0/1로 반환
    val tags: List<String>? = null,
    val createdAt: String? = null
)

@Serializable
data class CallEndRequest(
    val callEndTime: String, // ISO 8601 format
    val callDuration: Int    // seconds
)

@Serializable
data class CallEndResponse(
    val success: Boolean = true,
    val message: String = "",
    // 서버에서 다른 필드명을 사용할 수 있으므로 추가
    val data: String? = null,
    val result: String? = null
)

// 실패한 통화 종료 요청을 로컬에 저장하기 위한 데이터 클래스
@Serializable
data class PendingCallEndRequest(
    val callId: String,
    val request: CallEndRequest,
    val timestamp: String,
    val retryCount: Int = 0
)
