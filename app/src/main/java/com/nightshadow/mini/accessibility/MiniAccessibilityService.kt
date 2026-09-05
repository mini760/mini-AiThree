package com.nightshadow.mini.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.nightshadow.mini.diagnostics.MiniLogger

class MiniAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        MiniLogger.i("Accessibility", "Service Connected")
        AccessibilityController.setService(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // In Phase 1, we rely primarily on screen capture for vision, 
        // but we keep the event hook available for future UI tree parsing.
    }

    override fun onInterrupt() {
        MiniLogger.w("Accessibility", "Service Interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        MiniLogger.i("Accessibility", "Service Destroyed")
        AccessibilityController.clearService()
    }
}
