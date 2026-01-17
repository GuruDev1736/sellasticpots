package com.sellasticpots.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.sellasticpots.app.adapters.CartAdapter
import com.sellasticpots.app.databinding.FragmentMyCartBinding
import com.sellasticpots.app.utils.CartManager

class MyCartFragment : Fragment() {

    private var _binding: FragmentMyCartBinding? = null
    private val binding get() = _binding!!
    private lateinit var cartAdapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeCartData()
        setupCheckoutButton()
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            onQuantityChange = { productId, newQuantity ->
                CartManager.updateQuantity(productId, newQuantity)
            },
            onRemoveItem = { productId ->
                CartManager.removeFromCart(productId)
            }
        )

        binding.cartRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cartAdapter
        }
    }

    private fun observeCartData() {
        CartManager.cartItems.observe(viewLifecycleOwner) { items ->
            if (items.isEmpty()) {
                binding.emptyCartLayout.visibility = View.VISIBLE
                binding.cartContentLayout.visibility = View.GONE
            } else {
                binding.emptyCartLayout.visibility = View.GONE
                binding.cartContentLayout.visibility = View.VISIBLE
                cartAdapter.updateItems(items)
            }
        }

        CartManager.totalPrice.observe(viewLifecycleOwner) { total ->
            binding.subtotalValue.text = String.format("₹%.2f", total)
            // For now, delivery is free
            binding.deliveryValue.text = "₹0.00"
            binding.totalValue.text = String.format("₹%.2f", total)
        }
    }

    private fun setupCheckoutButton() {
        binding.btnCheckout.setOnClickListener {
            // TODO: Implement checkout functionality
            android.widget.Toast.makeText(
                requireContext(),
                "Checkout feature coming soon!",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
