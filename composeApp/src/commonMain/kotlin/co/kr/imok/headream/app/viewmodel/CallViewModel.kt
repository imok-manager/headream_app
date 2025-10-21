package co.kr.imok.headream.app.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import co.kr.imok.headream.app.data.*
import co.kr.imok.headream.app.network.ApiClient
import co.kr.imok.headream.app.phone.PhoneManager
import co.kr.imok.headream.app.platform.PhoneNumberProvider
import co.kr.imok.headream.app.platform.DeviceInfoProvider

class CallViewModel(
    private val phoneManager: PhoneManager,
    private val apiClient: ApiClient,
    private val haedreamApiClient: co.kr.imok.headream.app.network.HaedreamApiClient,
    private val userManager: co.kr.imok.headream.app.data.UserManager,
    private val phoneNumberProvider: PhoneNumberProvider
) : ViewModel() {
    
    var uiState by mutableStateOf(CallUiState())
        private set
    
    // 실패한 통화 종료 요청들을 저장할 리스트
    private val pendingCallEndRequests = mutableListOf<PendingCallEndRequest>()
    
    // 통화 추적을 위한 변수들
    private var currentCallId: String? = null
    private var callStartTime: kotlinx.datetime.Instant? = null
    private var callConnectedTime: kotlinx.datetime.Instant? = null // 실제 연결된 시간
    private var isCallActive: Boolean = false
    
    var callHistory by mutableStateOf<List<CallRecord>>(emptyList())
        private set
    
    // 페이징 관련 상태
    private var currentPage = 1
    var isLoadingMore by mutableStateOf(false)
        private set
    private var hasMoreData = true
    private val pageSize = 10
    var totalRecords by mutableStateOf(0)
        private set
    
    // 실패한 통화 요청 로컬 저장
    private val pendingCallRequests = mutableListOf<CallRequest>()
    
    var callHistories by mutableStateOf<List<CallHistory>>(emptyList())
        private set
    
    fun startCall(phoneNumber: String) {
        viewModelScope.launch {
            uiState = uiState.copy(
                phoneNumber = phoneNumber,
                isLoading = true, 
                errorMessage = null
            )
            
            // 1. 먼저 API에 통화 시작 정보 전송
            try {
                val devicePhoneNumber = getDevicePhoneNumber()
                
                // UUID 가져오기 - 여러 방법 시도
                val userUuid = try {
                    // 방법 1: UserManager에서 가져오기
                    val currentUser = userManager.currentUser.value
                    val userManagerUuid = currentUser?.uuid
                    
                    // 방법 2: UserManager의 userUuid StateFlow에서 가져오기
                    val stateFlowUuid = userManager.userUuid.value
                    
                    // 방법 3: DeviceInfoProvider에서 기기 고유 UUID 가져오기
                    val deviceInfoProvider = DeviceInfoProvider()
                    val deviceUuid = deviceInfoProvider.getOrCreateUUID()
                    
                    println("🔍 UUID 확인:")
                    println("- UserManager.currentUser.uuid: $userManagerUuid")
                    println("- UserManager.userUuid.value: $stateFlowUuid")
                    println("- DeviceInfoProvider.getOrCreateUUID(): $deviceUuid")
                    
                    // 우선순위: UserManager UUID -> StateFlow UUID -> Device UUID
                    userManagerUuid ?: stateFlowUuid ?: deviceUuid
                } catch (e: Exception) {
                    println("❌ UUID 가져오기 실패: ${e.message}")
                    // 최후의 수단: DeviceInfoProvider로 다시 시도
                    try {
                        val deviceInfoProvider = DeviceInfoProvider()
                        deviceInfoProvider.getOrCreateUUID()
                    } catch (ex: Exception) {
                        "fallback-uuid-${Clock.System.now().toEpochMilliseconds()}"
                    }
                }
                
                // 통화 시작 시간 기록 (로컬에서만)
                val startTime = Clock.System.now()
                callStartTime = startTime
                
                println("📞 통화 시작 준비:")
                println("- userUuid: $userUuid")
                println("- phoneNumber: ${devicePhoneNumber ?: "unknown"}")
                println("- callStartTime: $startTime")
                println("📞 API 호출은 통화 종료 시 한 번에 처리됩니다")
                
                isCallActive = true
                
                // 통화 상태 모니터링 시작
                startCallStateMonitoring()
            } catch (e: Exception) {
                println("💥 통화 API 오류: ${e.message}")
            }
            
            // 2. 실제 전화 걸기
            phoneManager.makeCall(phoneNumber)
                .onSuccess {
                    uiState = uiState.copy(
                        isLoading = false,
                        callStatus = phoneManager.getCurrentCallStatus()
                    )
                    
                    // 통화 녹음 시작 (Android에서만)
                    if (phoneManager.isRecordingSupported()) {
                        startRecording()
                    }
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "전화 걸기에 실패했습니다"
                    )
                }
        }
    }
    
    fun makeCall() {
        startCall(uiState.phoneNumber)
    }
    
    private fun startRecording() {
        viewModelScope.launch {
            phoneManager.startRecording()
                .onSuccess {
                    uiState = uiState.copy(
                        isRecording = true,
                        callStatus = CallStatus.RECORDING
                    )
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        errorMessage = "녹음 시작에 실패했습니다: ${error.message}"
                    )
                }
        }
    }
    
    fun endCall() {
        viewModelScope.launch {
            var audioData: ByteArray? = null
            
            // 녹음 중이면 녹음 중지
            if (uiState.isRecording) {
                phoneManager.stopRecording()
                    .onSuccess { data ->
                        audioData = data
                        uiState = uiState.copy(isRecording = false)
                    }
                    .onFailure { error ->
                        uiState = uiState.copy(
                            errorMessage = "녹음 중지에 실패했습니다: ${error.message}"
                        )
                    }
            }
            
            // 통화 상태 모니터링 중지
            isCallActive = false
            
            // 통화 종료 API 호출
            sendCallEndToServer()
            
            // 통화 기록을 서버에 업로드
            audioData?.let { data ->
                uploadCallRecord(data)
            }
            
            uiState = uiState.copy(
                callStatus = CallStatus.COMPLETED,
                isRecording = false
            )
        }
    }
    
    private fun sendCallEndToServer() {
        viewModelScope.launch {
            try {
                val startTime = callStartTime
                val endTime = Clock.System.now()
                
                if (startTime == null) {
                    println("⚠️ 시작 시간이 없어서 통화 기록을 저장할 수 없습니다")
                    return@launch
                }
                
                // 실제 연결된 시간이 있으면 연결 시간부터 계산, 없으면 0초
                val durationSeconds = if (callConnectedTime != null) {
                    (endTime - callConnectedTime!!).inWholeSeconds.toInt()
                } else {
                    println("⚠️ 통화가 연결되지 않았음 - 통화 시간 0초로 설정")
                    0
                }
                
                println("⏱️ 통화 시간 계산:")
                println("- 통화 시작 시간: $startTime")
                println("- 통화 연결 시간: $callConnectedTime")
                println("- 통화 종료 시간: $endTime")
                println("- 실제 통화 시간: ${durationSeconds}초")
                
                // 5초 이상 통화한 경우에만 API 호출
                if (durationSeconds >= 5) {
                    println("✅ 통화 시간이 5초 이상이므로 API 호출합니다")
                    
                    // 사용자 UUID 가져오기
                    val userUuid = try {
                        userManager.userUuid.value ?: throw Exception("User UUID is null")
                    } catch (e: Exception) {
                        println("❌ UUID 가져오기 실패: ${e.message}")
                        return@launch
                    }
                    
                    // 기기 전화번호 가져오기
                    val devicePhoneNumber = try {
                        phoneNumberProvider.getPhoneNumber()
                    } catch (e: Exception) {
                        println("⚠️ 기기 전화번호 가져오기 실패: ${e.message}")
                        null
                    }
                    
                    val callRequest = CallRequest(
                        userUuid = userUuid,
                        callType = "outgoing",
                        phoneNumber = devicePhoneNumber ?: "unknown",
                        callStartTime = startTime.toString(),
                        callEndTime = endTime.toString(),
                        callDuration = durationSeconds
                    )
                
                    // 통화 종료 후 네트워크 안정화를 위해 3초 대기
                    println("⏳ 네트워크 안정화를 위해 3초 대기 중...")
                    kotlinx.coroutines.delay(3000)
                    
                    println("📞 통화 기록 API 호출:")
                    println("- userUuid: ${callRequest.userUuid}")
                    println("- phoneNumber: ${callRequest.phoneNumber}")
                    println("- callStartTime: ${callRequest.callStartTime}")
                    println("- callEndTime: ${callRequest.callEndTime}")
                    println("- callDuration: ${callRequest.callDuration}초")
                    
                    // 통화 기록 API 호출 (시작/종료 정보 모두 포함)
                    val result = haedreamApiClient.startCall(callRequest)
                    result.onSuccess { callResponse ->
                        println("✅ 통화 기록 저장 성공: ${callResponse.message}")
                        println("📱 Call ID: ${callResponse.callId}")
                        
                        // 통화 기록 새로고침
                        loadCallHistory()
                        
                    }.onFailure { error ->
                        println("❌ 통화 기록 저장 실패: ${error.message}")
                        // 실패한 요청을 로컬에 저장하여 나중에 재시도
                        savePendingCallRequest(callRequest)
                        
                        // 10초 후 재시도 스케줄링
                        scheduleRetryCallRequest(10000)
                    }
                } else {
                    println("⚠️ 통화 시간이 5초 미만이므로 API 호출하지 않습니다 (${durationSeconds}초)")
                }
                
                // 통화 정보 초기화
                currentCallId = null
                callStartTime = null
                callConnectedTime = null
                isCallActive = false
                
            } catch (e: Exception) {
                println("💥 통화 종료 API 오류: ${e.message}")
            }
        }
    }
    
    private fun startCallStateMonitoring() {
        viewModelScope.launch {
            println("🔍 통화 상태 모니터링 시작")
            
            // 통화 시작 후 3초 대기 (PhoneManager 상태 안정화)
            kotlinx.coroutines.delay(3000)
            println("🔍 통화 상태 모니터링 실제 시작 (3초 대기 후)")
            
            var monitoringTime = 3 // 이미 3초 대기했음
            val maxMonitoringTime = 300 // 5분 최대 모니터링
            var lastStatus = CallStatus.DIALING
            var sameStatusCount = 0
            val minMonitoringTimeBeforeEnd = 10 // 최소 10초는 모니터링 후 종료 감지
            
            while (isCallActive && monitoringTime < maxMonitoringTime) {
                try {
                    val currentStatus = phoneManager.getCurrentCallStatus()
                    
                    // 상태 변화 감지
                    if (currentStatus == lastStatus) {
                        sameStatusCount++
                    } else {
                        println("📞 통화 상태 변화: $lastStatus → $currentStatus")
                        
                        // CONNECTED 상태가 되면 실제 연결 시간 기록
                        if (currentStatus == CallStatus.CONNECTED && callConnectedTime == null) {
                            callConnectedTime = kotlinx.datetime.Clock.System.now()
                            println("✅ 통화 연결됨! 연결 시간: $callConnectedTime")
                        }
                        
                        lastStatus = currentStatus
                        sameStatusCount = 0
                    }
                    
                    // 5초마다 로그 출력 (너무 많은 로그 방지)
                    if (monitoringTime % 5 == 0) {
                        println("📞 현재 통화 상태: $currentStatus (${monitoringTime}초)")
                    }
                    
                    // 최소 모니터링 시간 후에만 종료 감지
                    if (monitoringTime >= minMonitoringTimeBeforeEnd) {
                        // 통화가 종료되었는지 확인
                        if (currentStatus == CallStatus.COMPLETED || currentStatus == CallStatus.FAILED) {
                            println("📞 통화 종료 감지! 자동으로 종료 API 호출 (${monitoringTime}초 후)")
                            isCallActive = false
                            sendCallEndToServer()
                            break
                        }
                    } else {
                        println("📞 최소 모니터링 시간 대기 중... (${monitoringTime}/${minMonitoringTimeBeforeEnd}초)")
                    }
                    
                    // DIALING 상태가 30초 이상 지속되면 통화 실패로 간주
                    if (currentStatus == CallStatus.DIALING && sameStatusCount >= 30) {
                        println("📞 DIALING 상태 30초 초과 - 통화 실패로 간주")
                        isCallActive = false
                        sendCallEndToServer()
                        break
                    }
                    
                    // 1초마다 상태 확인
                    kotlinx.coroutines.delay(1000)
                    monitoringTime++
                    
                } catch (e: Exception) {
                    println("❌ 통화 상태 모니터링 오류: ${e.message}")
                    kotlinx.coroutines.delay(2000) // 오류 시 2초 대기
                    monitoringTime += 2
                }
            }
            
            // 최대 시간 초과 시 자동 종료
            if (monitoringTime >= maxMonitoringTime && isCallActive) {
                println("📞 모니터링 시간 초과 (5분) - 자동 종료")
                isCallActive = false
                sendCallEndToServer()
            }
            
            println("🔍 통화 상태 모니터링 종료")
        }
    }
    
    private fun uploadCallRecord(audioData: ByteArray) {
        viewModelScope.launch {
            val request = UploadCallRequest(
                phoneNumber = uiState.phoneNumber,
                duration = 0L, // 실제로는 통화 시간을 계산해야 함
                timestamp = Clock.System.now(),
                audioData = audioData
            )
            
            apiClient.uploadCallRecording(request)
                .onSuccess { callRecord ->
                    // 업로드 성공 후 히스토리 새로고침
                    loadCallHistory()
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        errorMessage = "통화 기록 업로드에 실패했습니다: ${error.message}"
                    )
                }
        }
    }
    
    fun loadCallHistory(phoneNumber: String? = null, loadMore: Boolean = false) {
        viewModelScope.launch {
            // 더 불러오기가 아니면 첫 페이지부터 시작
            if (!loadMore) {
                println("📋 첫 페이지 로드 시작")
                currentPage = 1
                hasMoreData = true
                uiState = uiState.copy(isLoadingHistory = true)
            } else {
                println("📋 더 불러오기 요청: currentPage=$currentPage, isLoadingMore=$isLoadingMore, hasMoreData=$hasMoreData")
                if (isLoadingMore || !hasMoreData) {
                    println("📋 이미 로딩 중이거나 더 이상 데이터가 없습니다")
                    return@launch
                }
                isLoadingMore = true
                println("📋 더 불러오기 시작: 페이지 $currentPage")
            }
            
            try {
                // 로그인은 HaedreamApp에서 이미 처리하므로 여기서는 제거
                // 사용자가 로그인되어 있지 않으면 에러 표시
                if (!userManager.isLoggedIn()) {
                    uiState = uiState.copy(
                        isLoadingHistory = false,
                        errorMessage = "로그인이 필요합니다. 앱을 다시 시작해주세요."
                    )
                    return@launch
                }
                
                val userUuid = userManager.userUuid.value
                if (userUuid != null) {
                    // 실제 API 호출 (페이징 지원)
                    haedreamApiClient.getCallRecords(
                        userUuid = userUuid,
                        page = currentPage,
                        limit = pageSize
                    ).onSuccess { response ->
                        val newRecords = response.records.map { it.toCallRecord() }
                        
                        // 전체 레코드 수 업데이트
                        totalRecords = response.pagination.totalRecords
                        println("📋 전체 레코드 수 업데이트: $totalRecords")
                        
                        if (loadMore) {
                            // 더 불러오기: 기존 데이터에 추가
                            callHistory = callHistory + newRecords
                            isLoadingMore = false
                            println("📋 페이지 ${currentPage} 로드 완료: ${newRecords.size}개 추가 (총 ${callHistory.size}개)")
                        } else {
                            // 새로고침: 기존 데이터 교체
                            callHistory = newRecords
                            uiState = uiState.copy(isLoadingHistory = false)
                            println("📋 첫 페이지 로드 완료: ${newRecords.size}개")
                        }
                        
                        // 더 이상 데이터가 없는지 확인
                        hasMoreData = newRecords.size >= pageSize
                        println("📋 페이징 상태 업데이트: newRecords.size=${newRecords.size}, pageSize=$pageSize, hasMoreData=$hasMoreData")
                        
                        if (loadMore) {
                            // 더 불러오기 완료 후 다음 페이지로 증가
                            currentPage++
                            println("📋 다음 페이지로 증가: currentPage=$currentPage")
                        } else {
                            // 첫 페이지 로드 후 다음 페이지 준비
                            currentPage = 2
                            println("📋 첫 페이지 완료, 다음 페이지 준비: currentPage=$currentPage")
                        }
                        
                    }.onFailure { error ->
                        // API 실패 시 처리
                        if (loadMore) {
                            isLoadingMore = false
                        } else {
                            callHistory = getSampleCallHistory()
                            uiState = uiState.copy(
                                isLoadingHistory = false,
                                errorMessage = "서버 연결 실패, 샘플 데이터를 표시합니다: ${error.message}"
                            )
                        }
                    }
                } else {
                    // UUID가 없으면 샘플 데이터 사용
                    callHistory = getSampleCallHistory()
                    uiState = uiState.copy(isLoadingHistory = false)
                }
            } catch (error: Exception) {
                // 예외 발생 시 샘플 데이터 사용
                callHistory = getSampleCallHistory()
                uiState = uiState.copy(
                    isLoadingHistory = false,
                    errorMessage = "데이터 로드 중 오류 발생, 샘플 데이터를 표시합니다: ${error.message}"
                )
            }
        }
    }
    
    // 더 많은 통화 기록 불러오기
    fun loadMoreCallHistory() {
        println("📋 더 많은 통화 기록 요청")
        loadCallHistory(loadMore = true)
    }
    
    private fun getSampleCallHistory(): List<CallRecord> {
        return listOf(
            CallRecord(
                id = "1",
                phoneNumber = "010-8745-8123",
                counselorName = "나혜리",
                duration = 227, // 3분 47초
                timestamp = Clock.System.now(),
                status = "completed",
                summary = "기술 상담 관련 통화. 앱 사용법과 기능에 대한 문의사항을 해결했습니다.",
                isImportant = true,
                tags = listOf("기술상담", "앱사용법", "중요")
            ),
            CallRecord(
                id = "2", 
                phoneNumber = "010-8745-8123",
                counselorName = "나혜리",
                duration = 145, // 2분 25초
                timestamp = Clock.System.now(),
                status = "completed",
                summary = "일반 문의사항에 대한 답변을 제공했습니다.",
                isImportant = false,
                tags = listOf("일반문의")
            ),
            CallRecord(
                id = "3",
                phoneNumber = "010-8745-8123", 
                counselorName = "나혜리",
                duration = 0,
                timestamp = Clock.System.now(),
                status = "missed",
                isImportant = false,
                tags = listOf("부재중")
            ),
            CallRecord(
                id = "4",
                phoneNumber = "010-8745-8123",
                counselorName = "나혜리", 
                duration = 312, // 5분 12초
                timestamp = Clock.System.now(),
                status = "completed",
                summary = "서비스 이용 관련 상세 안내 및 추가 기능 설명을 진행했습니다.",
                isImportant = true,
                tags = listOf("서비스안내", "기능설명", "상세상담")
            )
        )
    }
    
    fun getCallDetail(callId: String): CallRecord? {
        return callHistory.find { it.id == callId }
    }
    
    fun loadCallDetail(callId: String, onResult: (CallRecord?) -> Unit) {
        viewModelScope.launch {
            try {
                val callIdInt = callId.toIntOrNull()
                if (callIdInt != null) {
                    haedreamApiClient.getCallDetail(callIdInt)
                        .onSuccess { response ->
                            onResult(response.toCallRecord())
                        }
                        .onFailure {
                            // API 실패 시 로컬 데이터에서 찾기
                            onResult(getCallDetail(callId))
                        }
                } else {
                    // ID가 숫자가 아니면 로컬 데이터에서 찾기
                    onResult(getCallDetail(callId))
                }
            } catch (e: Exception) {
                onResult(getCallDetail(callId))
            }
        }
    }
    
    fun clearError() {
        uiState = uiState.copy(errorMessage = null)
    }
    
    fun getHttpClient() = apiClient.httpClient
    
    fun getUserManager() = userManager
    
    fun forceEndCall() {
        println("🔴 강제 통화 종료 요청")
        isCallActive = false
        sendCallEndToServer()
    }
    
    fun getDevicePhoneNumber(): String? {
        return try {
            val phoneNumberProvider = PhoneNumberProvider()
            val phoneNumber = phoneNumberProvider.getPhoneNumber()
            println("📱 CallViewModel - 기기 전화번호: ${phoneNumber ?: "없음"}")
            phoneNumber
        } catch (e: Exception) {
            println("❌ CallViewModel - 전화번호 가져오기 실패: ${e.message}")
            null
        }
    }
    
    // 실패한 통화 종료 요청을 로컬에 저장
    private fun savePendingCallEndRequest(callId: String, request: CallEndRequest) {
        val pendingRequest = PendingCallEndRequest(
            callId = callId,
            request = request,
            timestamp = kotlinx.datetime.Clock.System.now().toString(),
            retryCount = 0
        )
        
        pendingCallEndRequests.add(pendingRequest)
        println("💾 로컬 저장 완료: ${pendingCallEndRequests.size}개의 대기 중인 요청")
    }
    
    // 일정 시간 후 재시도 스케줄링
    private fun scheduleRetryCallEndRequest(delayMs: Long) {
        viewModelScope.launch {
            println("⏰ ${delayMs/1000}초 후 재시도 예약됨")
            kotlinx.coroutines.delay(delayMs)
            retryPendingCallEndRequests()
        }
    }
    
    // 앱이 포그라운드로 돌아올 때 호출되는 메서드
    fun onAppResumed() {
        viewModelScope.launch {
            if (pendingCallEndRequests.isNotEmpty()) {
                println("📱 앱이 포그라운드로 돌아옴 - ${pendingCallEndRequests.size}개의 대기 중인 종료 요청 재시도")
                retryPendingCallEndRequests()
            }
            if (pendingCallRequests.isNotEmpty()) {
                println("📱 앱이 포그라운드로 돌아옴 - ${pendingCallRequests.size}개의 대기 중인 통화 요청 재시도")
                retryPendingCallRequests()
            }
        }
    }
    
    // 대기 중인 통화 종료 요청들을 재시도
    private suspend fun retryPendingCallEndRequests() {
        if (pendingCallEndRequests.isEmpty()) {
            println("📭 재시도할 요청이 없습니다")
            return
        }
        
        println("🔄 ${pendingCallEndRequests.size}개의 대기 중인 요청 재시도 시작")
        
        val requestsToRetry = pendingCallEndRequests.toList()
        pendingCallEndRequests.clear()
        
        for (pendingRequest in requestsToRetry) {
            try {
                println("🔄 재시도 중: Call ID ${pendingRequest.callId} (시도 ${pendingRequest.retryCount + 1})")
                
                val result = haedreamApiClient.endCall(pendingRequest.callId, pendingRequest.request)
                result.onSuccess { response ->
                    println("✅ 재시도 성공: Call ID ${pendingRequest.callId}")
                }.onFailure { error ->
                    println("❌ 재시도 실패: Call ID ${pendingRequest.callId} - ${error.message}")
                    
                    // 최대 3번까지 재시도
                    if (pendingRequest.retryCount < 2) {
                        val updatedRequest = pendingRequest.copy(retryCount = pendingRequest.retryCount + 1)
                        pendingCallEndRequests.add(updatedRequest)
                        println("📝 재시도 대기열에 다시 추가: ${pendingRequest.retryCount + 1}/3")
                    } else {
                        println("❌ 최대 재시도 횟수 초과: Call ID ${pendingRequest.callId}")
                    }
                }
                
                // 각 요청 사이에 1초 대기
                kotlinx.coroutines.delay(1000)
                
            } catch (e: Exception) {
                println("💥 재시도 중 오류: ${e.message}")
            }
        }
        
        // 아직 실패한 요청이 있으면 다시 스케줄링
        if (pendingCallEndRequests.isNotEmpty()) {
            println("🔄 ${pendingCallEndRequests.size}개 요청이 남아있음 - 30초 후 다시 재시도")
            scheduleRetryCallEndRequest(30000) // 30초 후 재시도
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        apiClient.close()
    }
    
    // 실패한 통화 요청 로컬 저장
    private fun savePendingCallRequest(callRequest: CallRequest) {
        pendingCallRequests.add(callRequest)
        println("💾 로컬 저장 완료: ${pendingCallRequests.size}개의 대기 중인 요청")
    }
    
    // 일정 시간 후 재시도 스케줄링
    private fun scheduleRetryCallRequest(delayMs: Long) {
        viewModelScope.launch {
            println("⏰ ${delayMs/1000}초 후 재시도 예약됨")
            kotlinx.coroutines.delay(delayMs)
            retryPendingCallRequests()
        }
    }
    
    
    // 대기 중인 통화 요청들을 재시도
    private suspend fun retryPendingCallRequests() {
        if (pendingCallRequests.isEmpty()) {
            println("📭 재시도할 요청이 없습니다")
            return
        }
        
        println("🔄 ${pendingCallRequests.size}개의 대기 중인 요청 재시도 시작")
        
        val requestsToRetry = pendingCallRequests.toList()
        pendingCallRequests.clear()
        
        for (callRequest in requestsToRetry) {
            try {
                println("🔄 재시도 중: ${callRequest.userUuid}")
                
                val result = haedreamApiClient.startCall(callRequest)
                result.onSuccess { callResponse ->
                    println("✅ 재시도 성공: ${callResponse.message}")
                    // 통화 기록 새로고침
                    loadCallHistory()
                }.onFailure { error ->
                    println("❌ 재시도 실패: ${error.message}")
                    // 다시 로컬에 저장
                    pendingCallRequests.add(callRequest)
                }
                
                // 각 요청 사이에 1초 대기
                kotlinx.coroutines.delay(1000)
                
            } catch (e: Exception) {
                println("💥 재시도 중 오류: ${e.message}")
                // 다시 로컬에 저장
                pendingCallRequests.add(callRequest)
            }
        }
        
        // 아직 실패한 요청이 있으면 다시 스케줄링
        if (pendingCallRequests.isNotEmpty()) {
            println("🔄 ${pendingCallRequests.size}개 요청이 여전히 실패 - 10초 후 재시도")
            scheduleRetryCallRequest(10000)
        }
    }
}

data class CallUiState(
    val phoneNumber: String = "010-3044-0428", // 고정 전화번호
    val isLoading: Boolean = false,
    val isLoadingHistory: Boolean = false,
    val isRecording: Boolean = false,
    val callStatus: CallStatus = CallStatus.COMPLETED,
    val errorMessage: String? = null
)
