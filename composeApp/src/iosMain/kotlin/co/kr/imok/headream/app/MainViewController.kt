package co.kr.imok.headream.app

import androidx.compose.ui.window.ComposeUIViewController
import co.kr.imok.headream.app.di.AppModule

fun MainViewController() = ComposeUIViewController { 
    val appModule = AppModule()
    App(appModule) 
}