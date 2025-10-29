package co.kr.imokapp.headream.di

import co.kr.imokapp.headream.network.ApiClient
import co.kr.imokapp.headream.phone.PhoneManager
import co.kr.imokapp.headream.phone.createPhoneManager
import co.kr.imokapp.headream.viewmodel.CallViewModel

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
