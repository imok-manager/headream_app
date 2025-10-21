package co.kr.imok.headream.app.di

import android.content.Context
import co.kr.imok.headream.app.AndroidPlatform
import co.kr.imok.headream.app.Platform
import co.kr.imok.headream.app.data.UserManager
import co.kr.imok.headream.app.network.ApiClient
import co.kr.imok.headream.app.network.HaedreamApiClient
import co.kr.imok.headream.app.phone.PhoneManager
import co.kr.imok.headream.app.phone.PhoneManagerImpl
import co.kr.imok.headream.app.platform.PhoneNumberProvider
import co.kr.imok.headream.app.viewmodel.CallViewModel

actual class AppModule(private val context: Context) {
    
    private val haedreamApiClient by lazy { HaedreamApiClient() }
    private val platform by lazy { AndroidPlatform() }
    private val userManager by lazy { UserManager(haedreamApiClient, platform) }
    
    actual fun providePhoneManager(): PhoneManager {
        return PhoneManagerImpl(context)
    }
    
    actual fun provideApiClient(): ApiClient {
        return ApiClient()
    }
    
    actual fun provideHaedreamApiClient(): HaedreamApiClient {
        return haedreamApiClient
    }
    
    actual fun provideUserManager(): UserManager {
        return userManager
    }
    
    actual fun providePhoneNumberProvider(): PhoneNumberProvider {
        return PhoneNumberProvider()
    }
    
    actual fun providePlatform(): Platform {
        return platform
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
