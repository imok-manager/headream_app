package co.kr.imok.headream.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import co.kr.imok.headream.app.di.AppModule
import co.kr.imok.headream.app.ui.HaedreamApp

@Composable
@Preview
fun App(appModule: AppModule? = null) {
    MaterialTheme {
        if (appModule != null) {
            val viewModel = remember { appModule.provideCallViewModel() }
            
            // 앱 시작 시 통화 히스토리 로드
            LaunchedEffect(Unit) {
                viewModel.loadCallHistory()
            }
            
            HaedreamApp(
                viewModel = viewModel,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxSize()
            )
        } else {
            // AppModule이 없는 경우 (미리보기 등)
            HaedreamAppPreview()
        }
    }
}

@Composable
private fun HaedreamAppPreview() {
    // 미리보기용 더미 구현
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
    ) {
        androidx.compose.material3.Text(
            text = "HAEDREAM 앱 미리보기\n실제 기능은 플랫폼별 빌드에서 사용 가능합니다.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = androidx.compose.ui.Modifier.align(androidx.compose.ui.Alignment.Center)
        )
    }
}