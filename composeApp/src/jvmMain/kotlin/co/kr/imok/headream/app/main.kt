package co.kr.imok.headream.app

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import co.kr.imok.headream.app.di.AppModule

fun main() = application {
    val appModule = AppModule()
    
    Window(
        onCloseRequest = ::exitApplication,
        title = "해드림",
        icon = painterResource("app_icon.png")
    ) {
        App(appModule)
    }
}