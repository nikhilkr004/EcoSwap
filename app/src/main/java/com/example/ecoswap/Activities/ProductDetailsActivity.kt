package com.example.ecoswap.Activities

import ImagesPagerAdapter
import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.ecoswap.DataClass.UserData
import com.example.ecoswap.R
import com.example.ecoswap.databinding.ActivityProductDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.ArrayList

class ProductDetailsActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityProductDetailsBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val productId   = intent.getStringExtra("productId")
        val title       = intent.getStringExtra("title")
        val description = intent.getStringExtra("description")
        val category    = intent.getStringExtra("category")
        val avaliblity  = intent.getStringExtra("avaliblity")
        val userId      = intent.getStringExtra("userid")
        val imageUrls   = intent.getStringArrayListExtra("imageUrls") ?: arrayListOf()
        val lat         = intent.getDoubleExtra("latitude",  0.0)
        val lng         = intent.getDoubleExtra("longitude", 0.0)


        binding.avaliblity.text=avaliblity.toString()
        binding.productTitle.text=title.toString()
        binding.disc.text=description.toString()

        ///fetch user data like user image , name ,address more

        fetchUserdata(userId.toString())
        ////set image
        setProductImage(imageUrls)

    }

    private fun fetchUserdata(userId: String) {


        val db = FirebaseFirestore.getInstance()


        if (userId.isNotEmpty()) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val user = document.toObject(UserData::class.java)

                        if (user!=null){
                            binding.byUser.text="By:${user.name.toString()}"
                        }
                    } else {
                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to fetch data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setProductImage(imageUrls: java.util.ArrayList<kotlin.String>) {
        val pager=binding.imagesPager
        pager.adapter=ImagesPagerAdapter(imageUrls)

        ///image indicator or image count
        ///initially image count
        binding.pageIndicator.text="$1/${imageUrls.size}"

        /// after change a images
        pager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // position zero-based है, इसलिए +1 करें
                binding.pageIndicator.text = "${position + 1}/${imageUrls.size}"
            }
        })
    }
}