package co.kr.imokapp.headream.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class CallRecord(
    val id: String,
    val phoneNumber: String,
    val counselorName: String, // 상담사 이름
    val duration: Long, // 통화 시간 (초)
    val timestamp: Instant,
    val audioFileUrl: String? = null,
    val transcription: String? = null, // 통화 전체 내용 (STT 결과)
    val summary: String? = null, // 통화 요약
    val callContent: String? = null, // 통화 전체 내용 (서버 필드명)
    val status: String = "completed", // "completed", "missed", "ongoing"
    val isImportant: Boolean = false,
    val tags: List<String> = emptyList()
) {
    val formattedDateTime: String
        get() {
            // 더 나은 날짜 포맷팅
            val dateStr = timestamp.toString()
            return try {
                val month = dateStr.substring(5, 7)
                val day = dateStr.substring(8, 10)
                val hour = dateStr.substring(11, 13)
                val minute = dateStr.substring(14, 16)
                "${month}월 ${day}일 ${hour}:${minute}"
            } catch (e: Exception) {
                dateStr.substring(0, 16)
            }
        }
    
    val formattedDuration: String
        get() {
            if (duration == 0L) return "0초"
            val minutes = duration / 60
            val seconds = duration % 60
            return if (minutes > 0) {
                "${minutes}분 ${seconds}초"
            } else {
                "${seconds}초"
            }
        }
    
    val formattedTime: String
        get() {
            val dateStr = timestamp.toString()
            return try {
                val hour = dateStr.substring(11, 13).toInt()
                val minute = dateStr.substring(14, 16)
                val amPm = if (hour < 12) "오전" else "오후"
                val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
                "$amPm $displayHour:$minute"
            } catch (e: Exception) {
                "오후 4:16" // 기본값
            }
        }
}

@Serializable
enum class CallStatus {
    DIALING,
    CONNECTED,
    RECORDING,
    COMPLETED,
    FAILED
}

@Serializable
data class CallHistory(
    val phoneNumber: String,
    val calls: List<CallRecord>,
    val totalCalls: Int,
    val lastCallTime: Instant?
)

@Serializable
data class UploadCallRequest(
    val phoneNumber: String,
    val duration: Long,
    val timestamp: Instant,
    val audioData: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as UploadCallRequest

        if (phoneNumber != other.phoneNumber) return false
        if (duration != other.duration) return false
        if (timestamp != other.timestamp) return false
        if (!audioData.contentEquals(other.audioData)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = phoneNumber.hashCode()
        result = 31 * result + duration.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + audioData.contentHashCode()
        return result
    }
}

