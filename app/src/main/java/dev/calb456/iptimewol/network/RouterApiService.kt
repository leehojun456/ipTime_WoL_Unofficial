package dev.calb456.iptimewol.network

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class RouterApiService {
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true // Useful for cases where API returns null for non-nullable fields with defaults
            })
        }
    }

    private suspend inline fun <reified Req, reified Res> makePostRequest(
        gatewayIp: String,
        requestBody: Req,
        logTag: String
    ): Res? {
        val url = "http://$gatewayIp/cgi/service.cgi"
        Log.d("RouterApiService", "Sending POST request to: $url with body: $requestBody")
        return try {
            val response: HttpResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
                headers.append("Origin", "http://$gatewayIp")
                headers.append("Referer", "http://$gatewayIp/ui/")
                headers.append("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36 Edg/143.0.0.0")
            }
            Log.d("RouterApiService", "Received $logTag response: ${response.status.value}, Body: ${response.body<String>()}")
            response.body<Res>()
        } catch (e: Exception) {
            Log.e("RouterApiService", "Error sending $logTag POST request to $url: ${e.message}", e)
            null
        }
    }

    suspend fun getProductName(gatewayIp: String): ProductNameResponse? {
        val requestBody = SimpleMethodRequest(method = "product/name")
        return makePostRequest<SimpleMethodRequest, ProductNameResponse>(gatewayIp, requestBody, "product name")
    }

    suspend fun checkCaptchaRequirement(gatewayIp: String): CaptchaCheckResponse? {
        val requestBody = SimpleMethodRequest(method = "session/login")
        return makePostRequest<SimpleMethodRequest, CaptchaCheckResponse>(gatewayIp, requestBody, "captcha check")
    }

    suspend fun getNewCaptcha(gatewayIp: String): NewCaptchaResponse? {
        val requestBody = SimpleMethodRequest(method = "captcha/new")
        return makePostRequest<SimpleMethodRequest, NewCaptchaResponse>(gatewayIp, requestBody, "new captcha")
    }

    suspend fun login(gatewayIp: String, id: String, pw: String, captchaText: String?, captchaUrl: String?): LoginResponse? {
        val captchaInfo = if (captchaText != null && captchaUrl != null) {
            CaptchaInfo(text = captchaText, url = captchaUrl)
        } else {
            null
        }
        val loginParams = LoginParams(id = id, pw = pw, captcha = captchaInfo)
        val requestBody = LoginRequest(method = "session/login", params = loginParams)
        return makePostRequest<LoginRequest, LoginResponse>(gatewayIp, requestBody, "login")
    }
}
