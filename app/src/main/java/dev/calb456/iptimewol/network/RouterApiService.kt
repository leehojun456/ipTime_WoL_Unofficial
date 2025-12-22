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
            })
        }
    }

    suspend fun getProductName(gatewayIp: String): ProductNameResponse? {
        val url = "http://$gatewayIp/cgi/service.cgi"
        val requestBody = ProductNameRequest(method = "product/name")
        Log.d("RouterApiService", "Sending POST request to: $url with body: $requestBody")
        return try {
            val response: HttpResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
                headers.append("Origin", "http://$gatewayIp")
                headers.append("Referer", "http://$gatewayIp/ui/")
                headers.append("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36 Edg/143.0.0.0")
            }
            Log.d("RouterApiService", "Received response: ${response.status.value}, Body: ${response.body<String>()}")
            response.body<ProductNameResponse>()
        } catch (e: Exception) {
            Log.e("RouterApiService", "Error sending POST request to $url: ${e.message}", e)
            null
        }
    }
}
