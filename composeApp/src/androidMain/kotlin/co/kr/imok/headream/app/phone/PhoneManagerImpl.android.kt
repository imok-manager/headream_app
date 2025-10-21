package co.kr.imok.headream.app.phone

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import co.kr.imok.headream.app.data.CallStatus
import java.io.File
import java.io.IOException
import java.util.concurrent.Executor
import kotlin.coroutines.resume

class PhoneManagerImpl(private val context: Context) : PhoneManager {
    
    private var mediaRecorder: MediaRecorder? = null
    private var recordingFile: File? = null
    private var currentCallStatus = CallStatus.COMPLETED
    private var telephonyManager: TelephonyManager? = null
    
    init {
        telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        setupPhoneStateListener()
    }
    
    private fun setupPhoneStateListener() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12+ (API 31+)에서는 TelephonyCallback 사용
                setupTelephonyCallback()
            } else {
                // Android 11 이하에서는 PhoneStateListener 사용
                setupLegacyPhoneStateListener()
            }
        } catch (e: Exception) {
            println("❌ PhoneStateListener 설정 실패: ${e.message}")
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.S)
    private fun setupTelephonyCallback() {
        try {
            val executor = ContextCompat.getMainExecutor(context)
            val callback = object : TelephonyCallback(), TelephonyCallback.CallStateListener {
                override fun onCallStateChanged(state: Int) {
                    val oldStatus = currentCallStatus
                    currentCallStatus = when (state) {
                        TelephonyManager.CALL_STATE_IDLE -> CallStatus.COMPLETED
                        TelephonyManager.CALL_STATE_RINGING -> CallStatus.DIALING
                        TelephonyManager.CALL_STATE_OFFHOOK -> CallStatus.CONNECTED
                        else -> CallStatus.COMPLETED
                    }
                    if (oldStatus != currentCallStatus) {
                        println("📞 TelephonyCallback: 통화 상태 변화 $oldStatus → $currentCallStatus (state: $state)")
                    }
                }
            }
            telephonyManager?.registerTelephonyCallback(executor, callback)
            println("✅ TelephonyCallback 등록 성공 (Android 12+)")
        } catch (e: Exception) {
            println("❌ TelephonyCallback 등록 실패: ${e.message}")
        }
    }
    
    private fun setupLegacyPhoneStateListener() {
        try {
            @Suppress("DEPRECATION")
            telephonyManager?.listen(object : PhoneStateListener() {
                @Suppress("DEPRECATION")
                override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                    val oldStatus = currentCallStatus
                    currentCallStatus = when (state) {
                        TelephonyManager.CALL_STATE_IDLE -> CallStatus.COMPLETED
                        TelephonyManager.CALL_STATE_RINGING -> CallStatus.DIALING
                        TelephonyManager.CALL_STATE_OFFHOOK -> CallStatus.CONNECTED
                        else -> CallStatus.COMPLETED
                    }
                    if (oldStatus != currentCallStatus) {
                        println("📞 PhoneStateListener: 통화 상태 변화 $oldStatus → $currentCallStatus (state: $state)")
                    }
                }
            }, PhoneStateListener.LISTEN_CALL_STATE)
            println("✅ PhoneStateListener 등록 성공 (Android 11 이하)")
        } catch (e: Exception) {
            println("❌ PhoneStateListener 등록 실패: ${e.message}")
        }
    }
    
    override suspend fun makeCall(phoneNumber: String): Result<Unit> {
        return try {
            // 전화 권한 확인
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) 
                != PackageManager.PERMISSION_GRANTED) {
                return Result.failure(SecurityException("전화 권한이 필요합니다"))
            }
            
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$phoneNumber")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            println("📞 PhoneManager: 전화 걸기 시작 - $phoneNumber")
            context.startActivity(intent)
            currentCallStatus = CallStatus.DIALING
            println("📞 PhoneManager: 통화 상태를 DIALING으로 설정")
            
            Result.success(Unit)
        } catch (e: Exception) {
            currentCallStatus = CallStatus.FAILED
            Result.failure(e)
        }
    }
    
    override suspend fun startRecording(): Result<Unit> {
        return try {
            // 녹음 권한 확인
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) 
                != PackageManager.PERMISSION_GRANTED) {
                return Result.failure(SecurityException("녹음 권한이 필요합니다"))
            }
            
            // 녹음 파일 생성
            recordingFile = File(context.cacheDir, "call_recording_${System.currentTimeMillis()}.3gp")
            
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.VOICE_CALL)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(recordingFile?.absolutePath)
                
                prepare()
                start()
            }
            
            currentCallStatus = CallStatus.RECORDING
            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun stopRecording(): Result<ByteArray?> {
        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            
            val audioData = recordingFile?.readBytes()
            recordingFile?.delete() // 임시 파일 삭제
            recordingFile = null
            
            Result.success(audioData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun isRecordingSupported(): Boolean {
        return true // Android에서는 통화 녹음 지원
    }
    
    override fun getCurrentCallStatus(): CallStatus {
        // 실제 TelephonyManager 상태도 확인해서 동기화
        try {
            val actualState = telephonyManager?.callState
            val actualStatus = when (actualState) {
                TelephonyManager.CALL_STATE_IDLE -> CallStatus.COMPLETED
                TelephonyManager.CALL_STATE_RINGING -> CallStatus.DIALING
                TelephonyManager.CALL_STATE_OFFHOOK -> CallStatus.CONNECTED
                else -> currentCallStatus // 알 수 없는 상태면 기존 상태 유지
            }
            
            // 상태가 다르면 동기화하고 로그 출력
            if (actualStatus != currentCallStatus) {
                println("📞 상태 동기화: $currentCallStatus → $actualStatus (TelephonyManager state: $actualState)")
                currentCallStatus = actualStatus
            }
        } catch (e: Exception) {
            println("❌ TelephonyManager 상태 확인 실패: ${e.message}")
        }
        
        return currentCallStatus
    }
}

actual fun createPhoneManager(): PhoneManager {
    // Android에서는 Context가 필요하므로 별도 처리 필요
    throw UnsupportedOperationException("Android에서는 AppModule을 통해 PhoneManager를 생성하세요")
}
