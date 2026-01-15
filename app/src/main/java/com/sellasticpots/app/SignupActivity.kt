package com.sellasticpots.app

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.sellasticpots.app.databinding.ActivitySignupBinding
import com.sellasticpots.app.models.User

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Force light mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Enable edge-to-edge
        enableEdgeToEdge()

        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        binding.btnSignInTab.setOnClickListener {
            finish()
        }

        binding.btnSignUp.setOnClickListener {
            signUpUser()
        }
    }

    private fun signUpUser() {
        val fullName = binding.etFullName.text.toString().trim()
        val phoneNo = binding.etPhoneNumber.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        // Validation
        if (fullName.isEmpty()) {
            binding.etFullName.error = "Full name is required"
            binding.etFullName.requestFocus()
            return
        }

        if (fullName.length < 3) {
            binding.etFullName.error = "Name must be at least 3 characters"
            binding.etFullName.requestFocus()
            return
        }

        if (phoneNo.isEmpty()) {
            binding.etPhoneNumber.error = "Phone number is required"
            binding.etPhoneNumber.requestFocus()
            return
        }

        if (phoneNo.length < 10) {
            binding.etPhoneNumber.error = "Please enter a valid phone number"
            binding.etPhoneNumber.requestFocus()
            return
        }

        if (email.isEmpty()) {
            binding.etEmail.error = "Email is required"
            binding.etEmail.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Please enter a valid email"
            binding.etEmail.requestFocus()
            return
        }

        if (password.isEmpty()) {
            binding.etPassword.error = "Password is required"
            binding.etPassword.requestFocus()
            return
        }

        if (password.length < 6) {
            binding.etPassword.error = "Password must be at least 6 characters"
            binding.etPassword.requestFocus()
            return
        }

        if (confirmPassword.isEmpty()) {
            binding.etConfirmPassword.error = "Please confirm your password"
            binding.etConfirmPassword.requestFocus()
            return
        }

        if (password != confirmPassword) {
            binding.etConfirmPassword.error = "Passwords do not match"
            binding.etConfirmPassword.requestFocus()
            return
        }

        // Show progress
        showLoading(true)

        // Create user with Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign up success, save user data to Realtime Database
                    val firebaseUser = auth.currentUser
                    firebaseUser?.let {
                        val username = email.substringBefore("@")
                        val user = User(
                            uid = it.uid,
                            email = email,
                            username = username,
                            fullName = fullName,
                            phoneNo = phoneNo,
                            createdAt = System.currentTimeMillis()
                        )

                        // Store user data in Realtime Database
                        database.reference.child("users").child(it.uid)
                            .setValue(user)
                            .addOnSuccessListener {
                                showLoading(false)
                                Toast.makeText(
                                    this,
                                    "Sign up successful!",
                                    Toast.LENGTH_SHORT
                                ).show()

                                // Navigate to MainActivity
                                startActivity(Intent(this, MainActivity::class.java))
                                finishAffinity() // Clear back stack
                            }
                            .addOnFailureListener { e ->
                                showLoading(false)
                                Toast.makeText(
                                    this,
                                    "Failed to save user data: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                } else {
                    // Sign up failed
                    showLoading(false)
                    Toast.makeText(
                        this,
                        "Sign up failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.btnSignUp.isEnabled = !isLoading
        binding.btnSignUp.text = if (isLoading) "Signing up..." else getString(R.string.sign_up)
    }
}

