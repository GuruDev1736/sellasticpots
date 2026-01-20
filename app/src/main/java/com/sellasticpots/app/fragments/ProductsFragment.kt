package com.sellasticpots.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.sellasticpots.app.R
import com.sellasticpots.app.adapters.ProductsAdapter
import com.sellasticpots.app.databinding.FragmentProductsBinding
import com.sellasticpots.app.models.Product
import com.sellasticpots.app.utils.CartManager

class ProductsFragment : Fragment() {

    private var _binding: FragmentProductsBinding? = null
    private val binding get() = _binding!!
    private lateinit var productsAdapter: ProductsAdapter
    private val allProducts = mutableListOf<Product>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSwipeRefresh()
        loadSampleProducts()
        setupCategoryChips()
    }

    private fun setupRecyclerView() {
        productsAdapter = ProductsAdapter(allProducts) { product, quantity ->
            CartManager.addToCart(product, quantity)
            Toast.makeText(requireContext(), "${product.name} added to cart!", Toast.LENGTH_SHORT).show()
        }

        binding.productsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = productsAdapter
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setColorSchemeResources(
            R.color.secondary,
            R.color.primary,
            R.color.text_dark
        )

        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshProducts()
        }
    }

    private fun refreshProducts() {
        // Reload products and refresh adapter to get updated ratings
        loadSampleProducts()

        // Notify adapter to rebind all items (this will trigger rating fetch)
        productsAdapter.notifyDataSetChanged()

        // Stop refresh animation after a short delay
        binding.swipeRefreshLayout.postDelayed({
            binding.swipeRefreshLayout.isRefreshing = false
            Toast.makeText(requireContext(), "Products refreshed!", Toast.LENGTH_SHORT).show()
        }, 1000)
    }

    private fun loadSampleProducts() {
        allProducts.clear()
        allProducts.addAll(
            listOf(
                Product(
                    id = "1",
                    name = "Cooking Pots",
                    price = 599.00,
                    rating = 4.8,
                    reviews = 156,
                    category = "Cooking",
                    imageUrl = "android.resource://com.sellasticpots.app/" + R.drawable.cooking_pot,
                    description = "Traditional clay cooking pots perfect for slow-cooking curries, dals, rice, and more. Experience the authentic taste of home cooked meals.",
                    images = listOf(
                        "android.resource://com.sellasticpots.app/" + R.drawable.cooking_pot,
                        "android.resource://com.sellasticpots.app/" + R.drawable.cooking_pot,
                        "android.resource://com.sellasticpots.app/" + R.drawable.cooking_pot
                    ),
                    tags = listOf("TRADITIONAL", "AUTHENTIC TASTE"),
                    freeDelivery = true
                ),
                Product(
                    id = "2",
                    name = "Water Pots (Matka)",
                    price = 449.00,
                    rating = 4.9,
                    reviews = 203,
                    category = "Matkas",
                    imageUrl = "android.resource://com.sellasticpots.app/" + R.drawable.water_pot,
                    description = "Traditional matka water pots that naturally cool water and add beneficial minerals. The healthy, eco-friendly way to stay hydrated.",
                    images = listOf(
                        "android.resource://com.sellasticpots.app/" + R.drawable.water_pot,
                        "android.resource://com.sellasticpots.app/" + R.drawable.water_pot
                    ),
                    tags = listOf("ECO-FRIENDLY", "NATURAL COOLING"),
                    freeDelivery = true
                ),
                Product(
                    id = "3",
                    name = "Serving Bowls",
                    price = 499.00,
                    rating = 4.9,
                    reviews = 187,
                    category = "Matkas",
                    imageUrl = "android.resource://com.sellasticpots.app/" + R.drawable.serving_bowls,
                    description = "Beautiful earthen bowls for serving traditional Indian meals. Keep your food warm longer while adding rustic charm to your dining table.",
                    images = listOf(
                        "android.resource://com.sellasticpots.app/" + R.drawable.serving_bowls,
                        "android.resource://com.sellasticpots.app/" + R.drawable.serving_bowls,
                        "android.resource://com.sellasticpots.app/" + R.drawable.serving_bowls
                    ),
                    tags = listOf("HEALTHY", "MINERAL RICH"),
                    freeDelivery = true
                ),
                Product(
                    id = "4",
                    name = "Decorative Pots",
                    price = 799.00,
                    rating = 5.0,
                    reviews = 124,
                    category = "Decorative",
                    imageUrl = "android.resource://com.sellasticpots.app/" + R.drawable.decorative_pot,
                    description = "Hand-painted and artistically designed earthen pots for home décor. Add a touch of traditional elegance to any space.",
                    images = listOf(
                        "android.resource://com.sellasticpots.app/" + R.drawable.decorative_pot,
                        "android.resource://com.sellasticpots.app/" + R.drawable.decorative_pot,
                        "android.resource://com.sellasticpots.app/" + R.drawable.decorative_pot
                    ),
                    tags = listOf("HAND-PAINTED", "ELEGANT"),
                    freeDelivery = true
                )
            )
        )
        productsAdapter.updateProducts(allProducts)
    }

    private fun setupCategoryChips() {
        binding.chipAllItems.setOnClickListener {
            filterProducts("All")
        }

        binding.chipVases.setOnClickListener {
            filterProducts("Cooking")
        }

        binding.chipMatkas.setOnClickListener {
            filterProducts("Matkas")
        }

        binding.chipDinnerware.setOnClickListener {
            filterProducts("Decorative")
        }
    }

    private fun filterProducts(category: String) {
        val filteredProducts = if (category == "All") {
            allProducts
        } else {
            allProducts.filter { it.category == category }
        }
        productsAdapter.updateProducts(filteredProducts)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
