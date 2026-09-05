package com.nightshadow.mini.diagnostics

import android.util.Log

object MiniLogger {
    private const val GLOBAL_TAG = "MiniAgent"

    fun init() {
        i("System", "MiniLogger initialized")
    }

    fun d(tag: String, message: String) {
        Log.d("$GLOBAL_TAG-$tag", message)
    }

    fun i(tag: String, message: String) {
        Log.i("$GLOBAL_TAG-$tag", message)
    }

    fun w(tag: String, message: String) {
        Log.w("$GLOBAL_TAG-$tag", message)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e("$GLOBAL_TAG-$tag", message, throwable)
    }
}
