package com.sellasticpots.app.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sellasticpots.app.models.CartItem
import com.sellasticpots.app.models.Product

object CartManager {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private var cartRef = database.reference.child("carts")

    private var isInitialized = false
    private val _cartItems = MutableLiveData<MutableList<CartItem>>(mutableListOf())
    val cartItems: LiveData<MutableList<CartItem>> = _cartItems

    private val _cartCount = MutableLiveData<Int>(0)
    val cartCount: LiveData<Int> = _cartCount

    private val _totalPrice = MutableLiveData<Double>(0.0)
    val totalPrice: LiveData<Double> = _totalPrice

    // Initialize cart data from Firebase
    fun initialize() {
        if (isInitialized) return
        isInitialized = true

        val userId = auth.currentUser?.uid ?: return

        cartRef.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = mutableListOf<CartItem>()
                for (itemSnapshot in snapshot.children) {
                    val cartItem = itemSnapshot.getValue(CartItem::class.java)
                    cartItem?.let { items.add(it) }
                }
                _cartItems.value = items
                updateCartStats()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    fun addToCart(product: Product, quantity: Int = 1) {
        val userId = auth.currentUser?.uid ?: return
        val currentList = _cartItems.value ?: mutableListOf()

        // Check if product already exists in cart
        val existingItem = currentList.find { it.product.id == product.id }

        if (existingItem != null) {
            // Update quantity
            existingItem.quantity += quantity
            // Update in Firebase
            cartRef.child(userId).child(product.id).setValue(existingItem)
        } else {
            // Add new item
            val newItem = CartItem(productId = product.id, product = product, quantity = quantity)
            currentList.add(newItem)
            // Save to Firebase
            cartRef.child(userId).child(product.id).setValue(newItem)
        }

        _cartItems.value = currentList
        updateCartStats()
    }

    fun removeFromCart(productId: String) {
        val userId = auth.currentUser?.uid ?: return
        val currentList = _cartItems.value ?: mutableListOf()
        currentList.removeAll { it.product.id == productId }
        _cartItems.value = currentList

        // Remove from Firebase
        cartRef.child(userId).child(productId).removeValue()

        updateCartStats()
    }

    fun updateQuantity(productId: String, quantity: Int) {
        val userId = auth.currentUser?.uid ?: return
        val currentList = _cartItems.value ?: mutableListOf()
        val item = currentList.find { it.product.id == productId }

        if (item != null) {
            if (quantity <= 0) {
                removeFromCart(productId)
            } else {
                item.quantity = quantity
                _cartItems.value = currentList

                // Update in Firebase
                cartRef.child(userId).child(productId).setValue(item)

                updateCartStats()
            }
        }
    }

    fun clearCart() {
        val userId = auth.currentUser?.uid ?: return
        _cartItems.value = mutableListOf()

        // Clear from Firebase
        cartRef.child(userId).removeValue()

        updateCartStats()
    }

    fun getCartItemCount(): Int {
        return _cartItems.value?.sumOf { it.quantity } ?: 0
    }

    fun getTotalPrice(): Double {
        return _cartItems.value?.sumOf { it.totalPrice } ?: 0.0
    }

    private fun updateCartStats() {
        _cartCount.value = getCartItemCount()
        _totalPrice.value = getTotalPrice()
    }
}
