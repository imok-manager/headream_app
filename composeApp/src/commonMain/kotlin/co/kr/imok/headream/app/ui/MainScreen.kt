package co.kr.imok.headream.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToCallHistory: () -> Unit,
    onStartCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA)) // 연한 회색 배경
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 상단 타이틀
        Text(
            text = "HAEDREAM",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121),
            modifier = Modifier.padding(vertical = 24.dp)
        )
        
        // 배너 섹션 (Android에서는 이미지, 다른 플랫폼에서는 텍스트)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5F3E7) // 베이지 색상
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            BannerImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp) // 더 큰 높이
            )
        }
        
        // 상담 연결 버튼
        Button(
            onClick = onStartCall,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2E7D32) // 녹색
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color.White
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = "지금 상담 연결",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 상담 내역 보기 링크
        TextButton(
            onClick = onNavigateToCallHistory
        ) {
            Text(
                text = "상담 내역 보기",
                color = Color(0xFF666666),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        // 하단 여백
        Spacer(modifier = Modifier.weight(1f))
    }
}
