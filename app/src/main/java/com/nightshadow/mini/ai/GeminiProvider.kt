package com.nightshadow.mini.ai

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.nightshadow.mini.diagnostics.MiniLogger
import com.nightshadow.mini.security.SecretManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiProvider : AIProvider {

    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash", // Fast, multimodal model suitable for agents
            apiKey = SecretManager.getGeminiApiKey(),
            generationConfig = generationConfig {
                temperature = 0.1f // Low temperature for deterministic JSON output
                responseMimeType = "application/json"
            }
        )
    }

    override suspend fun getNextAction(prompt: String, screenshot: Bitmap): String = withContext(Dispatchers.IO) {
        try {
            val systemInstruction = """
                You are an Android AI Agent. Analyze the screenshot and the user's goal.
                Return ONLY a valid JSON object representing the next action.
                Valid actions:
                {"action": "tap", "x": 100, "y": 200}
                {"action": "swipe", "direction": "up"} (directions: up, down, left, right)
                {"action": "home"}
                {"action": "back"}
                {"action": "done"}
                {"action": "stop", "reason": "Cannot complete task"}
            """.trimIndent()

            val inputContent = content {
                text(systemInstruction)
                text("User Goal: $prompt")
                image(screenshot)
            }

            val response = generativeModel.generateContent(inputContent)
            val responseText = response.text ?: throw IllegalStateException("Empty response from Gemini")
            
            MiniLogger.d("GeminiProvider", "Raw response: $responseText")
            return@withContext responseText
        } catch (e: Exception) {
            MiniLogger.e("GeminiProvider", "AI Request failed", e)
            throw e
        }
    }
}
