package com.example.data.gemini

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class BharamputraGeminiClient {

    private val client = OkHttpClient()
    private val apiKey: String = BuildConfig.GEMINI_API_KEY

    suspend fun enhanceVideoMetadata(draftTitle: String, draftDescription: String, category: String): Triple<String, String, String> {
        return withContext(Dispatchers.IO) {
            try {
                if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                    Log.e("BharamputraGemini", "No valid Gemini API key set in Secrets, using smart local rule fallback")
                    return@withContext getLocalFallback(draftTitle, draftDescription, category)
                }

                val prompt = """
                    You are Bharamputra AI, the integrated smart content assistant for the Bharamputra video sharing ecosystem.
                    You are helping a creator optimize their upload draft to get maximum views.
                    
                    Upload Draft:
                    - Title: $draftTitle
                    - Description: $draftDescription
                    - Selected Category: $category
                    
                    Generate and return a JSON object with EXACTLY three fields:
                    1. "enhancedTitle": A highly engaging, professional, and click-worthy title. (maximum 70 characters)
                    2. "enhancedDescription": A beautifully styled SEO-optimized description with an encouraging community vibe and social hashtags.
                    3. "suggestedTags": A string of 5-8 relevant comma-separated tags tailored to this content.
                    
                    Ensure you return ONLY a clean valid JSON string. Do not wrap it in markdown block tags like ```json.
                """.trimIndent()

                val jsonRequest = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", prompt)
                                })
                            })
                        })
                    })
                }

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val requestBody = jsonRequest.toString().toRequestBody(mediaType)

                val request = Request.Builder()
                    .url("https://genergeneractivelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
                    .post(requestBody)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.e("BharamputraGemini", "Gemini HTTP Request failed: ${response.code} ${response.message}")
                        return@withContext getLocalFallback(draftTitle, draftDescription, category)
                    }

                    val responseBody = response.body?.string() ?: ""
                    val root = JSONObject(responseBody)
                    val candidates = root.getJSONArray("candidates")
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.getJSONObject("content")
                    val parts = content.getJSONArray("parts")
                    var textResponse = parts.getJSONObject(0).getString("text").trim()

                    // Strip any stray markdown blocks if returned
                    if (textResponse.startsWith("```json")) {
                        textResponse = textResponse.substringAfter("```json")
                    }
                    if (textResponse.endsWith("```")) {
                        textResponse = textResponse.substringBeforeLast("```")
                    }
                    textResponse = textResponse.trim()

                    val resultJson = JSONObject(textResponse)
                    Triple(
                        resultJson.optString("enhancedTitle", "$draftTitle (AI Enhanced)"),
                        resultJson.optString("enhancedDescription", "$draftDescription\n\nOptimized by Bharamputra AI."),
                        resultJson.optString("suggestedTags", "bharamputra, creator, video, global")
                    )
                }

            } catch (e: Exception) {
                Log.e("BharamputraGemini", "Error invoking Gemini AI: ${e.message}", e)
                getLocalFallback(draftTitle, draftDescription, category)
            }
        }
    }

    private fun getLocalFallback(draftTitle: String, draftDescription: String, category: String): Triple<String, String, String> {
        val enhancedTitle = if (draftTitle.length < 10) "$draftTitle - Bharamputra Stream Edition 🚀" else "$draftTitle | Featured Studio Clip"
        val enhancedDesc = "$draftDescription\n\n---\nThank you for watching this stream on Bharamputra, the ultimate river of digital media. Subscribe to the channel, click the Bell 🔔, and drop a comment below!\n\n#bharamputra #digitalriver #$category #creator"
        val finalTags = "bharamputra, $category, explorer, regional, trending, stream, viral"
        return Triple(enhancedTitle, enhancedDesc, finalTags)
    }
}
