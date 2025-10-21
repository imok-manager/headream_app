package co.kr.imok.headream.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
// BackHandler는 Android 전용이므로 제거
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
// LocalContext와 LocalLifecycleOwner는 Android 전용이므로 제거
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
// Lifecycle 관련은 Android 전용이므로 제거
import kotlinx.coroutines.delay
import co.kr.imok.headream.app.audio.AudioPlaybackManager
import co.kr.imok.headream.app.audio.initializeAudioWithContext
import co.kr.imok.headream.app.data.CallRecord
import co.kr.imok.headream.app.navigation.BottomNavItem
import co.kr.imok.headream.app.navigation.Screen
import co.kr.imok.headream.app.viewmodel.CallViewModel

// 플랫폼별 뒤로가기 처리를 위한 expect 함수
@Composable
expect fun MainAppScreenWithBackHandler(
    viewModel: CallViewModel,
    modifier: Modifier = Modifier
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: CallViewModel,
    modifier: Modifier = Modifier
) {
    // 플랫폼별 구현 호출
    MainAppScreenWithBackHandler(viewModel, modifier)
}
