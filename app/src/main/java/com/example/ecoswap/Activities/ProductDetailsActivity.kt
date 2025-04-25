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
import com.example.ecoswap.DataClass.SwapRequest
import com.example.ecoswap.DataClass.UserData
import com.example.ecoswap.R
import com.example.ecoswap.Utils
import com.example.ecoswap.databinding.ActivityProductDetailsBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProductDetailsActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

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
        auth = FirebaseAuth.getInstance()


///finish the activity
        binding.backBtn.setOnClickListener {
            finish()
        }


        val productId = intent.getStringExtra("productId")
        val title = intent.getStringExtra("title")
        val description = intent.getStringExtra("description")
        val category = intent.getStringExtra("category")
        val avaliblity = intent.getStringExtra("avaliblity")
        val userId = intent.getStringExtra("userid")
        val imageUrls = intent.getStringArrayListExtra("imageUrls") ?: arrayListOf()
        val lat = intent.getDoubleExtra("latitude", 0.0)
        val lng = intent.getDoubleExtra("longitude", 0.0)
        val currentUserId = auth.currentUser!!.uid

        binding.avaliblity.text = avaliblity.toString()
        binding.productTitle.text = title.toString()
        binding.disc.text = description.toString()

        ///fetch user data like user image , name ,address more

        fetchUserdata(userId.toString())
        ////set image
        setProductImage(imageUrls)

        /// add to card
        binding.addToCard.setOnClickListener {
            Utils.showLoadingDialog(this, "wait")
            addToCart()
        }
///check request if already have
        checkIfRequestExists(auth.currentUser!!.uid, userId!!, productId!!)

        /// request for product
        binding.requestUser.setOnClickListener {
            val text = binding.requestUser.text.toString().trim().replace(" ", "")

            if (text.equals("RequestforProduct")) {
                requestForProduct(userId, currentUserId, productId)
                Utils.showLoadingDialog(this,"Wait Sending Request")
            } else {
                Utils.hideLoadingDialog()
                Toast.makeText(this, "already request has been send ", Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun fetchUserdata(userId: String) {


        val db = FirebaseFirestore.getInstance()


        if (userId.isNotEmpty()) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val user = document.toObject(UserData::class.java)

                        if (user != null) {
                            binding.byUser.text = "By:${user.name.toString()}"
                        }
                    } else {
                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to fetch data: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setProductImage(imageUrls: java.util.ArrayList<kotlin.String>) {
        val pager = binding.imagesPager
        pager.adapter = ImagesPagerAdapter(imageUrls)

        ///image indicator or image count
        ///initially image count
        binding.pageIndicator.text = "$1/${imageUrls.size}"

        /// after change a images
        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                binding.pageIndicator.text = "${position + 1}/${imageUrls.size}"
            }
        })
    }


    private fun addToCart() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            Utils.hideLoadingDialog()
            return
        }

        val productId = intent.getStringExtra("productId") // or however you pass it
        if (productId == null) {
            Toast.makeText(this, "Product ID not found", Toast.LENGTH_SHORT).show()
            Utils.hideLoadingDialog()
            return
        }

        val db = FirebaseFirestore.getInstance()

        // Fetch the product details from Firestore
        db.collection("products").document(productId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val productData = document.data

                    // Save it inside user's cart collection
                    db.collection("users")
                        .document(currentUser.uid)
                        .collection("cart")
                        .document(productId)
                        .set(productData!!)
                        .addOnSuccessListener {
                            Utils.hideLoadingDialog()
                            Toast.makeText(this, "Product added to cart", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Utils.hideLoadingDialog()
                            Toast.makeText(this, "Failed to add: ${it.message}", Toast.LENGTH_SHORT)
                                .show()
                        }
                } else {
                    Utils.hideLoadingDialog()
                    Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Utils.hideLoadingDialog()
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun requestForProduct(receiverId: String, senderId: String, productId: String) {
        val db = FirebaseFirestore.getInstance()
        val requestRef = db.collection("swapRequests").document() // Auto-generates ID
        val requestId = requestRef.id

        val swapRequest = SwapRequest(
            requestId = requestId,
            senderId = senderId,
            receiverId = receiverId,
            productId = productId,
            status = "pending",
            timestamp = Timestamp.now()

        )
        requestRef.set(swapRequest)
            .addOnSuccessListener {

                Toast.makeText(this, "Swap request sent!", Toast.LENGTH_SHORT).show()
                checkIfRequestExists(senderId, requestId, productId)
                Utils.hideLoadingDialog()

            }
            .addOnFailureListener {
                Utils.hideLoadingDialog()
                Toast.makeText(this, "Failed to send swap request", Toast.LENGTH_SHORT).show()
            }

    }

    private fun checkIfRequestExists(
        senderId: String,
        receiverId: String,
        receiverProductId: String,
    ) {
        val db = FirebaseFirestore.getInstance()
        db.collection("swapRequests")
            .whereEqualTo("senderId", senderId)
            .whereEqualTo("receiverId", receiverId)
            .whereEqualTo("productId", receiverProductId)
            .whereEqualTo("status", "pending") // optional: check only pending requests
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    binding.requestUser.text = "Requested"
                    binding.requestUser.isEnabled = false
                } else {
                    binding.requestUser.text = "Request for Product"
                    binding.requestUser.isEnabled = true
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to check request", Toast.LENGTH_SHORT).show()
            }
    }

}


