package com.sellasticpots.app.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CartItem(
    val productId: String = "",
    val product: Product = Product(),
    var quantity: Int = 1
) : Parcelable {
    val totalPrice: Double
        get() = product.price * quantity
}
