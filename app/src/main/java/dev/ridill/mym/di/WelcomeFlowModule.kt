package dev.ridill.mym.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dev.ridill.mym.core.domain.util.EventBus
import dev.ridill.mym.welcomeFlow.presentation.WelcomeFlowViewModel

@Module
@InstallIn(ViewModelComponent::class)
object WelcomeFlowModule {

    @Provides
    fun provideWelcomeEventBus(): EventBus<WelcomeFlowViewModel.WelcomeFlowEvent> = EventBus()
}