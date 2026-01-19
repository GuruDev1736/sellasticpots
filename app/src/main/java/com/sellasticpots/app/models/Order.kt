package com.sellasticpots.app.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Order(
    val orderId: String = "",
    val userId: String = "",
    val items: List<OrderItem> = listOf(),
    val totalAmount: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val fullName: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val city: String = "",
    val state: String = "",
    val pincode: String = "",
    val orderStatus: String = "Pending",
    val orderDate: Long = System.currentTimeMillis(),
    val estimatedDeliveryDate: Long = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000)
) : Parcelable {
    fun getFormattedDate(): String {
        val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(orderDate))
    }

    fun getFormattedDeliveryDate(): String {
        val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(estimatedDeliveryDate))
    }

    fun getTotalItems(): Int {
        return items.sumOf { it.quantity }
    }
}

@Parcelize
data class OrderItem(
    val productId: String = "",
    val productName: String = "",
    val productPrice: Double = 0.0,
    val productImage: String = "",
    val quantity: Int = 1
) : Parcelable {
    val totalPrice: Double
        get() = productPrice * quantity
}
