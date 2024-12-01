package com.kana.smarthome

import com.fodouop_fodouop_nathan.smarthome.DeviceData

data class DeviceResponse(
    val devices: List<DeviceData>
)
data class Roo(
    val name: String,
    val devices: List<DeviceData>
)
