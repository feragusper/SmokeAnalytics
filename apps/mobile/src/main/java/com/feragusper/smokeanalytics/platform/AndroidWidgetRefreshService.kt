package com.feragusper.smokeanalytics.platform

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.feragusper.smokeanalytics.libraries.architecture.domain.WidgetRefreshService
import com.feragusper.smokeanalytics.libraries.architecture.domain.WidgetSnapshot
import com.feragusper.smokeanalytics.widget.HomeStatusWidget
import com.feragusper.smokeanalytics.widget.WidgetSnapshotStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidWidgetRefreshService @Inject constructor(
    @ApplicationContext private val context: Context,
) : WidgetRefreshService {

    override suspend fun refreshHomeSnapshot(snapshot: WidgetSnapshot) {
        WidgetSnapshotStore.write(context, snapshot)
        HomeStatusWidget().updateAll(context)
    }
}
