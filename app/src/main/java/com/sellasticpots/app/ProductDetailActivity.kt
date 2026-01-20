package com.sellasticpots.app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.sellasticpots.app.databinding.ActivityProductDetailBinding
import com.sellasticpots.app.databinding.ItemProductImageBinding
import com.sellasticpots.app.models.Product
import com.sellasticpots.app.models.Review
import com.sellasticpots.app.adapters.ReviewsAdapter
import com.sellasticpots.app.utils.ReviewManager
import com.sellasticpots.app.utils.CartManager
import com.sellasticpots.app.utils.WishlistManager

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductDetailBinding
    private lateinit var product: Product
    private var quantity = 1
    private var selectedRating = 0f
    private lateinit var reviewsAdapter: ReviewsAdapter
    private val reviews = mutableListOf<Review>()

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
        setupReviewsRecyclerView()
        loadReviews()
        updateAddToCartButton()

        // Observe cart changes to update button
        CartManager.cartItems.observe(this) {
            updateAddToCartButton()
        }

        // Observe wishlist changes to update button
        WishlistManager.wishlistItems.observe(this) {
            updateWishlistButton()
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        // Setup wishlist button
        updateWishlistButton()
        binding.btnFavorite.setOnClickListener {
            toggleWishlist()
        }
    }

    private fun setupProductDetails() {
        binding.productCategory.text = product.category.uppercase()
        binding.productName.text = product.name
        binding.productRating.text = "${product.rating} (${product.reviews} reviews)"
        binding.productPrice.text = "₹${product.price}"
        binding.totalPrice.text = "₹${product.price * quantity}"
        binding.productDescription.text = product.description
    }

    private fun setupImageViewPager() {
        val images = product.images.ifEmpty {
            listOf("")
        }

        val imageAdapter = ProductImageAdapter(images)
        binding.imagesViewpager.adapter = imageAdapter
    }

    private fun setupQuantityControls() {
        binding.quantityText.text = quantity.toString()
        updateDecreaseButtonState()

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
        binding.totalPrice.text = "₹${String.format("%.2f", product.price * quantity)}"
        updateDecreaseButtonState()
    }

    private fun updateDecreaseButtonState() {
        if (quantity <= 1) {
            binding.btnDecrease.isEnabled = false
            binding.btnDecrease.alpha = 0.3f
        } else {
            binding.btnDecrease.isEnabled = true
            binding.btnDecrease.alpha = 1.0f
        }
    }

    private fun setupButtons() {
        binding.btnAddToCartBottom.setOnClickListener {
            addToCart()
        }

        binding.btnSubmitReview.setOnClickListener {
            submitReview()
        }

        binding.btnSeeAllReviews.setOnClickListener {
            // Open AllReviewsActivity to show all reviews
            val intent = Intent(this, AllReviewsActivity::class.java)
            intent.putExtra("product", product)
            startActivity(intent)
        }

        // Interactive rating stars
        setupRatingStars()
    }

    private fun setupRatingStars() {
        updateRatingStars()

        binding.ratingStars.setOnClickListener {
            // Cycle through ratings 1-5
            selectedRating = if (selectedRating < 5f) selectedRating + 1f else 1f
            updateRatingStars()
        }
    }

    private fun updateRatingStars() {
        val fullStars = selectedRating.toInt()
        val emptyStars = 5 - fullStars
        val stars = "★".repeat(fullStars) + "☆".repeat(emptyStars)
        binding.ratingStars.text = stars
        binding.ratingStars.setTextColor(
            if (selectedRating > 0)
                resources.getColor(android.R.color.holo_orange_light, null)
            else
                resources.getColor(R.color.text_gray, null)
        )
    }

    private fun addToCart() {
        val currentCart = CartManager.cartItems.value ?: mutableListOf()
        val existingItem = currentCart.find { it.product.id == product.id }

        if (existingItem != null) {
            Snackbar.make(
                findViewById(android.R.id.content),
                "Product already in cart with ${existingItem.quantity} items",
                Snackbar.LENGTH_LONG
            ).apply {
                setAction("VIEW CART") {
                    val intent = Intent(this@ProductDetailActivity, MainActivity::class.java).apply {
                        putExtra("openCart", true)
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    }
                    startActivity(intent)
                    finish()
                }
                setActionTextColor(resources.getColor(R.color.secondary, null))
                show()
            }
        } else {
            CartManager.addToCart(product, quantity)

            Toast.makeText(
                this,
                "$quantity x ${product.name} added to cart!",
                Toast.LENGTH_SHORT
            ).show()

            showAddToCartConfirmation()
        }
    }

    private fun showAddToCartConfirmation() {
        Snackbar.make(
            findViewById(android.R.id.content),
            "Added to cart successfully!",
            Snackbar.LENGTH_LONG
        ).apply {
            setAction("VIEW CART") {
                val intent = Intent(this@ProductDetailActivity, MainActivity::class.java).apply {
                    putExtra("openCart", true)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                startActivity(intent)
                finish()
            }
            setActionTextColor(resources.getColor(R.color.secondary, null))
            show()
        }
    }

    private fun updateAddToCartButton() {
        val currentCart = CartManager.cartItems.value ?: mutableListOf()
        val existingItem = currentCart.find { it.product.id == product.id }

        if (existingItem != null) {
            binding.btnAddToCartBottom.text = "View in Cart (${existingItem.quantity})"
            binding.btnAddToCartBottom.icon = resources.getDrawable(android.R.drawable.ic_menu_view, null)
        } else {
            binding.btnAddToCartBottom.text = "Add to Cart"
            binding.btnAddToCartBottom.icon = resources.getDrawable(R.drawable.ic_cart, null)
        }
    }

    private fun submitReview() {
        val reviewText = binding.reviewText.text.toString().trim()

        binding.btnSubmitReview.isEnabled = false
        binding.btnSubmitReview.text = "Submitting..."

        ReviewManager.submitReview(
            productId = product.id,
            rating = selectedRating,
            reviewText = reviewText,
            onSuccess = {
                binding.btnSubmitReview.isEnabled = true
                binding.btnSubmitReview.text = "Submit Review"
                loadReviews()
                Toast.makeText(this, "Review submitted! Thank you!", Toast.LENGTH_SHORT).show()
                binding.reviewText.text?.clear()
                selectedRating = 0f
                updateRatingStars()
            },
            onError = { error ->
                binding.btnSubmitReview.isEnabled = true
                binding.btnSubmitReview.text = "Submit Review"
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun setupReviewsRecyclerView() {
        reviewsAdapter = ReviewsAdapter(reviews)
        binding.reviewsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ProductDetailActivity)
            adapter = reviewsAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun loadReviews() {
        binding.reviewsProgressBar.visibility = android.view.View.VISIBLE

        ReviewManager.getReviews(product.id) { loadedReviews ->
            binding.reviewsProgressBar.visibility = android.view.View.GONE

            reviews.clear()
            reviews.addAll(loadedReviews)

            val limitedReviews = reviews.take(3)
            reviewsAdapter.updateReviews(limitedReviews)

            if (reviews.isNotEmpty()) {
                val avgRating = ReviewManager.calculateAverageRating(reviews)
                binding.productRating.text = String.format("%.1f (%d reviews)", avgRating, reviews.size)
            }

            updateReviewsVisibility()
        }
    }

    private fun updateReviewsVisibility() {
        if (reviews.isEmpty()) {
            binding.reviewsRecyclerView.visibility = android.view.View.GONE
            binding.noReviewsText.visibility = android.view.View.VISIBLE
        } else {
            binding.reviewsRecyclerView.visibility = android.view.View.VISIBLE
            binding.noReviewsText.visibility = android.view.View.GONE
        }
    }

    private fun toggleWishlist() {
        WishlistManager.toggleWishlist(
            product = product,
            onSuccess = { isAdded ->
                updateWishlistButton()
                Toast.makeText(
                    this,
                    if (isAdded) "Added to wishlist" else "Removed from wishlist",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onError = { error ->
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun updateWishlistButton() {
        val isInWishlist = WishlistManager.isInWishlist(product.id)
        binding.btnFavorite.setImageResource(
            if (isInWishlist) android.R.drawable.btn_star_big_on
            else android.R.drawable.btn_star_big_off
        )
    }

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
            holder.binding.imageView.setImageResource(R.drawable.bg_image_1)
            holder.binding.imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        }

        override fun getItemCount() = if (images.isEmpty()) 1 else images.size
    }
}
