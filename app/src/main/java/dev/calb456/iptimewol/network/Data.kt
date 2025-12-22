package dev.calb456.iptimewol.network

import kotlinx.serialization.Serializable

@Serializable
data class SimpleMethodRequest(val method: String)

@Serializable
data class ProductNameResponse(val result: String?)

// --- New Login Request Structure ---
@Serializable
data class CaptchaInfo(val text: String, val url: String)

@Serializable
data class LoginParams(
    val id: String,
    val pw: String,
    val captcha: CaptchaInfo? = null
)

@Serializable
data class LoginRequest(
    val method: String,
    val params: LoginParams
)
// ------------------------------------

@Serializable
data class LoginResponse(val result: String?)

// For checking if captcha is required
@Serializable
data class ErrorData(val code: Int, val message: String, val data: List<String> = emptyList())

@Serializable
data class CaptchaCheckResponse(val result: String?, val error: ErrorData?)

// For getting a new captcha image URL
@Serializable
data class NewCaptchaResponse(val result: String?)