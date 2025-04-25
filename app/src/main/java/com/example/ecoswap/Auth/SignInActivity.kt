package com.example.ecoswap.Auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.withStyledAttributes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ecoswap.MainActivity
import com.example.ecoswap.R
import com.example.ecoswap.Utils
import com.example.ecoswap.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivitySignInBinding.inflate(layoutInflater)
    }

    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = FirebaseAuth.getInstance()

        ////sign up sereen

        binding.signIntxt.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }

////sign in with email and password
        binding.continuee.setOnClickListener {
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                signInWithEmailPassword(email, password)
                Utils.showLoadingDialog(this, "Signing in...")
            } else {
                Utils.hideLoadingDialog()
                Toast.makeText(this, "error...", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun signInWithEmailPassword(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
            Utils.hideLoadingDialog()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
            .addOnFailureListener {
                Utils.hideLoadingDialog()
                Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
            }

    }

}



