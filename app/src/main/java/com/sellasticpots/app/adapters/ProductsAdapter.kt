package com.sellasticpots.app.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sellasticpots.app.ProductDetailActivity
import com.sellasticpots.app.databinding.ItemProductBinding
import com.sellasticpots.app.models.Product

class ProductsAdapter(
    private var products: List<Product>,
    private val onAddToCart: (Product) -> Unit
) : RecyclerView.Adapter<ProductsAdapter.ProductViewHolder>() {

    private val quantities = mutableMapOf<String, Int>()

    inner class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.productName.text = product.name
            binding.productPrice.text = "$${product.price}"
            binding.productRating.text = "${product.rating} (${product.reviews})"

            // Initialize quantity
            val quantity = quantities[product.id] ?: 1
            binding.quantityText.text = quantity.toString()

            // Click on product card to view details
            binding.root.setOnClickListener {
                val intent = Intent(binding.root.context, ProductDetailActivity::class.java)
                intent.putExtra("product", product)
                binding.root.context.startActivity(intent)
            }

            // Decrease quantity
            binding.btnDecrease.setOnClickListener {
                val currentQty = quantities[product.id] ?: 1
                if (currentQty > 1) {
                    quantities[product.id] = currentQty - 1
                    binding.quantityText.text = (currentQty - 1).toString()
                }
            }

            // Increase quantity
            binding.btnIncrease.setOnClickListener {
                val currentQty = quantities[product.id] ?: 1
                quantities[product.id] = currentQty + 1
                binding.quantityText.text = (currentQty + 1).toString()
            }

            // Add to cart
            binding.btnAddToCart.setOnClickListener {
                onAddToCart(product)
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

