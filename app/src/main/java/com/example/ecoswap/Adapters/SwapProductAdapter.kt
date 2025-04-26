package com.example.ecoswap.Adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ecoswap.DataClass.ProductData
import com.example.ecoswap.DataClass.SwapRequest
import com.example.ecoswap.DataClass.UserData
import com.example.ecoswap.Utils
import com.example.ecoswap.databinding.SwapProductItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class SwapProductAdapter(
    private val data: List<SwapRequest>
) : RecyclerView.Adapter<SwapProductAdapter.Viewholder>() {

    inner class Viewholder(private val binding: SwapProductItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(request: SwapRequest) {
            val context = binding.root.context
            val db = FirebaseFirestore.getInstance()

            // Load product info
            db.collection("products").document(request.productId).get()
                .addOnSuccessListener { doc ->
                    val product = doc.toObject(ProductData::class.java)
                    product?.let {
                        binding.productTitle.text = it.title
                        Glide.with(context).load(it.imageUrls.firstOrNull()).into(binding.productImage)
                    }
                }

            // Load user info
            db.collection("users").document(request.senderId).get()
                .addOnSuccessListener { doc ->
                    val userData = doc.toObject(UserData::class.java)
                    userData?.let {
                        binding.userName.text = it.name

                        val locationMap = doc.get("location") as? Map<*, *>
                        val lat = locationMap?.get("latitude") as? Double
                        val lng = locationMap?.get("longitude") as? Double

                        if (lat != null && lng != null) {
                            getAddressFromCoordinates(lat, lng) { address ->
                                (context as? Activity)?.runOnUiThread {
                                    binding.userLocations.text = address
                                }
                            }
                        }
                    }
                }


            // Show/hide buttons and update status text
            when (request.status) {
                "accepted" -> {
                    binding.accept.visibility = ViewGroup.GONE
                    binding.reject.visibility = ViewGroup.GONE
                    binding.productStatus.text = "Accepted"
                    binding.productStatus.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark))
                    binding.productStatus.visibility = ViewGroup.VISIBLE
                }
                "rejected" -> {
                    binding.accept.visibility = ViewGroup.GONE
                    binding.reject.visibility = ViewGroup.GONE
                    binding.productStatus.text = "Rejected"
                    binding.productStatus.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))
                    binding.productStatus.visibility = ViewGroup.VISIBLE
                }
                else -> {
                    binding.accept.visibility = ViewGroup.VISIBLE
                    binding.reject.visibility = ViewGroup.VISIBLE
                    binding.productStatus.visibility = ViewGroup.GONE
                }
            }



            ////if request is accepted then show other user details

            if (request.status == "accepted") {
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                val otherUserId = if (currentUserId == request.senderId) request.receiverId else request.senderId

                db.collection("users").document(otherUserId).get()
                    .addOnSuccessListener { otherUserDoc ->
                        val otherUser = otherUserDoc.toObject(UserData::class.java)
                        val phoneNumber = otherUser?.phone ?: ""

                        binding.callUser.visibility = ViewGroup.VISIBLE

                        binding.callUser.setOnClickListener {
                            val dialIntent = Intent(Intent.ACTION_DIAL)
                            dialIntent.data = Uri.parse("tel:$phoneNumber")
                            binding.root.context.startActivity(dialIntent)
                        }
                    }
            }


            // Handle button clicks
            binding.accept.setOnClickListener {
            updateSwapStatusInFirestore(request,"accepted",context)
            }

            binding.reject.setOnClickListener {
                updateSwapStatusInFirestore(request, "rejected",context)
            }
        }


        fun updateSwapStatusInFirestore(request: SwapRequest, status: String, context: Context) {
            Utils.showLoadingDialog(context,"wait")
            val db = FirebaseFirestore.getInstance()
            val requestDocId = request.requestId

            if (requestDocId == null) {
                Toast.makeText(context, "Invalid request ID", Toast.LENGTH_SHORT).show()
                Utils.hideLoadingDialog()
                return
            }

            db.collection("swapRequests")
                .document(requestDocId)
                .update("status", status)
                .addOnSuccessListener {
                    Utils.hideLoadingDialog()
                    Toast.makeText(context, "Request $status", Toast.LENGTH_SHORT).show()
                    // Update local object and refresh UI
                    request.status = status
                    notifyItemChanged(adapterPosition)
                }
                .addOnFailureListener {
                    Utils.hideLoadingDialog()
                    Toast.makeText(context, "Failed to update status", Toast.LENGTH_SHORT).show()
                    Log.e("SwapStatus", "Update failed", it)
                }
        }


        private fun getAddressFromCoordinates(lat: Double, lon: Double, callback: (String) -> Unit) {
            val url = "https://nominatim.openstreetmap.org/reverse?lat=$lat&lon=$lon&format=json"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "EcoSwapApp")
                .build()

            OkHttpClient().newCall(request).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    val json = JSONObject(response.body?.string() ?: "")
                    val address = json.optString("display_name", "Unknown address")
                    callback(address)
                }

                override fun onFailure(call: Call, e: IOException) {
                    callback("Error: ${e.localizedMessage}")
                }
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = SwapProductItemBinding.inflate(inflater, parent, false)
        return Viewholder(binding)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size
}
