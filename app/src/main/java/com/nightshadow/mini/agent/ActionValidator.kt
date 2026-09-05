package com.nightshadow.mini.agent

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import com.nightshadow.mini.diagnostics.MiniLogger

class ActionValidator(context: Context) {
    private val screenWidth: Int
    private val screenHeight: Int

    init {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(metrics)
        screenWidth = metrics.widthPixels
        screenHeight = metrics.heightPixels
    }

    fun isValid(action: Action): Boolean {
        return when (action.action.lowercase()) {
            "tap" -> {
                val x = action.x
                val y = action.y
                if (x == null || y == null) return false
                x in 0f..screenWidth.toFloat() && y in 0f..screenHeight.toFloat()
            }
            "swipe" -> {
                val dir = action.direction?.lowercase()
                dir in listOf("up", "down", "left", "right")
            }
            "home", "back", "done", "stop" -> true
            else -> {
                MiniLogger.w("ActionValidator", "Unknown action type: ${action.action}")
                false
            }
        }
    }
}
