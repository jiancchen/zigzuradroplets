package com.zigzura.droplets.repository

import android.util.Log
import com.zigzura.droplets.api.ApiClient
import com.zigzura.droplets.data.ClaudeMessage
import com.zigzura.droplets.data.ClaudeRequest
import com.zigzura.droplets.data.PreferencesManager
import com.zigzura.droplets.data.PromptGenerator
import com.zigzura.droplets.data.PromptHistory
import kotlinx.coroutines.flow.first
import java.util.UUID

data class HtmlGenerationResult(
    val html: String,
    val promptHistory: PromptHistory
)

class ClaudeRepository(private val preferencesManager: PreferencesManager) {

    suspend fun generateHtml(uuid: String, prompt: String, enableDebug: Boolean = false, temperature: Double = 0.3): Result<HtmlGenerationResult> {
        return try {
            // UUID is now passed in from the ViewModel - no need to generate here

            val apiKey = preferencesManager.apiKey.first()
            if (apiKey.isNullOrBlank()) {
                return Result.failure(Exception("API key not found"))
            }

            // Enhanced API key validation
            if (!apiKey.startsWith("sk-ant-")) {
                Log.e("ClaudeRepository", "Invalid API key format. Expected format: sk-ant-...")
                return Result.failure(Exception("Invalid API key format. Claude API keys should start with 'sk-ant-'"))
            }

            Log.d("ClaudeRepository", "Using API key: ${apiKey.take(10)}...")

            // Enhance prompt with instructions for HTML output

            val request = ClaudeRequest(
                messages = listOf(
                    ClaudeMessage(
                        role = "user",
                        content = PromptGenerator.generatePrompt(prompt).trimIndent()
                    )
                ),
                temperature = temperature // Pass the temperature parameter
            )

            Log.d("ClaudeRepository", "Sending request to Claude API...")

            val response = ApiClient.claudeApiService.sendMessage(
                apiKey = apiKey,
                request = request
            )

            Log.d("ClaudeRepository", "Response code: ${response.code()}")

            if (response.isSuccessful) {
                val claudeResponse = response.body()
                val htmlContent = claudeResponse?.content?.firstOrNull()?.text ?: ""

                // Check if the response contains a prompt rejection
                if (htmlContent.contains("XPROMPTREJECTREASON")) {
                    val rejectionReason = htmlContent.substringAfter("XPROMPTREJECTREASON:")
                        .substringBefore("\n")
                        .trim()

                    Log.w("ClaudeRepository", "Prompt rejected: $rejectionReason")
                    return Result.failure(Exception("PROMPT_REJECTED:$rejectionReason"))
                }

                // Save to history using the pre-generated UUID
                val promptHistory = preferencesManager.savePrompt(uuid, prompt, htmlContent)

                // Return either debug HTML or original content based on flag
                val finalHtml = if (enableDebug) {
                    // Create debug HTML with original content
                    """
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <meta name="viewport" content="width=device-width, initial-scale=1.0">
                            <style>
                                .debug-info {
                                    position: fixed;
                                    top: 0;
                                    left: 0;
                                    right: 0;
                                    background: rgba(0, 123, 255, 0.9);
                                    color: white;
                                    padding: 8px;
                                    font-family: monospace;
                                    font-size: 11px;
                                    z-index: 10000;
                                    max-height: 60px;
                                    overflow-y: auto;
                                    line-height: 1.2;
                                }
                                .debug-toggle {
                                    position: fixed;
                                    top: 5px;
                                    right: 5px;
                                    background: #dc3545;
                                    color: white;
                                    border: none;
                                    padding: 3px 8px;
                                    border-radius: 3px;
                                    font-size: 10px;
                                    z-index: 10001;
                                    cursor: pointer;
                                }
                                .original-content {
                                    /* Don't add top margin - let the original content handle its own layout */
                                    position: relative;
                                    z-index: 1;
                                }
                                /* Fix for content that uses 100vh */
                                .original-content body {
                                    padding-top: 0 !important;
                                    margin-top: 0 !important;
                                }
                                /* Hide debug by default to avoid layout conflicts */
                                .debug-info.hidden {
                                    display: none;
                                }
                            </style>
                        </head>
                        <body>
                            <button class="debug-toggle" onclick="toggleDebug()">🐛</button>
                            <div class="debug-info hidden" id="debug-info">
                                <strong>Debug:</strong> "${prompt.take(50).replace("\"", "\\\"")}" | ${htmlContent.length}chars | ${java.text.SimpleDateFormat("HH:mm:ss").format(java.util.Date())}
                            </div>
                            <div class="original-content">
                                ${htmlContent}
                            </div>
                            <script>
                                function toggleDebug() {
                                    var debug = document.getElementById('debug-info');
                                    debug.classList.toggle('hidden');
                                }
                                // Auto-hide debug after 3 seconds
                                setTimeout(function() {
                                    document.getElementById('debug-info').classList.add('hidden');
                                }, 3000);
                            </script>
                        </body>
                        </html>
                    """.trimIndent()
                } else {
                    // Return original HTML content without any debug wrapper
                    htmlContent
                }

                Result.success(HtmlGenerationResult(finalHtml, promptHistory))
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("ClaudeRepository", "API Error: ${response.code()} - ${response.message()}")
                Log.e("ClaudeRepository", "Error body: $errorBody")
                Result.failure(Exception("API Error: ${response.code()} - ${response.message()}\nDetails: $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("ClaudeRepository", "Exception: ${e.message}", e)
            Result.failure(e)
        }
    }
}
