package com.zigzura.droplets.data

import com.google.gson.annotations.SerializedName

data class PromptHistory(
    @SerializedName("id")
    val id: String,
    @SerializedName("prompt")
    val prompt: String,
    @SerializedName("html")
    val html: String,
    @SerializedName("title")
    val title: String? = "",
    @SerializedName("favorite")
    val favorite: Boolean? = false,
    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis(),
    @SerializedName("lastUsed")
    val lastUsed: Long? = null,
    @SerializedName("version")
    val version: Int? = 1,
    @SerializedName("accessCount")
    val accessCount: Int? = 0,
    @SerializedName("model")
    val model: String? = "claude-3-haiku-20240307",
    @SerializedName("screenshotPath")
    val screenshotPath: String? = null
)

data class ClaudeRequest(
    @SerializedName("model")
    val model: String = "claude-3-haiku-20240307",
    @SerializedName("max_tokens")
    val maxTokens: Int = 4000,
    @SerializedName("temperature")
    val temperature: Double = 0.3,
    @SerializedName("messages")
    val messages: List<ClaudeMessage>
)

data class ClaudeMessage(
    @SerializedName("role")
    val role: String,
    @SerializedName("content")
    val content: String
)

data class ClaudeResponse(
    @SerializedName("content")
    val content: List<ClaudeContent>,
    @SerializedName("id")
    val id: String,
    @SerializedName("model")
    val model: String,
    @SerializedName("role")
    val role: String,
    @SerializedName("stop_reason")
    val stopReason: String?,
    @SerializedName("usage")
    val usage: ClaudeUsage
)

data class ClaudeContent(
    @SerializedName("text")
    val text: String,
    @SerializedName("type")
    val type: String
)

data class ClaudeUsage(
    @SerializedName("input_tokens")
    val inputTokens: Int,
    @SerializedName("output_tokens")
    val outputTokens: Int
)
