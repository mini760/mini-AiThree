package com.nightshadow.mini.security

import com.nightshadow.mini.BuildConfig

object SecretManager {
    /**
     * Retrieves the Gemini API key securely injected during the build process.
     * Never log this value.
     */
    fun getGeminiApiKey(): String {
        val key = BuildConfig.GEMINI_API_KEY
        if (key == "MISSING_API_KEY" || key.isBlank()) {
            throw IllegalStateException("API Key is missing. Check local.properties or CI secrets.")
        }
        return key
    }
}
