package co.kr.imok.headream.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun PlatformImage(
    resourceName: String,
    contentDescription: String?,
    modifier: Modifier = Modifier
)

@Composable
expect fun LogoImage(
    modifier: Modifier = Modifier
)

@Composable
expect fun BannerImage(
    modifier: Modifier = Modifier
)
