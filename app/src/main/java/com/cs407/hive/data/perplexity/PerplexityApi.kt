package com.cs407.hive.data.perplexity

import com.google.gson.annotations.SerializedName
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Lightweight client for Perplexity AI chat completions API that supports
 * multimodal (text + image) prompts using a base64 JPEG image.
 */
class PerplexityApi(
    private val apiKey: String,
    baseUrl: String = "https://api.perplexity.ai/"
) {
    init {
        require(apiKey.isNotBlank()) { "PERPLEXITY_API_KEY is blank. Add it to local.properties as PERPLEXITY_API_KEY=<key>." }
    }

    private val service: PerplexityService by lazy {
        val key = apiKey.trim()
        val authInterceptor = Interceptor { chain ->
            val req = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $key")
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build()
            chain.proceed(req)
        }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(PerplexityService::class.java)
    }

    /**
     * Ask Perplexity about a base64-encoded JPEG image with an optional prompt.
     * Returns the assistant's textual response.
     */
    suspend fun askAboutImage(
        base64Jpeg: String,
        prompt: String,
        model: String = "sonar-pro",
        temperature: Double = 0.2,
        topP: Double = 0.9
    ): String {
        val dataUrl = "data:image/jpeg;base64,$base64Jpeg"
        val content = listOf(
            mapOf("type" to "text", "text" to prompt),
            mapOf(
                "type" to "image_url",
                "image_url" to mapOf("url" to dataUrl)
            )
        )
        val request = ChatCompletionRequest(
            model = model,
            messages = listOf(
                ChatMessage(role = "user", content = content)
            ),
            temperature = temperature,
            topP = topP
        )

        val response = service.chatCompletions(request)
        val text = response.choices.firstOrNull()?.message?.content?.trim()
        return text ?: ""
    }
}

// ---- Retrofit service and DTOs ----
private interface PerplexityService {
    @POST("chat/completions")
    suspend fun chatCompletions(@Body body: ChatCompletionRequest): ChatCompletionResponse
}

private data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Double? = null,
    @SerializedName("top_p") val topP: Double? = null
)

private data class ChatMessage(
    val role: String,
    // Using Map for flexible multimodal content shape expected by Perplexity API
    val content: List<Map<String, Any?>>
)

private data class ChatCompletionResponse(
    val id: String? = null,
    val model: String? = null,
    val choices: List<Choice> = emptyList()
)

private data class Choice(
    val index: Int? = null,
    val message: ChoiceMessage? = null,
)

private data class ChoiceMessage(
    val role: String? = null,
    val content: String? = null,
)
