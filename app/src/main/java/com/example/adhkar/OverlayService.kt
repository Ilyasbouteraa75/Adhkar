package com.example.adhkar

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView

class OverlayService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private var overlayView: View? = null
    private lateinit var wm: WindowManager

    // كل كم ثانية تطلع الرسالة
    private val intervalMs = 5 * 60 * 1000L
    // مدة بقاء الرسالة
    private val showMs = 5000L
    private val maxCount = 1000

    private val tick = object : Runnable {
        override fun run() {
            val pm = getSystemService(POWER_SERVICE) as PowerManager
            if (pm.isInteractive) {
                showOverlay()
            }
            handler.postDelayed(this, intervalMs)
        }
    }

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        startAsForeground()
        handler.postDelayed(tick, intervalMs)
    }

    private fun startAsForeground() {
        val channelId = "adhkar_channel"
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(channelId, "Adhkar", NotificationManager.IMPORTANCE_MIN)
        )
        val notification = Notification.Builder(this, channelId)
            .setContentTitle("أذكار تعمل")
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .build()

        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(1, notification)
        }
    }

    private fun showOverlay() {
        val prefs = getSharedPreferences("adhkar", MODE_PRIVATE)
        val count = prefs.getInt("count", 0)
        if (count >= maxCount) return

        val newCount = count + 1
        prefs.edit().putInt("count", newCount).apply()

        removeOverlay()

        val tv = TextView(this).apply {
            text = "أستغفر الله وأتوب إليه  ($newCount)"
            setTextColor(Color.parseColor("#1B1B1B"))
            textSize = 16f
            setPadding(48, 28, 48, 28)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#E8F5E9"))
                cornerRadius = 60f
            }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 24
            y = 120
        }

        try {
            wm.addView(tv, params)
            overlayView = tv
            handler.postDelayed({ removeOverlay() }, showMs)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun removeOverlay() {
        overlayView?.let {
            try { wm.removeView(it) } catch (_: Exception) {}
        }
        overlayView = null
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        removeOverlay()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
