package com.kana.smarthome

data class DeviceData(
    val id: String,
    val availableCommands: List<String>,
    val opening: Int?,
    val openingMode: Int?,
    val power: Int?
)

