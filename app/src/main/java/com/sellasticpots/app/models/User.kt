package com.sellasticpots.app.models

data class User(
    val uid: String = "",
    val email: String = "",
    val username: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    // No-argument constructor for Firebase
    constructor() : this("", "", "", 0)
}

