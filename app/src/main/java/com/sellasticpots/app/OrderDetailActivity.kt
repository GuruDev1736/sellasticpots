package com.sellasticpots.app

import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.sellasticpots.app.adapters.OrderItemsAdapter
import com.sellasticpots.app.databinding.ActivityOrderDetailBinding
import com.sellasticpots.app.models.Order

class OrderDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderDetailBinding
    private lateinit var order: Order

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding = ActivityOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                0,
                systemBars.top,
                0,
                systemBars.bottom
            )
            insets
        }

        order = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("order", Order::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("order")
        } ?: run {
            finish()
            return
        }

        setupToolbar()
        displayOrderDetails()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun displayOrderDetails() {
        val formattedOrderId = formatOrderId(order.orderId)
        binding.orderIdText.text = formattedOrderId
        binding.orderDateText.text = order.getFormattedDate()
        binding.orderStatusText.text = order.orderStatus

        when (order.orderStatus) {
            "Pending" -> {
                binding.orderStatusText.setBackgroundResource(R.drawable.status_badge_pending)
            }
            "Shipped" -> {
                binding.orderStatusText.setBackgroundResource(R.drawable.status_badge_shipped)
            }
            "Delivered" -> {
                binding.orderStatusText.setBackgroundResource(R.drawable.status_badge_delivered)
            }
            "Cancelled" -> {
                binding.orderStatusText.setBackgroundResource(R.drawable.status_badge_cancelled)
            }
            else -> {
                binding.orderStatusText.setBackgroundResource(R.drawable.status_badge_pending)
            }
        }

        binding.deliveryNameText.text = order.fullName
        binding.deliveryPhoneText.text = order.phone
        binding.deliveryAddressText.text = "${order.address}, ${order.city}, ${order.state} - ${order.pincode}"

        val adapter = OrderItemsAdapter(order.items)
        binding.orderItemsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@OrderDetailActivity)
            this.adapter = adapter
        }

        binding.subtotalText.text = String.format("₹%.2f", order.totalAmount)
        binding.deliveryFeeText.text = String.format("₹%.2f", order.deliveryFee)
        binding.totalAmountText.text = String.format("₹%.2f", order.totalAmount + order.deliveryFee)
    }

    private fun formatOrderId(orderId: String): String {
        val numericPart = orderId.hashCode().toString().takeLast(5).padStart(5, '0')
        return "ORD-$numericPart"
    }
}
