package com.sellasticpots.app.fragments

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.database.FirebaseDatabase
import com.sellasticpots.app.ContactUsActivity
import com.sellasticpots.app.LoginActivity
import com.sellasticpots.app.R
import com.sellasticpots.app.databinding.FragmentProfileBinding
import com.sellasticpots.app.models.User

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        loadUserData()
        setupClickListeners()
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser ?: return

        binding.userEmail.text = currentUser.email ?: "No email"

        database.reference.child("users").child(currentUser.uid).get()
            .addOnSuccessListener { snapshot ->
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    binding.userName.text = user.fullName.ifEmpty {
                        user.username.ifEmpty { "Guest User" }
                    }
                    binding.profileImage.setImageResource(R.drawable.ic_profile)
                } else {
                    binding.userName.text = currentUser.displayName
                        ?: currentUser.email?.substringBefore("@")
                        ?: "Guest User"
                    binding.profileImage.setImageResource(R.drawable.ic_profile)
                }
            }
            .addOnFailureListener {
                binding.userName.text = currentUser.displayName
                    ?: currentUser.email?.substringBefore("@")
                    ?: "Guest User"
                binding.profileImage.setImageResource(R.drawable.ic_profile)
            }
    }

    private fun setupClickListeners() {
        binding.cardPrivacyPolicy.setOnClickListener {
            openCustomTab("https://potssellastic.in/privacy-policy/")
        }

        binding.cardVisitWebsite.setOnClickListener {
            openCustomTab("https://potssellastic.in/")
        }

        binding.cardContactUs.setOnClickListener {
            startActivity(Intent(requireContext(), ContactUsActivity::class.java))
        }

        binding.cardDeleteAccount.setOnClickListener {
            showDeleteAccountDialog()
        }

        binding.btnLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun openCustomTab(url: String) {
        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .setUrlBarHidingEnabled(false)
            .build()
        customTabsIntent.launchUrl(requireContext(), Uri.parse(url))
    }

    private fun showDeleteAccountDialog() {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This action cannot be undone. All your data including orders, reviews, and wishlists will be permanently deleted.")
            .setPositiveButton("Delete") { _, _ ->
                deleteAccount()
            }
            .setNegativeButton("Cancel", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .create()

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(
            resources.getColor(R.color.secondary, null)
        )
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(
            resources.getColor(R.color.secondary, null)
        )
    }

    private fun deleteAccount() {
        val currentUser = auth.currentUser ?: return
        val userId = currentUser.uid

        // Show progress
        val progressDialog = AlertDialog.Builder(requireContext())
            .setTitle("Deleting Account")
            .setMessage("Please wait...")
            .setCancelable(false)
            .create()
        progressDialog.show()

        // Delete user data from database
        val databaseRef = database.reference

        // Delete all user-related data
        val updates = hashMapOf<String, Any?>(
            "users/$userId" to null,
            "carts/$userId" to null,
            "wishlist/$userId" to null
        )

        databaseRef.updateChildren(updates)
            .addOnSuccessListener {
                // Delete authentication account
                currentUser.delete()
                    .addOnSuccessListener {
                        progressDialog.dismiss()
                        Toast.makeText(
                            requireContext(),
                            "Account deleted successfully",
                            Toast.LENGTH_LONG
                        ).show()

                        // Navigate to login
                        val intent = Intent(requireContext(), LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        requireActivity().finish()
                    }
                    .addOnFailureListener { e ->
                        progressDialog.dismiss()

                        if (e is FirebaseAuthRecentLoginRequiredException) {
                            showSessionExpiredDialog()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Failed to delete account: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    requireContext(),
                    "Failed to delete data: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun showLogoutDialog() {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                logout()
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(
            resources.getColor(R.color.secondary, null)
        )
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(
            resources.getColor(R.color.secondary, null)
        )
    }

    private fun showSessionExpiredDialog() {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Session Expired")
            .setMessage("Your current session has expired. Please re-login and try again.")
            .setPositiveButton("Re-login") { _, _ ->
                logout()
            }
            .setCancelable(false)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .create()

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(
            resources.getColor(R.color.secondary, null)
        )
    }

    private fun logout() {
        auth.signOut()

        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
