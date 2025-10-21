package co.kr.imok.headream.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Phone
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Main : Screen("main")
    object CallHistory : Screen("call_history")
    object CallDetail : Screen("call_detail/{callId}") {
        fun createRoute(callId: String) = "call_detail/$callId"
    }
    object Help : Screen("help")
}

// 하단 네비게이션 탭
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Call : BottomNavItem("main", "전화", Icons.Default.Phone)
    object History : BottomNavItem("call_history", "상담기록", Icons.Default.History)
    object Help : BottomNavItem("help", "도움말", Icons.Default.Help)
}
