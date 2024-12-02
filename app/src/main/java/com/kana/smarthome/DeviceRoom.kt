package com.kana.smarthome

import android.os.Parcel
import android.os.Parcelable

data class DeviceRoom(
    val id: String,
    val availableCommands: List<String>,
    val opening: Int?,
    val openingMode: Int?,
    val power: Int?
) : Parcelable {

    constructor(parcel: Parcel) : this(
        id = parcel.readString() ?: "",
        availableCommands = parcel.createStringArrayList() ?: emptyList(),
        opening = parcel.readValue(Int::class.java.classLoader) as? Int,
        openingMode = parcel.readValue(Int::class.java.classLoader) as? Int,
        power = parcel.readValue(Int::class.java.classLoader) as? Int
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeStringList(availableCommands)
        parcel.writeValue(opening)
        parcel.writeValue(openingMode)
        parcel.writeValue(power)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<DeviceRoom> {
        override fun createFromParcel(parcel: Parcel): DeviceRoom {
            return DeviceRoom(parcel)
        }

        override fun newArray(size: Int): Array<DeviceRoom?> {
            return arrayOfNulls(size)
        }
    }
}
