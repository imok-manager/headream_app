package co.kr.imok.headream.app.di

import co.kr.imok.headream.app.network.ApiClient
import co.kr.imok.headream.app.phone.PhoneManager
import co.kr.imok.headream.app.phone.createPhoneManager
import co.kr.imok.headream.app.viewmodel.CallViewModel

actual class AppModule {
    
    actual fun providePhoneManager(): PhoneManager {
        return createPhoneManager()
    }
    
    actual fun provideApiClient(): ApiClient {
        return ApiClient()
    }
    
    actual fun provideCallViewModel(): CallViewModel {
        return CallViewModel(
            phoneManager = providePhoneManager(),
            apiClient = provideApiClient()
        )
    }
}
