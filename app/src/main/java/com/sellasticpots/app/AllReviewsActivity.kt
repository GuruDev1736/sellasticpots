package com.sellasticpots.app

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import com.sellasticpots.app.adapters.ReviewsAdapter
import com.sellasticpots.app.databinding.ActivityAllReviewsBinding
import com.sellasticpots.app.models.Product
import com.sellasticpots.app.models.Review
import com.sellasticpots.app.utils.ReviewManager

class AllReviewsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAllReviewsBinding
    private lateinit var reviewsAdapter: ReviewsAdapter
    private val reviews = mutableListOf<Review>()
    private lateinit var product: Product

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding = ActivityAllReviewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        product = intent.getParcelableExtra("product") ?: run {
            finish()
            return
        }

        setupToolbar()
        setupRecyclerView()
        loadAllReviews()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.toolbarTitle.text = "All Reviews"
    }

    private fun setupRecyclerView() {
        reviewsAdapter = ReviewsAdapter(reviews)
        binding.reviewsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@AllReviewsActivity)
            adapter = reviewsAdapter
        }
    }

    private fun loadAllReviews() {
        binding.progressBar.visibility = View.VISIBLE

        ReviewManager.getReviews(product.id) { loadedReviews ->
            binding.progressBar.visibility = View.GONE

            reviews.clear()
            reviews.addAll(loadedReviews)
            reviewsAdapter.updateReviews(reviews)

            updateReviewStats()

            if (reviews.isEmpty()) {
                binding.emptyStateLayout.visibility = View.VISIBLE
                binding.reviewsRecyclerView.visibility = View.GONE
                binding.statsCard.visibility = View.GONE
            } else {
                binding.emptyStateLayout.visibility = View.GONE
                binding.reviewsRecyclerView.visibility = View.VISIBLE
                binding.statsCard.visibility = View.VISIBLE
            }
        }
    }

    private fun updateReviewStats() {
        if (reviews.isEmpty()) return

        val avgRating = ReviewManager.calculateAverageRating(reviews)
        val distribution = ReviewManager.getRatingDistribution(reviews)

        binding.averageRating.text = String.format("%.1f", avgRating)
        binding.totalReviews.text = "${reviews.size} Reviews"
        binding.ratingStars.text = getStarString(avgRating)

        val total = reviews.size
        binding.rating5Count.text = "${distribution[5] ?: 0}"
        binding.rating5Bar.progress = ((distribution[5] ?: 0) * 100 / total.coerceAtLeast(1))

        binding.rating4Count.text = "${distribution[4] ?: 0}"
        binding.rating4Bar.progress = ((distribution[4] ?: 0) * 100 / total.coerceAtLeast(1))

        binding.rating3Count.text = "${distribution[3] ?: 0}"
        binding.rating3Bar.progress = ((distribution[3] ?: 0) * 100 / total.coerceAtLeast(1))

        binding.rating2Count.text = "${distribution[2] ?: 0}"
        binding.rating2Bar.progress = ((distribution[2] ?: 0) * 100 / total.coerceAtLeast(1))

        binding.rating1Count.text = "${distribution[1] ?: 0}"
        binding.rating1Bar.progress = ((distribution[1] ?: 0) * 100 / total.coerceAtLeast(1))
    }

    private fun getStarString(rating: Float): String {
        val fullStars = rating.toInt()
        val hasHalfStar = (rating - fullStars) >= 0.5f
        return "★".repeat(fullStars) +
               (if (hasHalfStar) "⭐" else "") +
               "☆".repeat(5 - fullStars - (if (hasHalfStar) 1 else 0))
    }
}
