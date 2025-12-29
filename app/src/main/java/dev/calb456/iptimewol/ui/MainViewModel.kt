package dev.calb456.iptimewol.ui

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.calb456.iptimewol.data.Router
import dev.calb456.iptimewol.data.RouterRepository
import dev.calb456.iptimewol.network.RouterApiService
import dev.calb456.iptimewol.network.UnauthenticatedException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.Serializable
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.isSuccess

sealed class Screen : Serializable {
    object Home : Screen()
    object AddRouter : Screen()
}

sealed class AddRouterResult {

    data class ShowExtendedFields(val router: Router, val showManualDdnsInput: Boolean = false) : AddRouterResult()

    object Success : AddRouterResult()

    data class Error(val message: String) : AddRouterResult()

    object InProgress : AddRouterResult()

    object Idle : AddRouterResult()

}



sealed class PreLoginCheckState {







    object Idle : PreLoginCheckState()







    object Loading : PreLoginCheckState()







    data class Ready(val isCaptchaRequired: Boolean, val captchaRelativeUrl: String?) : PreLoginCheckState()







    data class Error(val message: String) : PreLoginCheckState()







}







sealed class DdnsDisplayState {



    object Idle : DdnsDisplayState()



    object Loading : DdnsDisplayState()



    data class Loaded(val externalIp: String, val ddnsAddress: String, val status: String) : DdnsDisplayState()



    object NoDdnsRegistered : DdnsDisplayState()



    data class Error(val message: String) : DdnsDisplayState()



}











class MainViewModel(private val routerRepository: RouterRepository) : ViewModel() {







    private val _gatewayIp = MutableStateFlow<String?>(null)

    val gatewayIp: StateFlow<String?> = _gatewayIp



    private val _productNames = MutableStateFlow<Map<String, String?>>(emptyMap())

    val productNames: StateFlow<Map<String, String?>> = _productNames



    private val _loadingIps = MutableStateFlow<Set<String>>(emptySet())

    val loadingIps: StateFlow<Set<String>> = _loadingIps



    private val _addRouterResult = MutableStateFlow<AddRouterResult>(AddRouterResult.Idle)

    val addRouterResult: StateFlow<AddRouterResult> = _addRouterResult



    private val _preLoginCheckState = MutableStateFlow<PreLoginCheckState>(PreLoginCheckState.Idle)

