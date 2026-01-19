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
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sellasticpots.app.databinding.ActivityMainBinding
import com.sellasticpots.app.fragments.MyCartFragment
import com.sellasticpots.app.fragments.MyOrdersFragment
import com.sellasticpots.app.fragments.ProductsFragment
import com.sellasticpots.app.fragments.ProfileFragment
import com.sellasticpots.app.fragments.WishlistFragment
import com.sellasticpots.app.models.User
import com.sellasticpots.app.utils.CartManager
import com.sellasticpots.app.utils.OrderManager
import com.sellasticpots.app.utils.WishlistManager

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        enableEdgeToEdge()


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                0,
                systemBars.top,
                0,
                systemBars.bottom
            )
            insets
        }


        ViewCompat.setOnApplyWindowInsetsListener(binding.navView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val currentUser = auth.currentUser
        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        CartManager.initialize()

        WishlistManager.initialize()

        OrderManager.initialize()

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

        loadUserDataFromDatabase(currentUser.uid)

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
                loadFragment(MyOrdersFragment())
                binding.toolbarTitle.text = "My Orders"
            }
            R.id.nav_wishlist -> {
                loadFragment(WishlistFragment())
                binding.toolbarTitle.text = "My Wishlist"
            }
            R.id.nav_profile -> {
                loadFragment(ProfileFragment())
                binding.toolbarTitle.text = "Profile"
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

    private fun loadUserDataFromDatabase(userId: String) {
        val headerView = binding.navView.getHeaderView(0)
        val navHeaderName = headerView.findViewById<TextView>(R.id.nav_header_name)
        val navHeaderMobile = headerView.findViewById<TextView>(R.id.nav_header_mobile)
        val navHeaderEmail = headerView.findViewById<TextView>(R.id.nav_header_email)

        database.reference.child("users").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        navHeaderName.text = user.fullName.ifEmpty {
                            user.username.ifEmpty { "Guest User" }
                        }
                        navHeaderMobile.text = user.phoneNo.ifEmpty { "+1 234 567 8900" }
                        navHeaderEmail.text = user.email.ifEmpty { "user@example.com" }
                    } else {
                        val currentUser = auth.currentUser
                        navHeaderName.text = currentUser?.displayName
                            ?: currentUser?.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
                            ?: "Guest User"
                        navHeaderMobile.text = currentUser?.phoneNumber ?: "+1 234 567 8900"
                        navHeaderEmail.text = currentUser?.email ?: "user@example.com"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    val currentUser = auth.currentUser
                    navHeaderName.text = currentUser?.displayName
                        ?: currentUser?.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
                        ?: "Guest User"
                    navHeaderMobile.text = currentUser?.phoneNumber ?: "+1 234 567 8900"
                    navHeaderEmail.text = currentUser?.email ?: "user@example.com"
                }
            })
    }
}

