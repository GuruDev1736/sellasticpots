package com.sellasticpots.app.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sellasticpots.app.models.Product
import com.sellasticpots.app.models.WishlistItem

object WishlistManager {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val wishlistRef = database.reference.child("wishlist")

    private var isInitialized = false

    private val _wishlistItems = MutableLiveData<MutableList<WishlistItem>>(mutableListOf())
    val wishlistItems: LiveData<MutableList<WishlistItem>> = _wishlistItems

    private val _wishlistCount = MutableLiveData<Int>(0)
    val wishlistCount: LiveData<Int> = _wishlistCount

    // Initialize wishlist data from Firebase
    fun initialize() {
        if (isInitialized) return
        isInitialized = true

        val userId = auth.currentUser?.uid ?: return

        wishlistRef.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = mutableListOf<WishlistItem>()
                for (itemSnapshot in snapshot.children) {
                    val wishlistItem = itemSnapshot.getValue(WishlistItem::class.java)
                    wishlistItem?.let { items.add(it) }
                }
                _wishlistItems.value = items
                _wishlistCount.value = items.size
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    // Add product to wishlist
    fun addToWishlist(product: Product, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onError("Please login to add to wishlist")
            return
        }

        // Check if already in wishlist
        val currentList = _wishlistItems.value ?: mutableListOf()
        val existingItem = currentList.find { it.productId == product.id }

        if (existingItem != null) {
            onError("Product already in wishlist")
            return
        }

        val wishlistItem = WishlistItem(
            id = product.id,
            productId = product.id,
            product = product,
            timestamp = System.currentTimeMillis()
        )

        wishlistRef.child(userId).child(product.id).setValue(wishlistItem)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to add to wishlist")
            }
    }

    // Remove product from wishlist
    fun removeFromWishlist(productId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onError("Please login to manage wishlist")
            return
        }

        wishlistRef.child(userId).child(productId).removeValue()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to remove from wishlist")
            }
    }

    // Check if product is in wishlist
    fun isInWishlist(productId: String): Boolean {
        val currentList = _wishlistItems.value ?: mutableListOf()
        return currentList.any { it.productId == productId }
    }

    // Toggle wishlist status
    fun toggleWishlist(product: Product, onSuccess: (Boolean) -> Unit, onError: (String) -> Unit) {
        if (isInWishlist(product.id)) {
            removeFromWishlist(
                productId = product.id,
                onSuccess = { onSuccess(false) },
                onError = onError
            )
        } else {
            addToWishlist(
                product = product,
                onSuccess = { onSuccess(true) },
                onError = onError
            )
        }
    }

    // Clear wishlist
    fun clearWishlist() {
        val userId = auth.currentUser?.uid ?: return
        wishlistRef.child(userId).removeValue()
    }
}
