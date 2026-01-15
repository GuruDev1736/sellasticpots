package com.sellasticpots.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.sellasticpots.app.adapters.ProductsAdapter
import com.sellasticpots.app.databinding.FragmentProductsBinding
import com.sellasticpots.app.models.Product

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
        loadSampleProducts()
        setupCategoryChips()
    }

    private fun setupRecyclerView() {
        productsAdapter = ProductsAdapter(allProducts) { product ->
            Toast.makeText(requireContext(), "${product.name} added to cart!", Toast.LENGTH_SHORT).show()
        }

        binding.productsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = productsAdapter
        }
    }

    private fun loadSampleProducts() {
        // Sample products
        allProducts.clear()
        allProducts.addAll(
            listOf(
                Product(
                    id = "1",
                    name = "Terracotta Matka",
                    price = 42.00,
                    rating = 4.8,
                    reviews = 124,
                    category = "Matkas",
                    imageUrl = "",
                    description = "Handcrafted by master potters from the Kutch region, this matka is made from 100% natural organic clay. Each piece is hand-painted with traditional motifs using lead-free pigments. The porous nature of the clay allows for natural evaporation, cooling water perfectly for a refreshing drink.",
                    images = listOf("", "", ""),
                    tags = listOf("100% ORGANIC", "LEAD FREE"),
                    freeDelivery = true
                ),
                Product(
                    id = "2",
                    name = "Minimal Clay Vase",
                    price = 38.50,
                    rating = 4.9,
                    reviews = 86,
                    category = "Vases",
                    imageUrl = "",
                    description = "A stunning minimalist vase crafted from premium clay. Perfect for displaying your favorite flowers or as a standalone decorative piece. The elegant design complements any interior style.",
                    images = listOf("", ""),
                    tags = listOf("HANDCRAFTED", "ECO-FRIENDLY"),
                    freeDelivery = true
                ),
                Product(
                    id = "3",
                    name = "Traditional Tea Set",
                    price = 65.00,
                    rating = 4.7,
                    reviews = 215,
                    category = "Dinnerware",
                    imageUrl = "",
                    description = "Complete tea serving set handcrafted from natural clay. Includes teapot and 4 cups. Each piece features intricate traditional patterns. Perfect for serving tea in authentic style.",
                    images = listOf("", "", "", ""),
                    tags = listOf("COMPLETE SET", "TRADITIONAL"),
                    freeDelivery = true
                ),
                Product(
                    id = "4",
                    name = "Floral Wall Plate",
                    price = 29.99,
                    rating = 5.0,
                    reviews = 52,
                    category = "Dinnerware",
                    imageUrl = "",
                    description = "Decorative wall plate with beautiful hand-painted floral motifs. Made from high-quality terracotta and finished with a protective glaze. Adds a touch of artisanal elegance to any wall.",
                    images = listOf("", ""),
                    tags = listOf("DECORATIVE", "HANDPAINTED"),
                    freeDelivery = true
                ),
                Product(
                    id = "5",
                    name = "Glazed Soup Bowls",
                    price = 18.00,
                    rating = 4.6,
                    reviews = 112,
                    category = "Dinnerware",
                    imageUrl = "",
                    description = "Set of 4 soup bowls with beautiful glazed finish. Microwave and dishwasher safe. Perfect size for soups, cereals, and desserts.",
                    images = listOf(""),
                    tags = listOf("SET OF 4", "DISHWASHER SAFE"),
                    freeDelivery = true
                ),
                Product(
                    id = "6",
                    name = "Rustic Vase",
                    price = 45.00,
                    rating = 4.8,
                    reviews = 98,
                    category = "Vases",
                    imageUrl = "",
                    description = "Hand-crafted rustic vase with natural clay texture. Each piece is unique with its own character. Perfect for dried flowers or as a statement piece.",
                    images = listOf("", ""),
                    tags = listOf("RUSTIC", "UNIQUE"),
                    freeDelivery = true
                ),
                Product(
                    id = "7",
                    name = "Clay Water Pot",
                    price = 35.00,
                    rating = 4.7,
                    reviews = 145,
                    category = "Matkas",
                    imageUrl = "",
                    description = "Traditional clay water pot that keeps water naturally cool. Made from porous clay that allows for evaporation cooling. Eco-friendly alternative to plastic bottles.",
                    images = listOf("", ""),
                    tags = listOf("ECO-FRIENDLY", "COOLING"),
                    freeDelivery = true
                ),
                Product(
                    id = "8",
                    name = "Ceramic Dinner Set",
                    price = 120.00,
                    rating = 4.9,
                    reviews = 203,
                    category = "Dinnerware",
                    imageUrl = "",
                    description = "Complete 16-piece dinner set handcrafted from premium ceramic. Includes 4 dinner plates, 4 side plates, 4 bowls, and 4 cups. Elegant design suitable for everyday use and special occasions.",
                    images = listOf("", "", ""),
                    tags = listOf("16-PIECE SET", "PREMIUM"),
                    freeDelivery = true
                )
            )
        )
        productsAdapter.updateProducts(allProducts)
    }

    private fun setupCategoryChips() {
        // Set up chip listeners
        binding.chipAllItems.setOnClickListener {
            filterProducts("All")
        }

        binding.chipVases.setOnClickListener {
            filterProducts("Vases")
        }

        binding.chipMatkas.setOnClickListener {
            filterProducts("Matkas")
        }

        binding.chipDinnerware.setOnClickListener {
            filterProducts("Dinnerware")
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

