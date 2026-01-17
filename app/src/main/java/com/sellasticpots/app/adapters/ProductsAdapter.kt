package com.sellasticpots.app.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sellasticpots.app.ProductDetailActivity
import com.sellasticpots.app.databinding.ItemProductBinding
import com.sellasticpots.app.models.Product
import com.sellasticpots.app.utils.ReviewManager

class ProductsAdapter(
    private var products: List<Product>,
    private val onAddToCart: (Product, Int) -> Unit
) : RecyclerView.Adapter<ProductsAdapter.ProductViewHolder>() {

    private val quantities = mutableMapOf<String, Int>()

    inner class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.productName.text = product.name
            binding.productPrice.text = "₹${product.price}"

            // Load dynamic rating from Firebase
            loadProductRating(product.id)

            val quantity = quantities[product.id] ?: 1
            binding.quantityText.text = quantity.toString()
            updateDecreaseButtonState(quantity)

            binding.root.setOnClickListener {
                val intent = Intent(binding.root.context, ProductDetailActivity::class.java)
                intent.putExtra("product", product)
                binding.root.context.startActivity(intent)
            }

            binding.btnDecrease.setOnClickListener {
                val currentQty = quantities[product.id] ?: 1
                if (currentQty > 1) {
                    quantities[product.id] = currentQty - 1
                    binding.quantityText.text = (currentQty - 1).toString()
                    updateDecreaseButtonState(currentQty - 1)
                }
            }

            binding.btnIncrease.setOnClickListener {
                val currentQty = quantities[product.id] ?: 1
                quantities[product.id] = currentQty + 1
                binding.quantityText.text = (currentQty + 1).toString()
                updateDecreaseButtonState(currentQty + 1)
            }

            binding.btnAddToCart.setOnClickListener {
                val currentQty = quantities[product.id] ?: 1
                onAddToCart(product, currentQty)
            }
        }

        private fun loadProductRating(productId: String) {
            // Show loading state
            binding.productRating.text = "Loading..."

            ReviewManager.getReviewsOnce(productId) { reviews ->
                if (reviews.isEmpty()) {
                    binding.productRating.text = "No reviews yet"
                } else {
                    val avgRating = ReviewManager.calculateAverageRating(reviews)
                    val reviewCount = reviews.size
                    binding.productRating.text = String.format("%.1f ⭐ (%d)", avgRating, reviewCount)
                }
            }
        }

        private fun updateDecreaseButtonState(quantity: Int) {
            if (quantity <= 1) {
                binding.btnDecrease.isEnabled = false
                binding.btnDecrease.alpha = 0.3f
            } else {
                binding.btnDecrease.isEnabled = true
                binding.btnDecrease.alpha = 1.0f
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount() = products.size

    fun updateProducts(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }
}

