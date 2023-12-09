package dev.ridill.rivo.application

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import dev.ridill.rivo.core.domain.util.BuildUtil
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class RivoApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        setupTimber()
    }

    private fun setupTimber() {
        if (BuildUtil.isDebug) {
            Timber.plant(Timber.DebugTree())
        }
    }
}