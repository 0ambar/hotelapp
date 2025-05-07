package com.example.hotelapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hotelapp.databinding.ActivityEnterPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class EnterPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEnterPasswordBinding
    private lateinit var mAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEnterPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val email = intent.getStringExtra("email")

        // Concatenate the email with the existing text
        val informationText = "Ingresa la contraseña para $email"
        binding.informationEmailText.text = informationText

        mAuth = FirebaseAuth.getInstance()

        binding.loginButton.setOnClickListener {
            val password = binding.passwordEditText.text.toString()

            // Call the login method
            if (email != null) {
                login(email, password)
            }
        }

    }
    private fun login(email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Login success, user is authenticated
                    val user = mAuth.currentUser
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Login failed
                    Toast.makeText(
                        this,
                        "Inicio de sesión fallido: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}