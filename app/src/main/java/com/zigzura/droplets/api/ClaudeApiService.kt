package com.zigzura.droplets.api

import com.zigzura.droplets.data.ClaudeRequest
import com.zigzura.droplets.data.ClaudeResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ClaudeApiService {
    @POST("v1/messages")
    suspend fun sendMessage(
        @Header("Authorization") authorization: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Header("anthropic-version") anthropicVersion: String = "2023-06-01",
        @Body request: ClaudeRequest
    ): Response<ClaudeResponse>
}
