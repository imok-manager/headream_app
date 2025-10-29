package co.kr.imokapp.headream

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import co.kr.imokapp.headream.di.AppModule

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val appModule = AppModule()
    ComposeViewport(document.body!!) {
        App(appModule) 
    }
}
