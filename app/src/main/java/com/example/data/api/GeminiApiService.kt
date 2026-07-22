package com.example.data.api

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// Moshi data structures for Gemini REST API
data class GeminiPart(
    @Json(name = "text") val text: String? = null
)

data class GeminiContent(
    @Json(name = "role") val role: String? = null,
    @Json(name = "parts") val parts: List<GeminiPart>
)

data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null
)

data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent?
)

data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateHealthAdvice(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val apiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun askHealthAssistant(userQuery: String, ageGroupAr: String = "جميع الأعمار"): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "ملاحظة: مفتاح API غير مهيأ حالياً. يمكنك تصفح دليل التوعية الصحية والأدوات المتاحة أوفلاين في التطبيق."
        }

        val systemPrompt = "أنت مساعد توعية صحية وثقافة علاجية مبسطة باللغة العربية الفصحى الميسرة. " +
                "مهتمك تقديم شرح صحي مبسط جداً يناسب الفئة العمرية ($ageGroupAr). " +
                "تجنب المصطلحات الطبية المعقدة واشرح المصطلحات بأسلوب سهل وأمثلة يومية. " +
                "تذكر دائماً في نهاية الإجابة التأكيد بعبارة: (تنبيه: هذا الشرح للتوعية والتثقيف الصحي فقط ولا يغني عن استشارة الطبيب المختص)."

        val request = GeminiRequest(
            systemInstruction = GeminiContent(
                parts = listOf(GeminiPart(text = systemPrompt))
            ),
            contents = listOf(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = userQuery))
                )
            )
        )

        return try {
            val response = apiService.generateHealthAdvice(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "تعذر الحصول على رد حالياً. يرجى المحاولة لاحقاً."
        } catch (e: Exception) {
            "عذراً، حدث خطأ أثناء الاتصال بالخدمة: ${e.localizedMessage ?: "تأكد من الاتصال بالإنترنت"}"
        }
    }
}
