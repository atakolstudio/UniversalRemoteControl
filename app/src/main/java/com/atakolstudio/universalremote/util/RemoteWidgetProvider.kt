package com.atakolstudio.universalremote.util

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.atakolstudio.universalremote.R
import com.atakolstudio.universalremote.data.local.entity.RemoteFunction
import com.atakolstudio.universalremote.data.repository.RemoteRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Home-screen widget mirroring the Quick Settings tile: shows the most recently used
 * device's name with Power / Volume Up / Volume Down buttons. Classic View-based
 * RemoteViews are required here since AppWidgets cannot host Compose content directly.
 */
@AndroidEntryPoint
class RemoteWidgetProvider : AppWidgetProvider() {

    @Inject lateinit var repository: RemoteRepository
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        const val ACTION_SEND_COMMAND = "com.atakolstudio.universalremote.WIDGET_SEND_COMMAND"
        const val EXTRA_FUNCTION = "extra_function"
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (widgetId in appWidgetIds) {
            fun pendingIntentFor(function: RemoteFunction): PendingIntent {
                val intent = Intent(context, RemoteWidgetProvider::class.java).apply {
                    action = ACTION_SEND_COMMAND
                    putExtra(EXTRA_FUNCTION, function.name)
                }
                return PendingIntent.getBroadcast(
                    context,
                    function.ordinal,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }

            val views = RemoteViews(context.packageName, R.layout.widget_remote)
            views.setOnClickPendingIntent(R.id.widget_btn_power, pendingIntentFor(RemoteFunction.POWER))
            views.setOnClickPendingIntent(R.id.widget_btn_vol_up, pendingIntentFor(RemoteFunction.VOLUME_UP))
            views.setOnClickPendingIntent(R.id.widget_btn_vol_down, pendingIntentFor(RemoteFunction.VOLUME_DOWN))
            appWidgetManager.updateAppWidget(widgetId, views)

            scope.launch {
                val device = repository.observeDevices().first().firstOrNull()
                views.setTextViewText(
                    R.id.widget_device_name,
                    device?.name ?: context.getString(R.string.widget_default_device)
                )
                appWidgetManager.updateAppWidget(widgetId, views)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_SEND_COMMAND) {
            val functionName = intent.getStringExtra(EXTRA_FUNCTION) ?: return
            val function = runCatching { RemoteFunction.valueOf(functionName) }.getOrNull() ?: return
            scope.launch {
                val device = repository.observeDevices().first().firstOrNull() ?: return@launch
                repository.sendCommand(device, function)
            }
        }
    }
}
