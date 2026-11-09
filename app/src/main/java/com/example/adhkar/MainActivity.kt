package com.example.adhkar

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
        }

        val status = TextView(this).apply {
            textSize = 18f
            gravity = Gravity.CENTER
        }

        val btnPermission = Button(this).apply {
            text = "1) السماح بالعرض فوق التطبيقات"
            setOnClickListener {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            }
        }

        val btnStart = Button(this).apply {
            text = "2) تشغيل"
            setOnClickListener {
                if (!Settings.canDrawOverlays(this@MainActivity)) {
                    status.text = "اسمح بالعرض فوق التطبيقات أولاً"
                } else {
                    if (Build.VERSION.SDK_INT >= 33 &&
                        checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
                    }
                    startForegroundService(Intent(this@MainActivity, OverlayService::class.java))
                    status.text = "الخدمة تعمل"
                }
            }
        }

        val btnStop = Button(this).apply {
            text = "إيقاف"
            setOnClickListener {
                stopService(Intent(this@MainActivity, OverlayService::class.java))
                status.text = "الخدمة متوقفة"
            }
        }

        val btnReset = Button(this).apply {
            text = "تصفير العدّاد"
            setOnClickListener {
                getSharedPreferences("adhkar", MODE_PRIVATE).edit().putInt("count", 0).apply()
                status.text = "العدّاد = 0"
            }
        }

        layout.addView(status)
        layout.addView(btnPermission)
        layout.addView(btnStart)
        layout.addView(btnStop)
        layout.addView(btnReset)
        setContentView(layout)
    }
}
