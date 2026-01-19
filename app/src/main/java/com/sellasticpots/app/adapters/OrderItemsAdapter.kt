package com.sellasticpots.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sellasticpots.app.databinding.ItemOrderDetailBinding
import com.sellasticpots.app.models.OrderItem

class OrderItemsAdapter(
    private val orderItems: List<OrderItem>
) : RecyclerView.Adapter<OrderItemsAdapter.OrderItemViewHolder>() {

    class OrderItemViewHolder(private val binding: ItemOrderDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(orderItem: OrderItem) {
            binding.productName.text = orderItem.productName
            binding.productQuantity.text = "x${orderItem.quantity}"
            binding.productPrice.text = String.format("₹%.2f", orderItem.totalPrice)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderItemViewHolder {
        val binding = ItemOrderDetailBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderItemViewHolder, position: Int) {
        holder.bind(orderItems[position])
    }

    override fun getItemCount() = orderItems.size
}
