package com.example.ecoswap.Activities

import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.UserHandle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ecoswap.DataClass.ProductData
import com.example.ecoswap.R
import com.example.ecoswap.databinding.ActivityCardItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class CardItemActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityCardItemBinding.inflate(layoutInflater)
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth= FirebaseAuth.getInstance()
        db= FirebaseFirestore.getInstance()


    }


    private fun fetchProducts(category: String? = null) {

    }
}