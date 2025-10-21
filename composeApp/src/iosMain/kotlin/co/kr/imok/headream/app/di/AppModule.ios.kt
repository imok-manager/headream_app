package co.kr.imok.headream.app.di

import co.kr.imok.headream.app.Platform
import co.kr.imok.headream.app.getPlatform
import co.kr.imok.headream.app.data.UserManager
import co.kr.imok.headream.app.network.ApiClient
import co.kr.imok.headream.app.network.HaedreamApiClient
import co.kr.imok.headream.app.phone.PhoneManager
import co.kr.imok.headream.app.phone.createPhoneManager
import co.kr.imok.headream.app.platform.PhoneNumberProvider
import co.kr.imok.headream.app.platform.PreferencesManager
import co.kr.imok.headream.app.platform.DeviceInfoProvider
import co.kr.imok.headream.app.viewmodel.CallViewModel

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
