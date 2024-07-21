package dev.ridill.rivo.di

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.ridill.rivo.account.data.repository.AuthRepositoryImpl
import dev.ridill.rivo.account.domain.repository.AuthRepository
import dev.ridill.rivo.account.domain.service.AccessTokenService
import dev.ridill.rivo.account.domain.service.AccessTokenSharedPrefService
import dev.ridill.rivo.account.domain.service.AuthService
import dev.ridill.rivo.account.domain.service.FirebaseAuthService
import dev.ridill.rivo.account.presentation.accountDetails.AccountDetailsViewModel
import dev.ridill.rivo.account.presentation.util.AuthorizationService
import dev.ridill.rivo.account.presentation.util.CredentialService
import dev.ridill.rivo.account.presentation.util.DefaultAuthorizationService
import dev.ridill.rivo.account.presentation.util.DefaultCredentialService
import dev.ridill.rivo.core.domain.util.EventBus

@Module
@InstallIn(SingletonComponent::class)
object AccountModule {

    @Provides
    fun provideCredentialService(
        @ApplicationContext context: Context
    ): CredentialService = DefaultCredentialService(context)

    @Provides
    fun provideAuthService(): AuthService = FirebaseAuthService()

    @Provides
    fun provideAuthorizationService(
        @ApplicationContext context: Context
    ): AuthorizationService = DefaultAuthorizationService(context)

    @Provides
    fun provideAccessTokenService(
        @Encrypted sharedPreferences: SharedPreferences
    ): AccessTokenService = AccessTokenSharedPrefService(
        sharedPref = sharedPreferences
    )

    @Provides
    fun provideAuthRepository(
        credentialService: CredentialService,
        authService: AuthService,
        authorizationService: AuthorizationService,
        accessTokenService: AccessTokenService
    ): AuthRepository = AuthRepositoryImpl(
        credentialService = credentialService,
        authService = authService,
        authorizationService = authorizationService,
        accessTokenService = accessTokenService
    )

    @Provides
    fun provideAccountDetailsEventBus(): EventBus<AccountDetailsViewModel.AccountDetailsEvent> =
        EventBus()
}