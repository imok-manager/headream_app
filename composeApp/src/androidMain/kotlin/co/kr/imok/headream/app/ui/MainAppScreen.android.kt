package co.kr.imok.headream.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import kotlinx.coroutines.delay
import co.kr.imok.headream.app.audio.AudioPlaybackManager
import co.kr.imok.headream.app.audio.initializeAudioWithContext
import co.kr.imok.headream.app.data.CallRecord
import co.kr.imok.headream.app.navigation.BottomNavItem
import co.kr.imok.headream.app.navigation.Screen
import co.kr.imok.headream.app.viewmodel.CallViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun MainAppScreenWithBackHandler(
    viewModel: CallViewModel,
    modifier: Modifier
) {
    var currentScreen by remember { mutableStateOf(Screen.Main.route) }
    var selectedCallId by remember { mutableStateOf<String?>(null) }
    
    // 오디오 재생 상태
    var isAudioPlaying by remember { mutableStateOf(false) }
    
    // 뒤로가기 처리 상태
    var backPressedOnce by remember { mutableStateOf(false) }
    var showExitMessage by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    
    // 오디오 매니저 초기화 (플랫폼별)
    LaunchedEffect(Unit) {
        AudioPlaybackManager.initialize()
        println("🎵 기본 오디오 초기화 완료")
    }
    
    // 뒤로가기 두 번 눌러서 종료 처리
    LaunchedEffect(backPressedOnce) {
        if (backPressedOnce) {
            snackbarHostState.showSnackbar(
                message = "한 번 더 누르면 앱이 종료됩니다.",
                duration = SnackbarDuration.Short
            )
            delay(2000) // 2초 대기
            backPressedOnce = false
        }
    }
    
    // 화면 변경 시 오디오 재생 중지
    LaunchedEffect(currentScreen) {
        if (!currentScreen.startsWith("call_detail/") && isAudioPlaying) {
            try {
                AudioPlaybackManager.stop()
                isAudioPlaying = false
                println("🎵 화면 이동으로 인한 오디오 재생 중지: $currentScreen")
            } catch (e: Exception) {
                println("❌ 오디오 정지 실패: ${e.message}")
            }
        }
    }
    
    val bottomNavItems = listOf(
        BottomNavItem.Call,
        BottomNavItem.History,
        BottomNavItem.Help
    )
    
    // 뒤로가기 처리 함수
    val handleBackPress: () -> Boolean = {
        when {
            currentScreen.startsWith("call_detail/") -> {
                // 상담상세 화면에서는 상담기록으로 이동
                currentScreen = Screen.CallHistory.route
                true
            }
            currentScreen == Screen.CallHistory.route -> {
                // 상담기록 화면에서는 메인으로 이동
                currentScreen = Screen.Main.route
                true
            }
            currentScreen == Screen.Help.route -> {
                // 도움말 화면에서는 메인으로 이동
                currentScreen = Screen.Main.route
                true
            }
            currentScreen == Screen.Main.route -> {
                // 메인 화면에서는 두 번 눌러야 종료
                if (backPressedOnce) {
                    false // 앱 종료
                } else {
                    backPressedOnce = true
                    true // 뒤로가기 소비
                }
            }
            else -> {
                // 기타 화면에서는 메인으로 이동
                currentScreen = Screen.Main.route
                true
            }
        }
    }
    
    // Android BackHandler 적용
    BackHandler(enabled = true) {
        val handled = handleBackPress()
        // handled가 false면 시스템 뒤로가기 동작 (앱 종료)
    }
    
    // CallDetail 화면인지 확인
    val isCallDetailScreen = currentScreen.startsWith("call_detail/")
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // 모든 화면에서 하단 네비게이션 표시
            NavigationBar(
                containerColor = Color.White,
                contentColor = Color(0xFF2E7D32)
            ) {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = null, // 텍스트 제거
                        selected = if (isCallDetailScreen && item.route == Screen.CallHistory.route) {
                            true // 상담상세 화면에서는 상담기록 아이콘 활성화
                        } else {
                            currentScreen == item.route
                        },
                        onClick = {
                            currentScreen = item.route
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF2E7D32),
                            unselectedIconColor = Color(0xFF999999),
                            indicatorColor = Color.Transparent // 배경 제거
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier.fillMaxSize()
        ) {
            // 상태바 영역 배경색 (해드림 브랜드 색상)
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsTopHeight(WindowInsets.statusBars)
                    .background(Color(0xFF2A7F62))
            )
            
            // 메인 콘텐츠 영역
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding())
            ) {
            when (currentScreen) {
                Screen.Main.route -> {
                    MainScreen(
                        onNavigateToCallHistory = {
                            currentScreen = Screen.CallHistory.route
                        },
                        onStartCall = {
                            // 기기의 전화번호를 가져와서 서버로 전송
                            val devicePhoneNumber = viewModel.getDevicePhoneNumber()
                            println("🔥 onStartCall - 기기 전화번호: ${devicePhoneNumber ?: "없음"}")
                            
                            // 실제 전화 걸기 (기존 로직)
                            viewModel.startCall("010-8745-8123")
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                Screen.CallHistory.route -> {
                    CallHistoryScreen(
                        callHistory = viewModel.callHistory,
                        isLoading = viewModel.uiState.isLoadingHistory,
                        isLoadingMore = viewModel.isLoadingMore,
                        totalCount = viewModel.totalRecords,
                        onNavigateBack = {
                            currentScreen = Screen.Main.route
                        },
                        onCallItemClick = { callRecord: CallRecord ->
                            selectedCallId = callRecord.id
                            currentScreen = Screen.CallDetail.createRoute(callRecord.id)
                        },
                        onRefresh = {
                            println("📋 MainAppScreen에서 통화 기록 새로고침 요청")
                            viewModel.loadCallHistory()
                        },
                        onLoadMore = {
                            println("📋 MainAppScreen에서 더 많은 통화 기록 요청")
                            viewModel.loadMoreCallHistory()
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                Screen.Help.route -> {
                    HelpScreen(
                        onNavigateBack = {
                            currentScreen = Screen.Main.route
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                else -> {
                    // CallDetail 화면
                    if (currentScreen.startsWith("call_detail/")) {
                        val callRecord = viewModel.callHistory.find { record: CallRecord -> record.id == selectedCallId }
                        val callIndex = if (callRecord != null) {
                            val recordIndex = viewModel.callHistory.indexOf(callRecord)
                            viewModel.totalRecords - recordIndex
                        } else 1
                        
                        CallDetailScreen(
                            callRecord = callRecord,
                            callIndex = callIndex,
                            isAudioPlaying = isAudioPlaying,
                            onNavigateBack = {
                                currentScreen = Screen.CallHistory.route
                            },
                            onLoadDetail = { callId, callback ->
                                viewModel.loadCallDetail(callId, callback)
                            },
                            onPlayAudio = { audioPath ->
                                println("🎵 오디오 재생 시도: $audioPath")
                                println("📱 파일 형식: m4a")
                                println("📱 Android는 m4a 형식을 지원합니다 (AAC 코덱)")
                                
                                try {
                                    if (AudioPlaybackManager.isPlaying()) {
                                        println("⏸️ 현재 재생 중 - 일시정지")
                                        AudioPlaybackManager.pause()
                                        isAudioPlaying = false
                                    } else {
                                        println("▶️ 오디오 재생 시작")
                                        AudioPlaybackManager.play(audioPath)
                                        isAudioPlaying = true
                                    }
                                } catch (e: Exception) {
                                    println("❌ 오디오 재생 오류: ${e.message}")
                                    isAudioPlaying = false
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
            }
        }
    }
}
