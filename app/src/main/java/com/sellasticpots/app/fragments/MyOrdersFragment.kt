package com.sellasticpots.app.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.sellasticpots.app.OrderDetailActivity
import com.sellasticpots.app.adapters.OrdersAdapter
import com.sellasticpots.app.databinding.FragmentMyOrdersBinding
import com.sellasticpots.app.models.Order
import com.sellasticpots.app.utils.OrderManager

class MyOrdersFragment : Fragment() {

    private var _binding: FragmentMyOrdersBinding? = null
    private val binding get() = _binding!!
    private lateinit var ordersAdapter: OrdersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeOrders()
    }

    private fun setupRecyclerView() {
        ordersAdapter = OrdersAdapter(
            onOrderClick = { order ->
                val intent = Intent(requireContext(), OrderDetailActivity::class.java)
                intent.putExtra("order", order)
                startActivity(intent)
            },
            onCancelOrder = { order ->
                showCancelOrderDialog(order)
            }
        )

        binding.ordersRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ordersAdapter
        }
    }

    private fun showCancelOrderDialog(order: Order) {
        val formattedOrderId = formatOrderId(order.orderId)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Cancel Order")
            .setMessage("Are you sure you want to cancel $formattedOrderId?\n\nThis action cannot be undone.")
            .setPositiveButton("Yes, Cancel") { dialog, _ ->
                cancelOrder(order)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(
            resources.getColor(com.sellasticpots.app.R.color.secondary, null)
        )
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(
            resources.getColor(com.sellasticpots.app.R.color.secondary, null)
        )
    }

    private fun cancelOrder(order: Order) {
        OrderManager.cancelOrder(
            orderId = order.orderId,
            onSuccess = {
                Toast.makeText(
                    requireContext(),
                    "Order cancelled successfully",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onError = { error ->
                Toast.makeText(
                    requireContext(),
                    "Error: $error",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    private fun formatOrderId(orderId: String): String {
        val numericPart = orderId.hashCode().toString().takeLast(5).padStart(5, '0')
        return "ORD-$numericPart"
    }

    private fun observeOrders() {
        OrderManager.orders.observe(viewLifecycleOwner) { orders ->
            if (orders.isEmpty()) {
                binding.emptyOrdersLayout.visibility = View.VISIBLE
                binding.ordersRecyclerView.visibility = View.GONE
            } else {
                binding.emptyOrdersLayout.visibility = View.GONE
                binding.ordersRecyclerView.visibility = View.VISIBLE
                ordersAdapter.updateOrders(orders)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
