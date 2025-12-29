package dev.calb456.iptimewol.network

import kotlinx.serialization.Serializable

class UnauthenticatedException : Exception("Authentication failed.")

@Serializable
data class BaseResponse<T>(
    val result: T? = null,
    val error: ErrorData? = null
)

@Serializable
data class ErrorData(
    val code: Int,
    val message: String,
    val data: List<String> = emptyList()
)

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
data class CaptchaCheckResponse(val result: String?, val error: ErrorData?)

// For getting a new captcha image URL
@Serializable
data class NewCaptchaResponse(val result: String?)

@Serializable
data class DdnsConfigRequest(
    val method: String,
    val params: String
)

@Serializable
data class DdnsConfigResponse(
    val result: List<DdnsConfig>
)

@Serializable
data class DdnsConfig(
    val type: String? = null,
    val host: String? = null,
    val id: String? = null,
    val wanname: String? = null
)