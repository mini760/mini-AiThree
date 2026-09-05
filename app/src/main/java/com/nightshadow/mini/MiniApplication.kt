package com.nightshadow.mini

import android.app.Application
import com.nightshadow.mini.data.AppDatabase
import com.nightshadow.mini.diagnostics.MiniLogger

class MiniApplication : Application() {
    
    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        MiniLogger.init()
        MiniLogger.i("MiniApplication", "Application started")
        database = AppDatabase.getDatabase(this)
    }
}
