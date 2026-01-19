package com.sellasticpots.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sellasticpots.app.databinding.ItemCheckoutSummaryBinding
import com.sellasticpots.app.models.CartItem

class CheckoutSummaryAdapter(
    private val cartItems: List<CartItem>
) : RecyclerView.Adapter<CheckoutSummaryAdapter.CheckoutSummaryViewHolder>() {

    class CheckoutSummaryViewHolder(private val binding: ItemCheckoutSummaryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(cartItem: CartItem) {
            binding.productName.text = cartItem.product.name
            binding.productQuantity.text = "x${cartItem.quantity}"
            binding.productPrice.text = String.format("₹%.2f", cartItem.totalPrice)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckoutSummaryViewHolder {
        val binding = ItemCheckoutSummaryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CheckoutSummaryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CheckoutSummaryViewHolder, position: Int) {
        holder.bind(cartItems[position])
    }

    override fun getItemCount() = cartItems.size
}