    val preLoginCheckState: StateFlow<PreLoginCheckState> = _preLoginCheckState

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Home)
    val currentScreen: StateFlow<Screen> = _currentScreen

    private val _currentGatewayIp = MutableStateFlow<String?>(null)
    val currentGatewayIp: StateFlow<String?> = _currentGatewayIp

    private val _isCaptchaRequired = MutableStateFlow(false)
    val isCaptchaRequired: StateFlow<Boolean> = _isCaptchaRequired

    private val _captchaRelativeUrl = MutableStateFlow<String?>(null)
    val captchaRelativeUrl: StateFlow<String?> = _captchaRelativeUrl

    val captchaFullUrl: StateFlow<String?> = combine(currentGatewayIp, captchaRelativeUrl) { ip, relativeUrl ->
        if (ip != null && relativeUrl != null) {
            "http://$ip$relativeUrl"
        } else {
            null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)


    private val _routerForExtendedFields = MutableStateFlow<Router?>(null)
    val routerForExtendedFields: StateFlow<Router?> = _routerForExtendedFields

    private val _showExtendedFields = MutableStateFlow(false)
    val showExtendedFields: StateFlow<Boolean> = _showExtendedFields

    private val _ddnsDisplayState = MutableStateFlow<DdnsDisplayState>(DdnsDisplayState.Idle)
    val ddnsDisplayState: StateFlow<DdnsDisplayState> = _ddnsDisplayState
    
    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun onBackToHome() {
        _currentScreen.value = Screen.Home
        _routerForExtendedFields.value = null
        _showExtendedFields.value = false
    }
    
    fun setCurrentGatewayIp(ip: String) {
        _currentGatewayIp.value = ip
    }


    val allRouters: Flow<List<Router>> = routerRepository.allRouters

    private val _showUnauthenticatedError = MutableStateFlow(false)
    val showUnauthenticatedError: StateFlow<Boolean> = _showUnauthenticatedError

    private val routerApiService = RouterApiService()
    private val httpClient = HttpClient()

    private suspend fun fetchPublicIpAddress(): String? {
        return try {
            val response = httpClient.get("https://api.ipify.org/")
            if (response.status.isSuccess()) {
                response.bodyAsText()
            } else {
                Log.e("MainViewModel", "Failed to fetch public IP: ${response.status.value}")
                null
            }
        } catch (e: Exception) {
            Log.e("MainViewModel", "Error fetching public IP: ${e.message}", e)
            null
        }
    }
    
    fun unauthenticatedErrorShown() {
        _showUnauthenticatedError.value = false
    }

    fun handleDiscoveredRouterClick(ip: String) {
        viewModelScope.launch {
            try {
                _preLoginCheckState.value = PreLoginCheckState.Loading
                val checkResponse = routerApiService.checkCaptchaRequirement(ip)

                if (checkResponse?.error != null && checkResponse.error.code == -31997 && checkResponse.error.data.contains("captcha")) {
                    val captchaResponse = routerApiService.getNewCaptcha(ip)
                    if (captchaResponse?.result != null) {
                        _isCaptchaRequired.value = true
                        _captchaRelativeUrl.value = captchaResponse.result
                        _preLoginCheckState.value = PreLoginCheckState.Ready(true, captchaResponse.result)
                    } else {
                        _preLoginCheckState.value = PreLoginCheckState.Error("Failed to load captcha image.")
                    }
                } else {
                    _isCaptchaRequired.value = false
                    _captchaRelativeUrl.value = null
                    _preLoginCheckState.value = PreLoginCheckState.Ready(false, null)
                }
            } catch (e: UnauthenticatedException) {
                _showUnauthenticatedError.value = true
                _preLoginCheckState.value = PreLoginCheckState.Error(e.message ?: "Authentication failed.")
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error in handleDiscoveredRouterClick: ${e.message}", e)
                _preLoginCheckState.value = PreLoginCheckState.Error(e.message ?: "An unexpected error occurred.")
            }
        }
    }



    fun resetPreLoginState() {

        _preLoginCheckState.value = PreLoginCheckState.Idle

    }



    fun addRouter(router: Router, captchaText: String?, captchaRelativeUrl: String?, isFinalAdd: Boolean) {
        viewModelScope.launch {
            _addRouterResult.value = AddRouterResult.InProgress
            try {
                if (!isFinalAdd) {
                    val loginResponse = routerApiService.login(
                        gatewayIp = router.ipAddress,
                        id = router.loginId,
                        pw = router.password,
                        captchaText = captchaText,
                        captchaUrl = captchaRelativeUrl
                    )
                    if (loginResponse?.result == "done") {
                        Log.d("MainViewModel", "Login successful for ${router.name}.")
                        var updatedRouter = router.copy()
                        var showManualDdnsInput = false
                        // Fetch the client's public IP address
                        val externalIp = fetchPublicIpAddress() ?: "Unknown"

                        // Try to get DDNS info
                        val ddnsConfig =
                            routerApiService.getDdnsConfig(gatewayIp = router.ipAddress)
                        if (ddnsConfig?.result?.isNotEmpty() == true) {
                            val ddnsInfo = ddnsConfig.result.first()
                            val ddnsAddress = ddnsInfo.host ?: "Unknown"
                            _ddnsDisplayState.value = DdnsDisplayState.Loaded(externalIp, ddnsAddress, "정상 등록")
                            Log.d("MainViewModel", "DDNS Config found: $ddnsConfig")
                        } else {
                            showManualDdnsInput = true
                            _ddnsDisplayState.value = DdnsDisplayState.NoDdnsRegistered
                            Log.d("MainViewModel", "No DDNS Config found.")
                        }

                        _routerForExtendedFields.value = updatedRouter
                        _showExtendedFields.value = true // Always show extended fields after login
                        _addRouterResult.value = AddRouterResult.ShowExtendedFields(
                            updatedRouter,
                            showManualDdnsInput
                        )
                        Log.d(
                            "MainViewModel",
                            "Showing extended fields for ${router.name}. Manual DDNS input: $showManualDdnsInput. DDNS Config found: ${ddnsConfig.toString()}"
                        )
                    } else {
                        _addRouterResult.value = AddRouterResult.Error("Login failed. Please check your credentials.")
                        Log.e(
                            "MainViewModel",
                            "Login failed for router ${router.name} at ${router.ipAddress}. Router not saved."
                        )
                    }
                } else {
                    // Final add step
                    routerRepository.insert(router)
                    _addRouterResult.value = AddRouterResult.Success
                    Log.d("MainViewModel", "Router saved successfully: ${router.name}")
                }
            } catch (e: UnauthenticatedException) {
                _showUnauthenticatedError.value = true
                _addRouterResult.value = AddRouterResult.Error(e.message ?: "Authentication failed.")
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error in addRouter: ${e.message}", e)
                _addRouterResult.value = AddRouterResult.Error(e.message ?: "An unexpected error occurred.")
            }
        }
    }



    fun resetAddRouterResult() {

        _addRouterResult.value = AddRouterResult.Idle

    }



    fun deleteRouterById(routerId: Int) {

        viewModelScope.launch {

            routerRepository.deleteRouterById(routerId)

        }

    }



    @Suppress("DEPRECATION")

    fun getGatewayIp(context: Context) {

        val wifiManager =

            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        val dhcpInfo = wifiManager.dhcpInfo

        val gatewayAddress = intToIp(dhcpInfo.gateway)

        _gatewayIp.value = gatewayAddress

        getProductName(gatewayAddress)

    }



    private fun intToIp(ipAddress: Int): String {

        return android.text.format.Formatter.formatIpAddress(ipAddress)

    }



    fun getProductName(ip: String) {
        viewModelScope.launch {
            _loadingIps.value += ip
            Log.d("MainViewModel", "Attempting to get product name for $ip")
            try {
                val response = routerApiService.getProductName(ip)
                if (response != null) {
                    _productNames.value += (ip to response.result)
                    Log.d(
                        "MainViewModel",
                        "Successfully retrieved product name for $ip: ${response.result}"
                    )
                } else {
                    Log.e(
                        "MainViewModel",
                        "Failed to retrieve product name for $ip. Check logs in RouterApiService for details."
                    )
                }
            } catch (e: UnauthenticatedException) {
                _showUnauthenticatedError.value = true
                Log.e("MainViewModel", "Authentication failed for product name request for $ip: ${e.message}", e)
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error in getProductName for $ip: ${e.message}", e)
            } finally {
                _loadingIps.value -= ip
            }
        }
    }

}
