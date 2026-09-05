package com.nightshadow.mini.agent

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import com.nightshadow.mini.accessibility.AccessibilityController
import com.nightshadow.mini.diagnostics.MiniLogger
import kotlinx.coroutines.delay

class ActionExecutor(context: Context) {
    private val screenWidth: Int
    private val screenHeight: Int

    init {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(metrics)
        screenWidth = metrics.widthPixels
        screenHeight = metrics.heightPixels
    }

    suspend fun execute(action: Action): Boolean {
        MiniLogger.i("ActionExecutor", "Executing: ${action.action}")
        return when (action.action.lowercase()) {
            "tap" -> {
                AccessibilityController.performTap(action.x!!, action.y!!)
            }
            "swipe" -> {
                val cx = screenWidth / 2f
                val cy = screenHeight / 2f
                val offset = 300f
                when (action.direction?.lowercase()) {
                    "up" -> AccessibilityController.performSwipe(cx, cy + offset, cx, cy - offset)
                    "down" -> AccessibilityController.performSwipe(cx, cy - offset, cx, cy + offset)
                    "left" -> AccessibilityController.performSwipe(cx + offset, cy, cx - offset, cy)
                    "right" -> AccessibilityController.performSwipe(cx - offset, cy, cx + offset, cy)
                    else -> false
                }
            }
            "home" -> {
                AccessibilityController.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
                delay(1000) // Wait for home screen
                true
            }
            "back" -> {
                AccessibilityController.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
                true
            }
            "done", "stop" -> true
            else -> false
        }
    }
}
