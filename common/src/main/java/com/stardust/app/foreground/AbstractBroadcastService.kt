package com.stardust.app.foreground

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * 支持广播消息的Service
 */
abstract class AbstractBroadcastService : Service() {
    private var mReceiver: BroadcastReceiver? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate() {
        super.onCreate()
        registerBroadcastReceiver()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun registerBroadcastReceiver() {
        mReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                // 响应停止广播
                if (intent.action == ACTION_STOP_ALL_SERVICES) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                } else {
                    onHandleAction(context, intent)
                }
            }
        }
        val filter = IntentFilter(ACTION_STOP_ALL_SERVICES)
        registerReceiver(mReceiver, filter, RECEIVER_NOT_EXPORTED)
    }

    /**
     * 个性化广播消息处理
     */
    open fun onHandleAction(context: Context, intent: Intent) {}

    override fun onDestroy() {
        super.onDestroy()
        // 注销广播
        unregisterReceiver(mReceiver)
    }

    companion object {
        const val ACTION_STOP_ALL_SERVICES: String = "AUTOX_STOP_ALL_SERVICES"
    }
}