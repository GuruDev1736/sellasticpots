package com.sellasticpots.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sellasticpots.app.databinding.ItemWishlistBinding
import com.sellasticpots.app.models.Product

class WishlistAdapter(
    private val onItemClick: (Product) -> Unit,
    private val onRemoveClick: (Product) -> Unit,
    private val onMoveToCartClick: (Product) -> Unit
) : RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder>() {

    private var wishlistProducts = listOf<Product>()

    inner class WishlistViewHolder(private val binding: ItemWishlistBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.productName.text = product.name
            binding.productPrice.text = "₹${product.price}"
            binding.productRating.text = "${product.rating} ★"
            binding.productCategory.text = product.category

            binding.root.setOnClickListener {
                onItemClick(product)
            }

            binding.btnRemove.setOnClickListener {
                onRemoveClick(product)
            }

            binding.btnMoveToCart.setOnClickListener {
                onMoveToCartClick(product)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WishlistViewHolder {
        val binding = ItemWishlistBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WishlistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WishlistViewHolder, position: Int) {
        holder.bind(wishlistProducts[position])
    }

    override fun getItemCount() = wishlistProducts.size

    fun updateWishlist(products: List<Product>) {
        wishlistProducts = products
        notifyDataSetChanged()
    }
}
