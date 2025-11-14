package com.cs407.hive.data.perplexity

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import android.graphics.Bitmap

class PerplexityRepository(
    private val api: PerplexityApi = PerplexityApi("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")
) {
    suspend fun analyzeBitmap(bitmap: Bitmap, prompt: String): String = withContext(Dispatchers.IO) {
        val base64 = bitmap.compressToBase64()
        val model = "sonar-pro"
        api.askAboutImage(base64Jpeg = base64, prompt = prompt, model = model)
    }

    // text-only ask wrapper that forwards to PerplexityApi.ask
    suspend fun askPrompt(
        prompt: String
    ): String = withContext(Dispatchers.IO) {
        val model = "sonar-pro"
        api.ask(prompt = prompt, model = model)
    }
}

private fun Bitmap.compressToBase64(quality: Int = 90): String {
    val output = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.JPEG, quality, output)
    val bytes = output.toByteArray()
    return android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
}
