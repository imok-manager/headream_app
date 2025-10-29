package co.kr.imokapp.headream

import androidx.compose.ui.window.ComposeUIViewController
import co.kr.imokapp.headream.di.AppModule

fun MainViewController() = ComposeUIViewController { 
    val appModule = AppModule()
    App(appModule) 
}