package com.cs407.hive.data.perplexity

import com.cs407.hive.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import android.graphics.Bitmap

class PerplexityRepository(
    private val api: PerplexityApi = PerplexityApi(BuildConfig.PERPLEXITY_API_KEY)
) {
    suspend fun analyzeBitmap(bitmap: Bitmap, prompt: String = "Describe what's in this photo and list notable items."): String = withContext(Dispatchers.IO) {
        val key = BuildConfig.PERPLEXITY_API_KEY.trim()
        require(key.isNotEmpty()) { "Missing PERPLEXITY_API_KEY. Add it to local.properties." }
        val base64 = bitmap.compressToBase64()
        val model = "sonar"
        api.askAboutImage(base64Jpeg = base64, prompt = prompt, model = model)
    }
}

private fun Bitmap.compressToBase64(quality: Int = 90): String {
    val output = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.JPEG, quality, output)
    val bytes = output.toByteArray()
    return android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
}
