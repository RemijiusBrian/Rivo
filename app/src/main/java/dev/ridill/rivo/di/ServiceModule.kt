package dev.ridill.rivo.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.ridill.rivo.core.domain.notification.NotificationHelper
import dev.ridill.rivo.settings.domain.notification.AppLockNotificationHelper
import javax.inject.Qualifier

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {
    @AppLockFeature
    @Provides
    fun provideAppLockNotificationHelper(
        @ApplicationContext context: Context
    ): NotificationHelper<Unit> = AppLockNotificationHelper(context)
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppLockFeature