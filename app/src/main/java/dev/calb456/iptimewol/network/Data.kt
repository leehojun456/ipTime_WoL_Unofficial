package dev.calb456.iptimewol.network

import kotlinx.serialization.Serializable

@Serializable
data class ProductNameRequest(val method: String)

@Serializable
data class ProductNameResponse(val result: String)
