package com.sellasticpots.app.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Review(
    val id: String = "",
    val productId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val rating: Float = 0f,
    val reviewText: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isVerifiedPurchase: Boolean = false
) : Parcelable {
    fun getFormattedDate(): String {
        val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }

    fun getStarRating(): String {
        val fullStars = rating.toInt()
        val hasHalfStar = (rating - fullStars) >= 0.5f
        return "★".repeat(fullStars) +
               (if (hasHalfStar) "⭐" else "") +
               "☆".repeat(5 - fullStars - (if (hasHalfStar) 1 else 0))
    }
}
