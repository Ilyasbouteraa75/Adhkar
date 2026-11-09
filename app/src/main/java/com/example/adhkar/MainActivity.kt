package com.example.adhkar

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("adhkar", MODE_PRIVATE)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
        }

        val status = TextView(this).apply {
            textSize = 18f
            gravity = Gravity.CENTER
        }

        val etText = EditText(this).apply {
            hint = "الذكر"
            setText(prefs.getString("text", "أستغفر الله وأتوب إليه"))
        }

        val etInterval = EditText(this).apply {
            hint = "الفاصل بالدقائق"
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(prefs.getInt("interval", 5).toString())
        }

        val etDuration = EditText(this).apply {
            hint = "مدة الظهور بالثواني"
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(prefs.getInt("duration", 5).toString())
        }

        val etMax = EditText(this).apply {
            hint = "العدد الأقصى"
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(prefs.getInt("max", 1000).toString())
        }

        fun saveSettings() {
            prefs.edit()
                .putString("text", etText.text.toString())
                .putInt("interval", (etInterval.text.toString().toIntOrNull() ?: 5).coerceAtLeast(1))
                .putInt("duration", (etDuration.text.toString().toIntOrNull() ?: 5).coerceAtLeast(1))
                .putInt("max", etMax.text.toString().toIntOrNull() ?: 1000)
                .apply()
        }

        val btnPermission = Button(this).apply {
            text = "1) السماح بالعرض فوق التطبيقات"
            setOnClickListener {
                startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                )
            }
        }

        val btnStart = Button(this).apply {
            text = "2) حفظ وتشغيل"
            setOnClickListener {
                if (!Settings.canDrawOverlays(this@MainActivity)) {
                    status.text = "اسمح بالعرض فوق التطبيقات أولاً"
                } else {
                    saveSettings()
                    if (Build.VERSION.SDK_INT >= 33 &&
                        checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
                    }
                    stopService(Intent(this@MainActivity, OverlayService::class.java))
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
                prefs.edit().putInt("count", 0).apply()
                status.text = "العدّاد = 0"
            }
        }

        layout.addView(status)
        layout.addView(etText)
        layout.addView(etInterval)
        layout.addView(etDuration)
        layout.addView(etMax)
        layout.addView(btnPermission)
        layout.addView(btnStart)
        layout.addView(btnStop)
        layout.addView(btnReset)
        setContentView(layout)
    }
}
