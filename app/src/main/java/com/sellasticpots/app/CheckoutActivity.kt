package com.sellasticpots.app

import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.sellasticpots.app.databinding.ActivityCheckoutBinding
import com.sellasticpots.app.models.CartItem
import com.sellasticpots.app.models.Order
import com.sellasticpots.app.models.OrderItem
import com.sellasticpots.app.models.User
import com.sellasticpots.app.utils.CartManager
import com.sellasticpots.app.utils.OrderManager

class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding
    private var cartItems: List<CartItem> = listOf()
    private var totalPrice: Double = 0.0
    private var loadingDialog: Dialog? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        cartItems = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra("cartItems", CartItem::class.java) ?: listOf()
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayListExtra("cartItems") ?: listOf()
        }
        totalPrice = intent.getDoubleExtra("totalPrice", 0.0)

        setupToolbar()
        setupOrderSummary()
        loadUserData()
        setupPlaceOrderButton()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupOrderSummary() {
        val totalItems = cartItems.sumOf { it.quantity }
        binding.totalItemsText.text = totalItems.toString()
        binding.subtotalText.text = String.format("₹%.2f", totalPrice)
        binding.deliveryText.text = "₹0.00"
        binding.totalText.text = String.format("₹%.2f", totalPrice)
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return

        database.reference.child("users").child(userId).get()
            .addOnSuccessListener { snapshot ->
                val user = snapshot.getValue(User::class.java)
                user?.let {
                    binding.etFullName.setText(it.fullName)
                    binding.etPhone.setText(it.phoneNo)
                    binding.etEmail.setText(it.email)
                }
            }
            .addOnFailureListener {
                val currentUser = auth.currentUser
                if (currentUser?.email != null) {
                    binding.etEmail.setText(currentUser.email)
                }
                if (currentUser?.displayName != null) {
                    binding.etFullName.setText(currentUser.displayName)
                }
            }
    }

    private fun setupPlaceOrderButton() {
        binding.btnPlaceOrder.setOnClickListener {
            if (validateForm()) {
                placeOrder()
            }
        }
    }

    private fun validateForm(): Boolean {
        val fullName = binding.etFullName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val city = binding.etCity.text.toString().trim()
        val state = binding.etState.text.toString().trim()
        val pincode = binding.etPincode.text.toString().trim()

        if (fullName.isEmpty()) {
            binding.etFullName.error = "Full name is required"
            binding.etFullName.requestFocus()
            return false
        }

        if (phone.isEmpty() || phone.length < 10) {
            binding.etPhone.error = "Valid phone number is required"
            binding.etPhone.requestFocus()
            return false
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Valid email is required"
            binding.etEmail.requestFocus()
            return false
        }

        if (address.isEmpty()) {
            binding.etAddress.error = "Address is required"
            binding.etAddress.requestFocus()
            return false
        }

        if (city.isEmpty()) {
            binding.etCity.error = "City is required"
            binding.etCity.requestFocus()
            return false
        }

        if (state.isEmpty()) {
            binding.etState.error = "State is required"
            binding.etState.requestFocus()
            return false
        }

        if (pincode.isEmpty() || pincode.length != 6) {
            binding.etPincode.error = "Valid 6-digit pincode is required"
            binding.etPincode.requestFocus()
            return false
        }

        return true
    }

    private fun placeOrder() {
        showLoadingDialog("Placing your order...")

        val orderItems = cartItems.map { cartItem ->
            OrderItem(
                productId = cartItem.product.id,
                productName = cartItem.product.name,
                productPrice = cartItem.product.price,
                productImage = cartItem.product.imageUrl,
                quantity = cartItem.quantity
            )
        }

        val order = Order(
            items = orderItems,
            totalAmount = totalPrice,
            deliveryFee = 0.0,
            fullName = binding.etFullName.text.toString().trim(),
            phone = binding.etPhone.text.toString().trim(),
            email = binding.etEmail.text.toString().trim(),
            address = binding.etAddress.text.toString().trim(),
            city = binding.etCity.text.toString().trim(),
            state = binding.etState.text.toString().trim(),
            pincode = binding.etPincode.text.toString().trim(),
            orderStatus = "Pending"
        )

        OrderManager.placeOrder(
            order = order,
            onSuccess = { orderId ->
                dismissLoadingDialog()
                CartManager.clearCart()

                val formattedOrderId = formatOrderId(orderId)

                Toast.makeText(
                    this,
                    "Order placed successfully! Order ID: $formattedOrderId",
                    Toast.LENGTH_LONG
                ).show()

                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            },
            onError = { error ->
                dismissLoadingDialog()
                Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun formatOrderId(orderId: String): String {
        val numericPart = orderId.hashCode().toString().takeLast(5).padStart(5, '0')
        return "ORD-$numericPart"
    }

    private fun showLoadingDialog(message: String) {
        dismissLoadingDialog()

        loadingDialog = Dialog(this).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(false)
            setContentView(R.layout.dialog_loading)

            findViewById<TextView>(R.id.loadingText)?.text = message

            window?.setBackgroundDrawableResource(android.R.color.transparent)
            show()
        }
    }

    private fun dismissLoadingDialog() {
        loadingDialog?.dismiss()
        loadingDialog = null
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissLoadingDialog()
    }
}
