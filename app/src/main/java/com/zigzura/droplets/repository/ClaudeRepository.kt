package com.zigzura.droplets.repository

import android.util.Log
import com.zigzura.droplets.api.ApiClient
import com.zigzura.droplets.data.ClaudeMessage
import com.zigzura.droplets.data.ClaudeRequest
import com.zigzura.droplets.data.PreferencesManager
import kotlinx.coroutines.flow.first

class ClaudeRepository(private val preferencesManager: PreferencesManager) {

    suspend fun generateHtml(prompt: String): Result<String> {
        return try {
            val apiKey = preferencesManager.apiKey.first()
            if (apiKey.isNullOrBlank()) {
                return Result.failure(Exception("API key not found"))
            }

            val enhancedPrompt = """
                Generate a complete HTML page based on this request: $prompt
                
                Requirements:
                - Create a full HTML document with <!DOCTYPE html>, <html>, <head>, and <body> tags
                - Include appropriate CSS styling within <style> tags in the head
                - Make it responsive and visually appealing
                - Use modern CSS practices
                - Ensure all content is self-contained (no external dependencies)
                - The page should be functional and interactive if applicable
                
                Only return the HTML code, no explanations or markdown formatting.
            """.trimIndent()

            val request = ClaudeRequest(
                messages = listOf(
                    ClaudeMessage(
                        role = "user",
                        content = enhancedPrompt
                    )
                )
            )

            val response = ApiClient.claudeApiService.sendMessage(
                authorization = "Bearer $apiKey",
                request = request
            )

            if (response.isSuccessful) {
                val claudeResponse = response.body()
                val htmlContent = claudeResponse?.content?.firstOrNull()?.text ?: ""

                // Save to history
                preferencesManager.savePrompt(prompt, htmlContent)

                Result.success(htmlContent)
            } else {
                Log.e("ClaudeRepository", "API Error: ${response.code()} - ${response.message()}")
                Result.failure(Exception("API Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("ClaudeRepository", "Exception: ${e.message}", e)
            Result.failure(e)
        }
    }
}
