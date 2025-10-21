package co.kr.imok.headream.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
actual fun PlatformImage(
    resourceName: String,
    contentDescription: String?,
    modifier: Modifier
) {
    val context = LocalContext.current
    val resourceId = when (resourceName) {
        "logo_haedream" -> context.resources.getIdentifier("logo_haedream", "drawable", context.packageName)
        "banner_counseling" -> context.resources.getIdentifier("banner_counseling", "drawable", context.packageName)
        else -> 0
    }
    
    if (resourceId != 0) {
        Image(
            painter = painterResource(id = resourceId),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
actual fun LogoImage(modifier: Modifier) {
    PlatformImage(
        resourceName = "logo_haedream",
        contentDescription = "해드림 로고",
        modifier = modifier
    )
}

@Composable
actual fun BannerImage(modifier: Modifier) {
    val context = LocalContext.current
    val resourceId = context.resources.getIdentifier("banner_counseling", "drawable", context.packageName)
    
    if (resourceId != 0) {
        Image(
            painter = painterResource(id = resourceId),
            contentDescription = "상담 안내 배너",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        // 이미지가 없을 경우 텍스트 대체
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
}
