package com.kana.smarthome

data class UsersData(
    val login: String?
) {
    override fun toString(): String {
        return login ?: ""
    }
}

