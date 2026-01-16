package com.sellasticpots.app.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class WishlistItem(
    val id: String = "",
    val productId: String = "",
    val product: Product = Product(),
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable
