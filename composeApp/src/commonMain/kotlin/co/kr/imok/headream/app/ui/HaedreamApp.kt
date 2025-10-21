package co.kr.imok.headream.app.ui

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import co.kr.imok.headream.app.data.CallRecord
import co.kr.imok.headream.app.navigation.Screen
import co.kr.imok.headream.app.viewmodel.CallViewModel
import co.kr.imok.headream.app.data.UserManager
import kotlinx.coroutines.launch

@Composable
fun HaedreamApp(
    viewModel: CallViewModel,
    modifier: Modifier = Modifier
) {
    var showSplash by remember { mutableStateOf(true) }
    var isLoggedIn by remember { mutableStateOf(false) }
    
    // 앱 시작 시 자동 로그인 실행 (한 번만) - UserManager를 통해서만
    LaunchedEffect("auto_login") {
        if (!isLoggedIn) { // 이미 로그인되지 않은 경우에만 실행
            println("=== 자동 로그인 시작 (UserManager) ===")
            try {
                // UserManager를 통해 로그인 (중복 호출 방지)
                val userManager = viewModel.getUserManager()
                val result = userManager.loginOrRegister()
                
                result.onSuccess { user ->
                    println("✅ 로그인 성공: 사용자 UUID ${user.uuid}")
                    isLoggedIn = true
                }.onFailure { error ->
                    println("❌ 로그인 실패: ${error.message}")
                    isLoggedIn = false
                }
            } catch (e: Exception) {
                println("💥 로그인 중 오류: ${e.message}")
                isLoggedIn = false
            }
            println("=== 자동 로그인 완료 ===")
        }
    }
    
    if (showSplash) {
        SplashScreen(
            onSplashFinished = {
                showSplash = false
            },
            modifier = modifier
        )
    } else {
        MainAppScreen(
            viewModel = viewModel,
            modifier = modifier
        )
    }
}

