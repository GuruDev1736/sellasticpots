package com.sellasticpots.app.fragments

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.sellasticpots.app.CheckoutActivity
import com.sellasticpots.app.adapters.CartAdapter
import com.sellasticpots.app.adapters.CheckoutSummaryAdapter
import com.sellasticpots.app.databinding.DialogCheckoutConfirmationBinding
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
            val cartItems = CartManager.cartItems.value
            if (cartItems.isNullOrEmpty()) {
                android.widget.Toast.makeText(
                    requireContext(),
                    "Your cart is empty",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            showCheckoutConfirmationDialog(cartItems)
        }
    }

    private fun showCheckoutConfirmationDialog(cartItems: List<com.sellasticpots.app.models.CartItem>) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val dialogBinding = DialogCheckoutConfirmationBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        val adapter = CheckoutSummaryAdapter(cartItems)
        dialogBinding.checkoutItemsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }

        val totalItems = cartItems.sumOf { it.quantity }
        val totalPrice = CartManager.totalPrice.value ?: 0.0

        dialogBinding.totalItemsText.text = totalItems.toString()
        dialogBinding.totalPriceText.text = String.format("₹%.2f", totalPrice)

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnConfirm.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(requireContext(), CheckoutActivity::class.java)
            intent.putParcelableArrayListExtra("cartItems", ArrayList(cartItems))
            intent.putExtra("totalPrice", totalPrice)
            startActivity(intent)
        }

        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
