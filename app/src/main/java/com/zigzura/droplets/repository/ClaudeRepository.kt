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

    companion object {
        // Debug flag to enable fake API mode for testing UI without API costs
        private const val ENABLE_FAKE_API = true // Set to false for real API calls
        private const val FAKE_API_DELAY_MS = 10000L // 10 seconds
    }

    suspend fun generateHtml(uuid: String, prompt: String, enableDebug: Boolean = false, temperature: Double = 0.3): Result<HtmlGenerationResult> {
        return try {
            // Check if fake API mode is enabled
            if (ENABLE_FAKE_API) {
                return generateFakeHtml(uuid, prompt, enableDebug, temperature)
            }

            // Real API implementation below...
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

            // Get user's preferred model from preferences
            val selectedModel = preferencesManager.claudeModel.first()
            Log.d("ClaudeRepository", "Using model: $selectedModel")

            // Enhance prompt with instructions for HTML output

            val request = ClaudeRequest(
                model = selectedModel, // Use the user's preferred model
                messages = listOf(
                    ClaudeMessage(
                        role = "user",
                        content = PromptGenerator.generatePrompt(prompt).trimIndent()
                    )
                ),
                temperature = temperature, // Pass the temperature parameter
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

                // Update the existing history entry instead of creating a new one
                val promptHistory = preferencesManager.updatePromptHistoryContent(
                    id = uuid,
                    html = htmlContent,
                    model = request.model
                ) ?: return Result.failure(Exception("Failed to update prompt history"))

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

    // Fake API implementation for testing
    private suspend fun generateFakeHtml(uuid: String, prompt: String, enableDebug: Boolean, temperature: Double): Result<HtmlGenerationResult> {
        Log.d("ClaudeRepository", "🎭 FAKE API MODE: Simulating generation...")

        // Simulate network delay
        kotlinx.coroutines.delay(FAKE_API_DELAY_MS)

        // Get existing prompt history to copy HTML from
        val existingHistory = preferencesManager.promptHistory.first()
        val validHistory = existingHistory.filter {
            !it.html.isBlank() && it.html != "GENERATING..."
        }

        val htmlContent = if (validHistory.isNotEmpty()) {
            // Pick a random existing HTML
            val randomItem = validHistory.random()
            Log.d("ClaudeRepository", "🎭 FAKE API: Using HTML from existing item: ${randomItem.prompt.take(50)}...")
            randomItem.html
        } else {
            // Fallback to sample HTML if no history exists
            Log.d("ClaudeRepository", "🎭 FAKE API: No existing history, using sample HTML")
            """
<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        body {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            font-family: 'Arial', sans-serif;
            margin: 0;
            padding: 20px;
            min-height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
        }
        .container {
            background: white;
            border-radius: 20px;
            padding: 30px;
            box-shadow: 0 20px 40px rgba(0,0,0,0.1);
            text-align: center;
            max-width: 400px;
            width: 100%;
        }
        h1 {
            color: #333;
            margin-bottom: 20px;
            font-size: 24px;
        }
        p {
            color: #666;
            line-height: 1.6;
            margin-bottom: 15px;
        }
        .badge {
            background: linear-gradient(45deg, #ff6b6b, #ee5a52);
            color: white;
            padding: 8px 16px;
            border-radius: 20px;
            display: inline-block;
            font-size: 12px;
            font-weight: bold;
            margin-top: 20px;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>🎭 Fake API Response</h1>
        <p><strong>Original Prompt:</strong> ${prompt.take(100)}${if (prompt.length > 100) "..." else ""}</p>
        <p><strong>UUID:</strong> ${uuid.take(8)}...</p>
        <p><strong>Temperature:</strong> $temperature</p>
        <p>This is a realistic fake response that simulates a real API call. Perfect for testing UI changes without API costs!</p>
        <div class="badge">FAKE MODE ENABLED</div>
    </div>
</body>
</html>
            """.trimIndent()
        }

        // Get user's preferred model from preferences (for consistency)
        val selectedModel = preferencesManager.claudeModel.first()

        // Update the existing history entry instead of creating a new one
        val promptHistory = preferencesManager.updatePromptHistoryContent(
            id = uuid,
            html = htmlContent,
            model = "$selectedModel (fake)"
        ) ?: return Result.failure(Exception("Failed to update prompt history"))

        Log.d("ClaudeRepository", "🎭 FAKE API: Generated ${htmlContent.length} chars of HTML")

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
                            background: rgba(255, 0, 0, 0.9);
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
                            position: relative;
                            z-index: 1;
                        }
                        .debug-info.hidden {
                            display: none;
                        }
                    </style>
                </head>
                <body>
                    <button class="debug-toggle" onclick="toggleDebug()">🎭</button>
                    <div class="debug-info hidden" id="debug-info">
                        <strong>🎭 FAKE API:</strong> "${prompt.take(50).replace("\"", "\\\"")}" | ${htmlContent.length}chars | ${java.text.SimpleDateFormat("HH:mm:ss").format(java.util.Date())}
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
            htmlContent
        }

        return Result.success(HtmlGenerationResult(finalHtml, promptHistory))
    }
}
