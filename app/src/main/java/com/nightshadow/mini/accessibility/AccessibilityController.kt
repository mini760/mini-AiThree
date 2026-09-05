package com.nightshadow.mini.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import com.nightshadow.mini.diagnostics.MiniLogger
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object AccessibilityController {
    private var service: AccessibilityService? = null

    fun setService(s: AccessibilityService) {
        service = s
    }

    fun clearService() {
        service = null
    }

    val isAvailable: Boolean
        get() = service != null

    suspend fun performTap(x: Float, y: Float): Boolean = suspendCancellableCoroutine { continuation ->
        val svc = service
        if (svc == null) {
            MiniLogger.e("AccessibilityController", "Service not available for tap")
            continuation.resume(false)
            return@suspendCancellableCoroutine
        }

        val path = Path().apply { moveTo(x, y) }
        val stroke = GestureDescription.StrokeDescription(path, 0, 100)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()

        val result = svc.dispatchGesture(gesture, object : AccessibilityService.GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                if (continuation.isActive) continuation.resume(true)
            }
            override fun onCancelled(gestureDescription: GestureDescription?) {
                if (continuation.isActive) continuation.resume(false)
            }
        }, null)

        if (!result && continuation.isActive) {
            continuation.resume(false)
        }
    }

    suspend fun performSwipe(startX: Float, startY: Float, endX: Float, endY: Float): Boolean = suspendCancellableCoroutine { continuation ->
        val svc = service
        if (svc == null) {
            continuation.resume(false)
            return@suspendCancellableCoroutine
        }

        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0, 300)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()

        val result = svc.dispatchGesture(gesture, object : AccessibilityService.GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                if (continuation.isActive) continuation.resume(true)
            }
            override fun onCancelled(gestureDescription: GestureDescription?) {
                if (continuation.isActive) continuation.resume(false)
            }
        }, null)

        if (!result && continuation.isActive) {
            continuation.resume(false)
        }
    }

    fun performGlobalAction(action: Int): Boolean {
        return service?.performGlobalAction(action) ?: false
    }
}
