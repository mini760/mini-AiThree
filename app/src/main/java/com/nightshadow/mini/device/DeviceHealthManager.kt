package com.nightshadow.mini.device

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

class DeviceHealthManager(private val context: Context) {

    fun getBatteryLevel(): Int {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            context.registerReceiver(null, ifilter)
        }
        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        
        return if (level != -1 && scale != -1) {
            (level * 100 / scale.toFloat()).toInt()
        } else {
            -1
        }
    }

    fun isBatterySufficientForTask(): Boolean {
        val level = getBatteryLevel()
        return level > 15 // Arbitrary safe threshold
    }
}
