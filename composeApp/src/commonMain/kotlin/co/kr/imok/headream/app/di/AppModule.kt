package co.kr.imok.headream.app.di

import co.kr.imok.headream.app.Platform
import co.kr.imok.headream.app.data.UserManager
import co.kr.imok.headream.app.network.ApiClient
import co.kr.imok.headream.app.network.HaedreamApiClient
import co.kr.imok.headream.app.phone.PhoneManager
import co.kr.imok.headream.app.platform.PhoneNumberProvider
import co.kr.imok.headream.app.viewmodel.CallViewModel

expect class AppModule {
    fun providePhoneManager(): PhoneManager
    fun provideApiClient(): ApiClient
    fun provideHaedreamApiClient(): HaedreamApiClient
    fun provideUserManager(): UserManager
    fun providePhoneNumberProvider(): PhoneNumberProvider
    fun providePlatform(): Platform
    fun provideCallViewModel(): CallViewModel
}
