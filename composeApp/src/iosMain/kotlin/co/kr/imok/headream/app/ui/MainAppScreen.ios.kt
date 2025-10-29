package co.kr.imokapp.headream.ui

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
import co.kr.imokapp.headream.audio.AudioPlaybackManager
import co.kr.imokapp.headream.audio.initializeAudioWithContext
import co.kr.imokapp.headream.data.CallRecord
import co.kr.imokapp.headream.navigation.BottomNavItem
import co.kr.imokapp.headream.navigation.Screen
import co.kr.imokapp.headream.viewmodel.CallViewModel

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
    
    // iOS에서는 시스템 제스처로 뒤로가기가 처리되므로 별도 처리 불필요
    val snackbarHostState = remember { SnackbarHostState() }
    
    // 오디오 매니저 초기화 (플랫폼별)
    LaunchedEffect(Unit) {
        AudioPlaybackManager.initialize()
        println("🎵 기본 오디오 초기화 완료")
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
            // 상태바 영역 배경색 (토닥 브랜드 색상)
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
                            // iOS에서는 전화번호 수집 없이 전화 앱만 열기
                            println("📱 iOS - 전화 앱 열기 (010-4798-8123)")
                            viewModel.startCallForIOS("010-4798-8123")
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
                                println("📱 iOS는 m4a 형식을 지원합니다 (AAC 코덱)")
                                
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
