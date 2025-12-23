package dev.calb456.iptimewol.ui

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.calb456.iptimewol.data.Router
import dev.calb456.iptimewol.data.RouterRepository
import dev.calb456.iptimewol.network.RouterApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AddRouterResult {

    data class ShowExtendedFields(val router: Router) : AddRouterResult()

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



    val allRouters: Flow<List<Router>> = routerRepository.allRouters



    private val routerApiService = RouterApiService()



    fun handleDiscoveredRouterClick(ip: String) {

        viewModelScope.launch {

            _preLoginCheckState.value = PreLoginCheckState.Loading

            val checkResponse = routerApiService.checkCaptchaRequirement(ip)

            if (checkResponse?.error != null && checkResponse.error.code == -31997) {

                val isCaptchaRequired = checkResponse.error.data.contains("captcha")

                var captchaRelativeUrl: String? = null

                if (isCaptchaRequired) {

                    val captchaResponse = routerApiService.getNewCaptcha(ip)

                    if (captchaResponse?.result != null) {

                        captchaRelativeUrl = captchaResponse.result

                    } else {

                        _preLoginCheckState.value = PreLoginCheckState.Error("Failed to load captcha image.")

                        return@launch

                    }

                }

                _preLoginCheckState.value = PreLoginCheckState.Ready(isCaptchaRequired, captchaRelativeUrl)

            } else {

                _preLoginCheckState.value = PreLoginCheckState.Ready(false, null)

            }

        }

    }



    fun resetPreLoginState() {

        _preLoginCheckState.value = PreLoginCheckState.Idle

    }



    fun addRouter(router: Router, captchaText: String?, captchaRelativeUrl: String?, isFinalAdd: Boolean) {

        viewModelScope.launch {

            _addRouterResult.value = AddRouterResult.InProgress

            if (!isFinalAdd) {

                val loginResponse = routerApiService.login(

                    gatewayIp = router.ipAddress,

                    id = router.loginId,

                    pw = router.password,

                    captchaText = captchaText,

                    captchaUrl = captchaRelativeUrl

                )

                if (loginResponse != null && loginResponse.result == "done") {

                    _addRouterResult.value = AddRouterResult.ShowExtendedFields(router)

                    Log.d("MainViewModel", "Login successful for ${router.name}, showing extended fields.")

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

            _loadingIps.value -= ip

        }

    }

}
