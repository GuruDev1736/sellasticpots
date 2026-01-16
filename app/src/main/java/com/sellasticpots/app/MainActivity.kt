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
import com.sellasticpots.app.fragments.MyCartFragment
import com.sellasticpots.app.fragments.ProductsFragment
import com.sellasticpots.app.fragments.WishlistFragment
import com.sellasticpots.app.utils.CartManager
import com.sellasticpots.app.utils.WishlistManager

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.toolbar.setPadding(0, systemBars.top, 0, 0)
            binding.fragmentContainer.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.navView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        CartManager.initialize()

        WishlistManager.initialize()

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

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

        val headerView = binding.navView.getHeaderView(0)
        val navHeaderEmail = headerView.findViewById<TextView>(R.id.nav_header_email)
        navHeaderEmail.text = currentUser.email ?: "No email"

        if (savedInstanceState == null) {
            val openCart = intent.getBooleanExtra("openCart", false)
            if (openCart) {
                loadFragment(MyCartFragment())
                binding.navView.setCheckedItem(R.id.nav_cart)
                binding.toolbarTitle.text = "My Cart"
            } else {
                loadFragment(ProductsFragment())
                binding.navView.setCheckedItem(R.id.nav_products)
                binding.toolbarTitle.text = "Products"
            }
        }

        binding.cartContainer.setOnClickListener {
            loadFragment(MyCartFragment())
            binding.toolbarTitle.text = "My Cart"
            binding.navView.setCheckedItem(R.id.nav_cart)
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
        CartManager.cartCount.observe(this) { count ->
            updateCartBadge(count)
        }

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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        val openCart = intent.getBooleanExtra("openCart", false)
        if (openCart) {
            loadFragment(MyCartFragment())
            binding.navView.setCheckedItem(R.id.nav_cart)
            binding.toolbarTitle.text = "My Cart"
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_products -> {
                loadFragment(ProductsFragment())
                binding.toolbarTitle.text = "Pottery Collection"
            }
            R.id.nav_cart -> {
                loadFragment(MyCartFragment())
                binding.toolbarTitle.text = "My Cart"
            }
            R.id.nav_orders -> {
                binding.toolbarTitle.text = "My Orders"
                Toast.makeText(this, "Orders feature coming soon", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_wishlist -> {
                loadFragment(WishlistFragment())
                binding.toolbarTitle.text = "My Wishlist"
            }
            R.id.nav_profile -> {
                binding.toolbarTitle.text = "Profile"
                Toast.makeText(this, "Profile feature coming soon", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_logout -> {
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finishAffinity()
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

    private fun updateCartBadge(count: Int) {
        if (count > 0) {
            binding.cartBadge.visibility = android.view.View.VISIBLE
            binding.cartBadge.text = count.toString()
        } else {
            binding.cartBadge.visibility = android.view.View.GONE
        }
    }
}

