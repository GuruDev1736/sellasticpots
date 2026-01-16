package com.sellasticpots.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sellasticpots.app.databinding.ItemReviewBinding
import com.sellasticpots.app.models.Review

class ReviewsAdapter(
    private var reviews: List<Review>
) : RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder>() {

    inner class ReviewViewHolder(private val binding: ItemReviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(review: Review) {
            binding.reviewerName.text = review.userName
            binding.reviewDate.text = review.getFormattedDate()
            binding.reviewRating.text = review.getStarRating()
            binding.reviewText.text = "\"${review.reviewText}\""

            binding.verifiedBadge.visibility = if (review.isVerifiedPurchase) {
                android.view.View.VISIBLE
            } else {
                android.view.View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemReviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(reviews[position])
    }

    override fun getItemCount() = reviews.size

    fun updateReviews(newReviews: List<Review>) {
        reviews = newReviews
        notifyDataSetChanged()
    }
}
