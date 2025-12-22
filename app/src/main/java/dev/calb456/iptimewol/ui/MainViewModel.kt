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

class MainViewModel(private val routerRepository: RouterRepository) : ViewModel() {
    private val _gatewayIp = MutableStateFlow<String?>(null)
    val gatewayIp: StateFlow<String?> = _gatewayIp

    private val _productNames = MutableStateFlow<Map<String, String>>(emptyMap())
    val productNames: StateFlow<Map<String, String>> = _productNames

    private val _loadingIps = MutableStateFlow<Set<String>>(emptySet())
    val loadingIps: StateFlow<Set<String>> = _loadingIps

    val allRouters: Flow<List<Router>> = routerRepository.allRouters

    private val routerApiService = RouterApiService()

    fun addRouter(router: Router) {
        viewModelScope.launch {
            routerRepository.insert(router)
        }
    }

    fun deleteRouterById(routerId: Int) {
        viewModelScope.launch {
            routerRepository.deleteRouterById(routerId)
        }
    }

    @Suppress("DEPRECATION")
    fun getGatewayIp(context: Context) {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
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
                Log.d("MainViewModel", "Successfully retrieved product name for $ip: ${response.result}")
            } else {
                Log.e("MainViewModel", "Failed to retrieve product name for $ip. Check logs in RouterApiService for details.")
            }
            _loadingIps.value -= ip
        }
    }
}

