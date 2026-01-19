package com.sellasticpots.app.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sellasticpots.app.models.Order
import com.sellasticpots.app.models.OrderItem

object OrderManager {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val ordersRef = database.reference.child("orders")

    private var isInitialized = false
    private val _orders = MutableLiveData<MutableList<Order>>(mutableListOf())
    val orders: LiveData<MutableList<Order>> = _orders

    fun initialize() {
        if (isInitialized) return
        isInitialized = true

        val userId = auth.currentUser?.uid ?: return

        ordersRef.child(userId).orderByChild("orderDate")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val orderList = mutableListOf<Order>()
                    for (orderSnapshot in snapshot.children) {
                        val order = orderSnapshot.getValue(Order::class.java)
                        order?.let { orderList.add(it) }
                    }
                    orderList.sortByDescending { it.orderDate }
                    _orders.value = orderList
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    fun placeOrder(
        order: Order,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onError("Please login to place order")
            return
        }

        val orderId = ordersRef.child(userId).push().key
        if (orderId == null) {
            onError("Failed to generate order ID")
            return
        }

        val orderWithId = order.copy(orderId = orderId, userId = userId)

        ordersRef.child(userId).child(orderId).setValue(orderWithId)
            .addOnSuccessListener {
                onSuccess(orderId)
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Failed to place order")
            }
    }

    fun getOrderById(orderId: String, callback: (Order?) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        ordersRef.child(userId).child(orderId).get()
            .addOnSuccessListener { snapshot ->
                val order = snapshot.getValue(Order::class.java)
                callback(order)
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    fun updateOrderStatus(orderId: String, status: String, onComplete: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        ordersRef.child(userId).child(orderId).child("orderStatus").setValue(status)
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    fun cancelOrder(orderId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onError("Please login to cancel order")
            return
        }

        updateOrderStatus(orderId, "Cancelled") { success ->
            if (success) {
                onSuccess()
            } else {
                onError("Failed to cancel order")
            }
        }
    }
}
