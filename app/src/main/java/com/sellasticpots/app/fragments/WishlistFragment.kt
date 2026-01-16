package com.sellasticpots.app.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.sellasticpots.app.ProductDetailActivity
import com.sellasticpots.app.adapters.WishlistAdapter
import com.sellasticpots.app.databinding.FragmentWishlistBinding
import com.sellasticpots.app.models.Product
import com.sellasticpots.app.utils.CartManager
import com.sellasticpots.app.utils.WishlistManager
import android.widget.Toast

class WishlistFragment : Fragment() {

    private var _binding: FragmentWishlistBinding? = null
    private val binding get() = _binding!!
    private lateinit var wishlistAdapter: WishlistAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWishlistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeWishlist()
    }

    private fun setupRecyclerView() {
        wishlistAdapter = WishlistAdapter(
            onItemClick = { product ->
                // Open product detail
                val intent = Intent(requireContext(), ProductDetailActivity::class.java)
                intent.putExtra("product", product)
                startActivity(intent)
            },
            onRemoveClick = { product ->
                // Remove from wishlist
                WishlistManager.removeFromWishlist(
                    productId = product.id,
                    onSuccess = {
                        Toast.makeText(requireContext(), "Removed from wishlist", Toast.LENGTH_SHORT).show()
                    },
                    onError = { error ->
                        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                    }
                )
            },
            onMoveToCartClick = { product ->
                // Move to cart
                CartManager.addToCart(product, 1)
                WishlistManager.removeFromWishlist(
                    productId = product.id,
                    onSuccess = {
                        Toast.makeText(requireContext(), "Moved to cart", Toast.LENGTH_SHORT).show()
                    },
                    onError = { error ->
                        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )

        binding.wishlistRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = wishlistAdapter
        }
    }

    private fun observeWishlist() {
        WishlistManager.wishlistItems.observe(viewLifecycleOwner) { items ->
            if (items.isEmpty()) {
                binding.emptyWishlistLayout.visibility = View.VISIBLE
                binding.wishlistRecyclerView.visibility = View.GONE
            } else {
                binding.emptyWishlistLayout.visibility = View.GONE
                binding.wishlistRecyclerView.visibility = View.VISIBLE

                // Convert WishlistItems to Products
                val products = items.map { it.product }
                wishlistAdapter.updateWishlist(products)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
