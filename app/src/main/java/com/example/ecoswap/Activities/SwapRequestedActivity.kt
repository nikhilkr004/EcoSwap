package com.example.ecoswap.Activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ecoswap.Adapters.SwapProductAdapter
import com.example.ecoswap.DataClass.SwapRequest
import com.example.ecoswap.R
import com.example.ecoswap.Utils
import com.example.ecoswap.databinding.ActivitySwapRequestedBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SwapRequestedActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivitySwapRequestedBinding.inflate(layoutInflater)
    }

    private val swapProductList = mutableListOf<SwapRequest>()
    private lateinit var swapProductAdapter: SwapProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        binding.backBtn.setOnClickListener {
            finish()
        }

        // Initialize adapter and assign it to RecyclerView
        swapProductAdapter = SwapProductAdapter(swapProductList)

        binding.recyclerview.layoutManager = LinearLayoutManager(this)
        binding.recyclerview.adapter = swapProductAdapter

        fetchMySwapRequests()
    }

    private fun fetchMySwapRequests() {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("swapRequests")
            .whereEqualTo("receiverId", currentUser.uid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val swapRequests = querySnapshot.toObjects(SwapRequest::class.java)
                swapProductList.clear()
                swapProductList.addAll(swapRequests)
                swapProductAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                Log.e("FetchSwapRequests", "Error fetching requests", it)
            }
    }

    fun updateSwapStatusInFirestore(request: SwapRequest, status: String) {
        Utils.showLoadingDialog(this,"wait")
        val db = FirebaseFirestore.getInstance()
        val requestDocId = request.requestId

        if (requestDocId == null) {
            Toast.makeText(this, "Invalid request ID", Toast.LENGTH_SHORT).show()
            Utils.hideLoadingDialog()
            return
        }

        db.collection("swapRequests")
            .document(requestDocId)
            .update("status", status)
            .addOnSuccessListener {
                Utils.hideLoadingDialog()
                Toast.makeText(this, "Request $status", Toast.LENGTH_SHORT).show()
                fetchMySwapRequests() // Refresh list to reflect changes
            }
            .addOnFailureListener {
                Utils.hideLoadingDialog()
                Toast.makeText(this, "Failed to update status", Toast.LENGTH_SHORT).show()
                Log.e("SwapStatus", "Update failed", it)
            }
    }
}
