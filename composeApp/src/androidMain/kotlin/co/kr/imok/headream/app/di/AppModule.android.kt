package co.kr.imokapp.headream.di

import android.content.Context
import co.kr.imokapp.headream.AndroidPlatform
import co.kr.imokapp.headream.Platform
import co.kr.imokapp.headream.data.UserManager
import co.kr.imokapp.headream.network.ApiClient
import co.kr.imokapp.headream.network.HaedreamApiClient
import co.kr.imokapp.headream.phone.PhoneManager
import co.kr.imokapp.headream.phone.PhoneManagerImpl
import co.kr.imokapp.headream.platform.PhoneNumberProvider
import co.kr.imokapp.headream.viewmodel.CallViewModel

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
