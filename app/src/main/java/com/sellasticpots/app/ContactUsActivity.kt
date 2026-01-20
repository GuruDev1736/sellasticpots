package com.sellasticpots.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.sellasticpots.app.databinding.ActivityContactUsBinding
import com.sellasticpots.app.models.User

class ContactUsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactUsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding = ActivityContactUsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        setupToolbar()
        loadUserData()
        setupSubmitButton()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser ?: return

        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.etEmail.setText(currentUser.email ?: "")

        database.reference.child("users").child(currentUser.uid).get()
            .addOnSuccessListener { snapshot ->
                binding.progressBar.visibility = android.view.View.GONE
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    binding.etFullName.setText(user.fullName)
                    binding.etContactNo.setText(user.phoneNo)
                    if (binding.etEmail.text.toString().isEmpty()) {
                        binding.etEmail.setText(user.email)
                    }
                }
            }
            .addOnFailureListener {
                binding.progressBar.visibility = android.view.View.GONE
            }
    }

    private fun setupSubmitButton() {
        binding.btnSubmit.setOnClickListener {
            submitContactForm()
        }
    }

    private fun submitContactForm() {
        val fullName = binding.etFullName.text.toString().trim()
        val contactNo = binding.etContactNo.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val subject = binding.etSubject.text.toString().trim()
        val question = binding.etQuestion.text.toString().trim()

        if (fullName.isEmpty()) {
            binding.etFullName.error = "Full name is required"
            binding.etFullName.requestFocus()
            return
        }

        if (contactNo.isEmpty()) {
            binding.etContactNo.error = "Contact number is required"
            binding.etContactNo.requestFocus()
            return
        }

        if (email.isEmpty()) {
            binding.etEmail.error = "Email is required"
            binding.etEmail.requestFocus()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Invalid email address"
            binding.etEmail.requestFocus()
            return
        }

        if (subject.isEmpty()) {
            binding.etSubject.error = "Subject is required"
            binding.etSubject.requestFocus()
            return
        }

        if (question.isEmpty()) {
            binding.etQuestion.error = "Question is required"
            binding.etQuestion.requestFocus()
            return
        }

        binding.btnSubmit.isEnabled = false
        binding.btnSubmit.text = "Submitting..."

        val contactData = hashMapOf(
            "fullName" to fullName,
            "contactNo" to contactNo,
            "email" to email,
            "subject" to subject,
            "question" to question,
            "timestamp" to System.currentTimeMillis(),
            "userId" to (auth.currentUser?.uid ?: "anonymous"),
            "status" to "pending"
        )

        val contactId = database.reference.child("contact_requests").push().key ?: return

        database.reference.child("contact_requests").child(contactId)
            .setValue(contactData)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Your message has been sent successfully! We'll get back to you soon.",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Failed to send message: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                binding.btnSubmit.isEnabled = true
                binding.btnSubmit.text = "Submit"
            }
    }
}
