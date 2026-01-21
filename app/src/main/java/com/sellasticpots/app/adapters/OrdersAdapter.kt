package com.sellasticpots.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sellasticpots.app.R
import com.sellasticpots.app.databinding.ItemOrderBinding
import com.sellasticpots.app.models.Order

class OrdersAdapter(
    private val onOrderClick: (Order) -> Unit,
    private val onCancelOrder: (Order) -> Unit,
    private val onDownloadReceipt: (Order) -> Unit
) : RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {

    private var orders = listOf<Order>()

    class OrderViewHolder(private val binding: ItemOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(order: Order, onOrderClick: (Order) -> Unit, onCancelOrder: (Order) -> Unit, onDownloadReceipt: (Order) -> Unit) {
            val formattedOrderId = formatOrderId(order.orderId)
            binding.orderIdText.text = formattedOrderId
            binding.orderDateText.text = order.getFormattedDate()
            binding.totalItemsText.text = order.getTotalItems().toString()
            binding.totalAmountText.text = String.format("₹%.2f", order.totalAmount)
            binding.deliveryAddressText.text = "${order.address}, ${order.city}"
            binding.estimatedDeliveryText.text = "Est. Delivery: ${order.getFormattedDeliveryDate()}"

            binding.orderStatusText.text = order.orderStatus
            when (order.orderStatus) {
                "Pending" -> {
                    binding.orderStatusText.setBackgroundResource(R.drawable.status_badge_pending)
                    binding.btnCancelOrder.visibility = View.VISIBLE
                }
                "Shipped" -> {
                    binding.orderStatusText.setBackgroundResource(R.drawable.status_badge_shipped)
                    binding.btnCancelOrder.visibility = View.GONE
                }
                "Delivered" -> {
                    binding.orderStatusText.setBackgroundResource(R.drawable.status_badge_delivered)
                    binding.btnCancelOrder.visibility = View.GONE
                }
                "Cancelled" -> {
                    binding.orderStatusText.setBackgroundResource(R.drawable.status_badge_cancelled)
                    binding.btnCancelOrder.visibility = View.GONE
                }
                else -> {
                    binding.orderStatusText.setBackgroundResource(R.drawable.status_badge_pending)
                    binding.btnCancelOrder.visibility = View.GONE
                }
            }

            binding.root.setOnClickListener {
                onOrderClick(order)
            }

            binding.btnDownloadReceipt.setOnClickListener {
                onDownloadReceipt(order)
            }

            binding.btnCancelOrder.setOnClickListener {
                onCancelOrder(order)
            }
        }

        private fun formatOrderId(orderId: String): String {
            val numericPart = orderId.hashCode().toString().takeLast(5).padStart(5, '0')
            return "ORD-$numericPart"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orders[position], onOrderClick, onCancelOrder, onDownloadReceipt)
    }

    override fun getItemCount() = orders.size

    fun updateOrders(newOrders: List<Order>) {
        orders = newOrders
        notifyDataSetChanged()
    }
}
