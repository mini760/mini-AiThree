package com.nightshadow.mini.ui

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.nightshadow.mini.R
import com.nightshadow.mini.agent.AgentEngine
import com.nightshadow.mini.agent.AgentState
import com.nightshadow.mini.diagnostics.MiniLogger
import com.nightshadow.mini.vision.ScreenCaptureManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OverlayService : Service() {

    companion object {
        const val EXTRA_RESULT_CODE = "EXTRA_RESULT_CODE"
        const val EXTRA_RESULT_DATA = "EXTRA_RESULT_DATA"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "MiniOverlayChannel"
    }

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private lateinit var agentEngine: AgentEngine
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        setupOverlayView()
        
        agentEngine = AgentEngine(applicationContext)
        observeAgentState()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val resultCode = intent?.getIntExtra(EXTRA_RESULT_CODE, 0) ?: 0
        val resultData = intent?.getParcelableExtra<Intent>(EXTRA_RESULT_DATA)
        
        if (resultCode != 0 && resultData != null) {
            ScreenCaptureManager.init(this, resultCode, resultData)
        }
        return START_NOT_STICKY
    }

    private fun setupOverlayView() {
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null)
        
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) 
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY 
            else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 200
        }

        val etTask = overlayView.findViewById<EditText>(R.id.et_task_input)
        
        // Allow EditText to receive focus when clicked
        overlayView.setOnTouchListener { _, _ ->
            params.flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
            windowManager.updateViewLayout(overlayView, params)
            false
        }

        overlayView.findViewById<Button>(R.id.btn_run).setOnClickListener {
            val task = etTask.text.toString()
            if (task.isNotBlank()) {
                params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                windowManager.updateViewLayout(overlayView, params)
                agentEngine.startTask(task)
            }
        }

        overlayView.findViewById<Button>(R.id.btn_stop).setOnClickListener {
            agentEngine.cancelTask()
        }

        windowManager.addView(overlayView, params)
    }

    private fun observeAgentState() {
        serviceScope.launch {
            agentEngine.stateFlow.collect { state ->
                withContext(Dispatchers.Main) {
                    overlayView.findViewById<TextView>(R.id.tv_status).text = "Mini: ${state.name}"
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.overlay_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.overlay_notification_title))
            .setContentText(getString(R.string.overlay_notification_text))
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::overlayView.isInitialized) {
            windowManager.removeView(overlayView)
        }
        agentEngine.cancelTask()
        ScreenCaptureManager.release()
        MiniLogger.i("OverlayService", "Service destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
