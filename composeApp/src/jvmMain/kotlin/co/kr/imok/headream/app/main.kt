package co.kr.imokapp.headream

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import co.kr.imokapp.headream.di.AppModule

fun main() = application {
    val appModule = AppModule()
    
    Window(
        onCloseRequest = ::exitApplication,
        title = "토닥",
        icon = painterResource("app_icon.png")
    ) {
        App(appModule)
    }
}