package co.kr.imokapp.headream.ui

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.main_banner
import kotlinproject.composeapp.generated.resources.logo_todoc
import org.jetbrains.compose.resources.painterResource

@Composable
actual fun PlatformImage(
    resourceName: String,
    contentDescription: String?,
    modifier: Modifier
) {
    when (resourceName) {
        "logo_todoc" -> {
            Image(
                painter = painterResource(Res.drawable.logo_todoc),
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = ContentScale.Fit
            )
        }
        "main_banner" -> {
            Image(
                painter = painterResource(Res.drawable.main_banner),
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = ContentScale.Fit
            )
        }
        else -> {
            // 리소스가 없을 경우 텍스트로 대체
            Box(
                modifier = modifier.background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "IMAGE",
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
actual fun LogoImage(modifier: Modifier) {
    PlatformImage(
        resourceName = "logo_todoc",
        contentDescription = "토닥 로고",
        modifier = modifier
    )
}

@Composable
actual fun BannerImage(modifier: Modifier) {
    Image(
        painter = painterResource(Res.drawable.main_banner),
        contentDescription = "상담 안내 배너",
        modifier = modifier,
        contentScale = ContentScale.FillWidth
    )
}
