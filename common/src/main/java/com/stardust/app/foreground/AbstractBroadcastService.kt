package com.stardust.app.foreground

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.CallSuper
import androidx.core.app.ServiceCompat

abstract class AbstractBroadcastService : Service() {
    private var mReceiver: BroadcastReceiver? = null
    protected val mainHandler = Handler(Looper.getMainLooper())
    private var lastStartId: Int = 0

    override fun onCreate() {
        super.onCreate()
        registerBroadcastReceiver()
    }

    private fun registerBroadcastReceiver() {
        mReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == ACTION_STOP_ALL_SERVICES) {
                    stopServiceInternal()
                } else {
                    onHandleAction(context, intent)
                }
            }
        }

        val filter = IntentFilter(ACTION_STOP_ALL_SERVICES)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(mReceiver, filter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(mReceiver, filter)
        }
    }

    /**
     * 统一停止服务的方法
     */
    protected fun stopServiceInternal() {
        mainHandler.post {
            // 1. 移除前台通知
            // 2. 调用抽象方法释放资源
            // 3. 停止服务实例
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
            releaseResources()
            stopSelf(lastStartId)
        }
    }

    /**
     * 子类实现资源释放
     */
    protected abstract fun releaseResources()

    /**
     * 处理个性化广播
     */
    open fun onHandleAction(context: Context, intent: Intent) {}

    @CallSuper
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lastStartId = startId
        return START_STICKY
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()

        try {
            unregisterReceiver(mReceiver)
        } catch (e: IllegalArgumentException) {
            // 忽略未注册的接收器
        }
    }

    companion object {
        const val ACTION_STOP_ALL_SERVICES: String = "AUTOX_STOP_ALL_SERVICES"
    }
}