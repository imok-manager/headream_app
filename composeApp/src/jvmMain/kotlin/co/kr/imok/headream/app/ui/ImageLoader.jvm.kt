package co.kr.imokapp.headream.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
actual fun PlatformImage(
    resourceName: String,
    contentDescription: String?,
    modifier: Modifier
) {
    // JVM에서는 텍스트로 대체
    Box(
        modifier = modifier.background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when (resourceName) {
                "logo_todoc" -> "LOGO"
                "main_banner" -> "BANNER"
                else -> "IMAGE"
            },
            color = Color.Gray
        )
    }
}

@Composable
actual fun LogoImage(modifier: Modifier) {
    Box(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(20.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "TODOC",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32),
                letterSpacing = 3.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "토닥",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2E7D32),
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
actual fun BannerImage(modifier: Modifier) {
    Row(
        modifier = modifier.padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "가장 불안한 그때,",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "따뜻한 목소리로",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "바로 연결합니다.",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333)
            )
        }
        
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(Color(0xFFE8F5E8), RoundedCornerShape(40.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "👩‍⚕️👵", fontSize = 24.sp)
        }
    }
}
