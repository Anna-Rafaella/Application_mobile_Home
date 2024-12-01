package com.kana.smarthome

import DeviceRoom
import android.os.Parcel
import android.os.Parcelable

data class Room(
    val name: String,
    val devices: List<DeviceRoom>
) : Parcelable {

    constructor(parcel: Parcel) : this(
        name = parcel.readString() ?: "",
        devices = parcel.createTypedArrayList(DeviceRoom.CREATOR) ?: emptyList()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeTypedList(devices)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Room> {
        override fun createFromParcel(parcel: Parcel): Room {
            return Room(parcel)
        }

        override fun newArray(size: Int): Array<Room?> {
            return arrayOfNulls(size)
        }
    }
}
