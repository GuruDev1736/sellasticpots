package com.sellasticpots.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sellasticpots.app.databinding.ItemCartBinding
import com.sellasticpots.app.models.CartItem

class CartAdapter(
    private val onQuantityChange: (String, Int) -> Unit,
    private val onRemoveItem: (String) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private var cartItems: List<CartItem> = listOf()

    inner class CartViewHolder(private val binding: ItemCartBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(cartItem: CartItem) {
            binding.productName.text = cartItem.product.name
            binding.productPrice.text = String.format("$%.2f", cartItem.product.price)
            binding.quantityText.text = cartItem.quantity.toString()
            binding.totalPrice.text = String.format("$%.2f", cartItem.totalPrice)

            binding.btnDecrease.setOnClickListener {
                if (cartItem.quantity > 1) {
                    onQuantityChange(cartItem.product.id, cartItem.quantity - 1)
                }
            }

            binding.btnIncrease.setOnClickListener {
                onQuantityChange(cartItem.product.id, cartItem.quantity + 1)
            }

            binding.btnRemove.setOnClickListener {
                onRemoveItem(cartItem.product.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(cartItems[position])
    }

    override fun getItemCount() = cartItems.size

    fun updateItems(newItems: List<CartItem>) {
        cartItems = newItems
        notifyDataSetChanged()
    }
}
