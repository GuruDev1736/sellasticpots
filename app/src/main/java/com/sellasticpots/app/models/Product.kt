package com.sellasticpots.app.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val rating: Double = 0.0,
    val reviews: Int = 0,
    val category: String = "",
    val imageUrl: String = "",
    val description: String = "",
    val images: List<String> = listOf(),
    val tags: List<String> = listOf(),
    val freeDelivery: Boolean = true
) : Parcelable

