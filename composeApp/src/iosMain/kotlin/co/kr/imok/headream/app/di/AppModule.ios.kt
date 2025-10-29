package co.kr.imokapp.headream.di

import co.kr.imokapp.headream.Platform
import co.kr.imokapp.headream.getPlatform
import co.kr.imokapp.headream.data.UserManager
import co.kr.imokapp.headream.network.ApiClient
import co.kr.imokapp.headream.network.HaedreamApiClient
import co.kr.imokapp.headream.phone.PhoneManager
import co.kr.imokapp.headream.phone.createPhoneManager
import co.kr.imokapp.headream.platform.PhoneNumberProvider
import co.kr.imokapp.headream.platform.PreferencesManager
import co.kr.imokapp.headream.platform.DeviceInfoProvider
import co.kr.imokapp.headream.viewmodel.CallViewModel

actual class AppModule {
    
    actual fun providePhoneManager(): PhoneManager {
        return createPhoneManager()
    }
    
    actual fun provideApiClient(): ApiClient {
        return ApiClient()
    }
    
    actual fun provideHaedreamApiClient(): HaedreamApiClient {
        return HaedreamApiClient()
    }
    
    actual fun provideUserManager(): UserManager {
        return UserManager(
            apiClient = provideHaedreamApiClient(),
            platform = providePlatform()
        )
    }
    
    actual fun providePhoneNumberProvider(): PhoneNumberProvider {
        return PhoneNumberProvider()
    }
    
    actual fun providePlatform(): Platform {
        return getPlatform()
    }
    
    actual fun provideCallViewModel(): CallViewModel {
        return CallViewModel(
            phoneManager = providePhoneManager(),
            apiClient = provideApiClient(),
            haedreamApiClient = provideHaedreamApiClient(),
            userManager = provideUserManager(),
            phoneNumberProvider = providePhoneNumberProvider()
        )
    }
}
