package co.kr.imokapp.headream.di

import co.kr.imokapp.headream.Platform
import co.kr.imokapp.headream.data.UserManager
import co.kr.imokapp.headream.network.ApiClient
import co.kr.imokapp.headream.network.HaedreamApiClient
import co.kr.imokapp.headream.phone.PhoneManager
import co.kr.imokapp.headream.platform.PhoneNumberProvider
import co.kr.imokapp.headream.viewmodel.CallViewModel

expect class AppModule {
    fun providePhoneManager(): PhoneManager
    fun provideApiClient(): ApiClient
    fun provideHaedreamApiClient(): HaedreamApiClient
    fun provideUserManager(): UserManager
    fun providePhoneNumberProvider(): PhoneNumberProvider
    fun providePlatform(): Platform
    fun provideCallViewModel(): CallViewModel
}
