package com.sellasticpots.app

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.sellasticpots.app.databinding.ActivityMainBinding
import com.sellasticpots.app.fragments.ProductsFragment

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Force light mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Enable edge-to-edge
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle window insets for the toolbar and main content
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Only apply top padding to toolbar, bottom to fragment container
            binding.toolbar.setPadding(0, systemBars.top, 0, 0)
            binding.fragmentContainer.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        // Handle insets for navigation drawer
        ViewCompat.setOnApplyWindowInsetsListener(binding.navView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, systemBars.bottom)
            insets
        }

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Check if user is logged in
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // User not logged in, redirect to login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Setup navigation drawer
        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            0,
            0
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)

        // Update navigation header with user info
        val headerView = binding.navView.getHeaderView(0)
        val navHeaderEmail = headerView.findViewById<TextView>(R.id.nav_header_email)
        navHeaderEmail.text = currentUser.email ?: "No email"

        // Load default fragment (Products)
        if (savedInstanceState == null) {
            loadFragment(ProductsFragment())
            binding.navView.setCheckedItem(R.id.nav_products)
            binding.toolbarTitle.text = "Products"
        }

        // Cart icon click
        binding.cartIcon.setOnClickListener {
            Toast.makeText(this, "Cart clicked", Toast.LENGTH_SHORT).show()
        }

        // Handle back button
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    finish()
                }
            }
        })
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_products -> {
                loadFragment(ProductsFragment())
                binding.toolbarTitle.text = "Pottery Collection"
            }
            R.id.nav_cart -> {
                binding.toolbarTitle.text = "My Cart"
                Toast.makeText(this, "Cart feature coming soon", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_orders -> {
                binding.toolbarTitle.text = "My Orders"
                Toast.makeText(this, "Orders feature coming soon", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_wishlist -> {
                binding.toolbarTitle.text = "Wishlist"
                Toast.makeText(this, "Wishlist feature coming soon", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_profile -> {
                binding.toolbarTitle.text = "Profile"
                Toast.makeText(this, "Profile feature coming soon", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_logout -> {
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finishAffinity() // Clear back stack
                return true
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}