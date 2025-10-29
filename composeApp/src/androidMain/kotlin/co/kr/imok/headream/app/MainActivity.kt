package co.kr.imokapp.headream

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.activity.compose.BackHandler
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.compose.material3.MaterialTheme
import co.kr.imokapp.headream.App
import co.kr.imokapp.headream.audio.initializeAudioWithContext
import co.kr.imokapp.headream.di.AppModule
import co.kr.imokapp.headream.platform.AndroidContext

class MainActivity : ComponentActivity() {
    
    private lateinit var appModule: AppModule
    
    // 권한 요청 런처
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // 권한 결과 처리
        val allGranted = permissions.values.all { it }
        if (!allGranted) {
            // 권한이 거부된 경우 사용자에게 알림
            // 실제로는 더 자세한 처리가 필요
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        // 시스템 바를 투명하게 설정
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // 상태바 아이콘과 텍스트를 밝게 설정 (브랜드 색상 배경에 맞게)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = false  // 상태바 아이콘을 흰색으로
        windowInsetsController.isAppearanceLightNavigationBars = true
        
        // AndroidContext 초기화 (기기 UUID 생성을 위해 필요)
        AndroidContext.context = this
        
        // 오디오 매니저 초기화
        initializeAudioWithContext(this)
        println("🎵 MainActivity - 오디오 매니저 초기화 완료")
        
        // AppModule 초기화
        appModule = AppModule(this)
        
        // 필요한 권한 요청
        requestPermissions()

        setContent {
            MaterialTheme {
                App(appModule = appModule)
            }
        }
    }
    
    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.CALL_PHONE,           // 전화 걸기
            Manifest.permission.RECORD_AUDIO,         // 통화 녹음
            Manifest.permission.READ_PHONE_STATE      // 통화 상태 모니터링
        )
        
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    MaterialTheme {
        App()
    }
}