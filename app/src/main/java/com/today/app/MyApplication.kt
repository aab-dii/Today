package com.today.app

import android.app.Application
import com.today.app.core.di.*
import com.today.app.di.useCaseModule
import com.today.app.di.viewModelModule
import com.today.app.notifications.ReleaseNotifications
import com.today.app.notifications.ReleaseNotificationScheduler
import com.today.app.social.di.socialModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.NONE)
            androidContext(this@MyApplication)
            modules(
                listOf(
                    databaseModule,
                    networkModule,
                    repositoryModule,
                    socialModule,
                    useCaseModule,
                    viewModelModule
                )
            )
        }

        ReleaseNotifications.ensureChannel(this)
        if (ReleaseNotifications.isEnabled(this)) {
            ReleaseNotificationScheduler.schedule(this)
        }
    }
}
