package com.fodouop_fodouop_nathan.smarthome

data class Category(
    val title: String,
    val devices: ArrayList<DeviceData>
) {
    fun addDevice(device: DeviceData) {
        devices.add(device)
    }
}
