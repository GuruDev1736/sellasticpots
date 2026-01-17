package com.sellasticpots.app.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sellasticpots.app.models.Review

object ReviewManager {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val reviewsRef = database.reference.child("reviews")

    // Submit a new review
    fun submitReview(
        productId: String,
        rating: Float,
        reviewText: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onError("Please login to submit a review")
            return
        }

        if (rating == 0f) {
            onError("Please select a rating")
            return
        }

        if (reviewText.isBlank()) {
            onError("Please write a review")
            return
        }

        val reviewId = reviewsRef.child(productId).push().key ?: return

        // Fetch user's actual name from database
        database.reference.child("users").child(currentUser.uid).get()
            .addOnSuccessListener { snapshot ->
                val user = snapshot.getValue(com.sellasticpots.app.models.User::class.java)

                // Get user name with proper fallback chain
                val userName = when {
                    user?.fullName?.isNotBlank() == true -> user.fullName
                    user?.username?.isNotBlank() == true -> user.username
                    currentUser.displayName?.isNotBlank() == true -> currentUser.displayName!!
                    currentUser.email?.isNotBlank() == true -> currentUser.email!!.substringBefore("@")
                    else -> "Anonymous User"
                }

                val review = Review(
                    id = reviewId,
                    productId = productId,
                    userId = currentUser.uid,
                    userName = userName,
                    userEmail = currentUser.email ?: "",
                    rating = rating,
                    reviewText = reviewText,
                    timestamp = System.currentTimeMillis(),
                    isVerifiedPurchase = true
                )

                reviewsRef.child(productId).child(reviewId).setValue(review)
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        onError(e.message ?: "Failed to submit review")
                    }
            }
            .addOnFailureListener {
                // If database fetch fails, use fallback
                val userName = when {
                    currentUser.displayName?.isNotBlank() == true -> currentUser.displayName!!
                    currentUser.email?.isNotBlank() == true -> currentUser.email!!.substringBefore("@")
                    else -> "Anonymous User"
                }

                val review = Review(
                    id = reviewId,
                    productId = productId,
                    userId = currentUser.uid,
                    userName = userName,
                    userEmail = currentUser.email ?: "",
                    rating = rating,
                    reviewText = reviewText,
                    timestamp = System.currentTimeMillis(),
                    isVerifiedPurchase = true
                )

                reviewsRef.child(productId).child(reviewId).setValue(review)
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        onError(e.message ?: "Failed to submit review")
                    }
            }
    }

    // Get reviews for a product
    fun getReviews(productId: String, callback: (List<Review>) -> Unit) {
        reviewsRef.child(productId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val reviews = mutableListOf<Review>()
                for (reviewSnapshot in snapshot.children) {
                    val review = reviewSnapshot.getValue(Review::class.java)
                    review?.let { reviews.add(it) }
                }
                // Sort by timestamp (newest first)
                reviews.sortByDescending { it.timestamp }
                callback(reviews)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyList())
            }
        })
    }

    // Get reviews once (no listener)
    fun getReviewsOnce(productId: String, callback: (List<Review>) -> Unit) {
        reviewsRef.child(productId).get()
            .addOnSuccessListener { snapshot ->
                val reviews = mutableListOf<Review>()
                for (reviewSnapshot in snapshot.children) {
                    val review = reviewSnapshot.getValue(Review::class.java)
                    review?.let { reviews.add(it) }
                }
                // Sort by timestamp (newest first)
                reviews.sortByDescending { it.timestamp }
                callback(reviews)
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }

    // Calculate average rating
    fun calculateAverageRating(reviews: List<Review>): Float {
        if (reviews.isEmpty()) return 0f
        return reviews.map { it.rating }.average().toFloat()
    }

    // Get rating distribution
    fun getRatingDistribution(reviews: List<Review>): Map<Int, Int> {
        val distribution = mutableMapOf(1 to 0, 2 to 0, 3 to 0, 4 to 0, 5 to 0)
        reviews.forEach { review ->
            val rating = review.rating.toInt()
            distribution[rating] = (distribution[rating] ?: 0) + 1
        }
        return distribution
    }
}
