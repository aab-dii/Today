package com.today.app.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.today.app.core.domain.usecase.ReleaseScheduleUseCase
import com.today.app.core.utils.ReleaseScheduleCalculator
import kotlinx.coroutines.flow.first
import org.koin.core.context.GlobalContext

class ReleaseNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        if (!ReleaseNotifications.isEnabled(context)) return Result.success()
        if (!ReleaseNotifications.hasPermission(context)) return Result.success()

        val useCase = GlobalContext.getOrNull()?.get<ReleaseScheduleUseCase>()
            ?: return Result.retry()

        val releases = runCatching { useCase.getUpcomingReleases().first() }
            .getOrDefault(emptyList())
        val soon = ReleaseScheduleCalculator.dueSoon(releases, System.currentTimeMillis(), windowDays = 1)
        if (soon.isNotEmpty()) {
            ReleaseNotifications.show(context, soon)
        }
        return Result.success()
    }
}
