package com.nightshadow.mini.ai

import android.graphics.Bitmap

interface AIProvider {
    suspend fun getNextAction(prompt: String, screenshot: Bitmap): String
}
