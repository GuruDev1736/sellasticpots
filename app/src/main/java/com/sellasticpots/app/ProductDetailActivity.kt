package com.sellasticpots.app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.RecyclerView
import com.sellasticpots.app.databinding.ActivityProductDetailBinding
import com.sellasticpots.app.databinding.ItemProductImageBinding
import com.sellasticpots.app.models.Product

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductDetailBinding
    private lateinit var product: Product
    private var quantity = 1
    private var isFavorite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Force light mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get product from intent
        product = intent.getParcelableExtra("product") ?: run {
            finish()
            return
        }

        setupToolbar()
        setupProductDetails()
        setupImageViewPager()
        setupQuantityControls()
        setupButtons()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.btnShare.setOnClickListener {
            shareProduct()
        }

        binding.btnFavorite.setOnClickListener {
            toggleFavorite()
        }
    }

    private fun setupProductDetails() {
        binding.productCategory.text = product.category.uppercase()
        binding.productName.text = product.name
        binding.productRating.text = "${product.rating} (${product.reviews} reviews)"
        binding.productPrice.text = "$${product.price}"
        binding.totalPrice.text = "$${product.price * quantity}"
        binding.productDescription.text = product.description
    }

    private fun setupImageViewPager() {
        // If product has no images, use placeholder
        val images = if (product.images.isEmpty()) {
            listOf("") // Placeholder
        } else {
            product.images
        }

        val imageAdapter = ProductImageAdapter(images)
        binding.imagesViewpager.adapter = imageAdapter
    }

    private fun setupQuantityControls() {
        binding.quantityText.text = quantity.toString()

        binding.btnDecrease.setOnClickListener {
            if (quantity > 1) {
                quantity--
                updateQuantity()
            }
        }

        binding.btnIncrease.setOnClickListener {
            quantity++
            updateQuantity()
        }
    }

    private fun updateQuantity() {
        binding.quantityText.text = quantity.toString()
        binding.totalPrice.text = "$${String.format("%.2f", product.price * quantity)}"
    }

    private fun setupButtons() {
        binding.btnAddToCartBottom.setOnClickListener {
            addToCart()
        }

        binding.btnSubmitReview.setOnClickListener {
            submitReview()
        }

        binding.btnSeeAllReviews.setOnClickListener {
            Toast.makeText(this, "See all reviews coming soon", Toast.LENGTH_SHORT).show()
        }

        // Simple rating system
        var selectedRating = 0
        binding.ratingStars.setOnClickListener {
            selectedRating = if (selectedRating < 5) selectedRating + 1 else 1
            binding.ratingStars.text = "★".repeat(selectedRating) + "☆".repeat(5 - selectedRating)
        }
    }

    private fun addToCart() {
        Toast.makeText(
            this,
            "$quantity x ${product.name} added to cart!",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun submitReview() {
        val reviewText = binding.reviewText.text.toString()
        if (reviewText.isNotEmpty()) {
            Toast.makeText(this, "Review submitted! Thank you!", Toast.LENGTH_SHORT).show()
            binding.reviewText.text?.clear()
        } else {
            Toast.makeText(this, "Please write a review", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareProduct() {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Check out ${product.name} for $${product.price}!")
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }

    private fun toggleFavorite() {
        isFavorite = !isFavorite
        binding.btnFavorite.setImageResource(
            if (isFavorite) android.R.drawable.btn_star_big_on
            else android.R.drawable.btn_star_big_off
        )
        Toast.makeText(
            this,
            if (isFavorite) "Added to favorites" else "Removed from favorites",
            Toast.LENGTH_SHORT
        ).show()
    }

    // ViewPager Adapter for product images
    inner class ProductImageAdapter(private val images: List<String>) :
        RecyclerView.Adapter<ProductImageAdapter.ImageViewHolder>() {

        inner class ImageViewHolder(val binding: ItemProductImageBinding) :
            RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
            val binding = ItemProductImageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ImageViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
            // For now, just show placeholder image
            // In a real app, you would load the image from URL using Glide or Coil
            holder.binding.imageView.setImageResource(R.drawable.bg_image_1)
            holder.binding.imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        }

        override fun getItemCount() = if (images.isEmpty()) 1 else images.size
    }
}

