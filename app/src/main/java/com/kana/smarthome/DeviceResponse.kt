package com.kana.smarthome


data class DeviceResponse(
    val devices: List<DeviceData>
)
data class Roo(
    val name: String,
    val devices: List<DeviceData>
)
