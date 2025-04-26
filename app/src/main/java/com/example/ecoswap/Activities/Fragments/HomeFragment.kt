package com.example.ecoswap.Activities.Fragments

import ProductAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ecoswap.Activities.ProductDetailsActivity
import com.example.ecoswap.Adapters.CategoriesAdapter
import com.example.ecoswap.DataClass.ProductData
import com.example.ecoswap.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var productAdapter: ProductAdapter
    private val productList = mutableListOf<ProductData>()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // List of categories, including "All"
    private val categories = listOf("All", "Books", "Shoes", "Shirt", "t-Shirt", "Toy")

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentHomeBinding.inflate(inflater, container, false).also {
        binding = it
        setupCategories()
        setupRecyclerView()
        fetchProducts()  // initial load: all products
    }.root

    private fun setupCategories() {
        binding.categoriesRecyclerview.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                RecyclerView.HORIZONTAL,
                false
            )
            adapter = CategoriesAdapter(categories) { selected ->
                // If "All" clicked, pass null; otherwise the chosen category
                fetchProducts(selected.takeIf { it != "All" })
            }
        }
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(productList){
            clickedProduct ->
            val intent= Intent(requireContext(), ProductDetailsActivity::class.java).apply {
                putExtra("productId",    clickedProduct.productId)
                putExtra("title",        clickedProduct.title)
                putExtra("description",  clickedProduct.description)
                putExtra("category",     clickedProduct.category)
                putExtra("avaliblity",     clickedProduct.availability)
                putExtra("userid",     clickedProduct.postedBy)
                putStringArrayListExtra("imageUrls", ArrayList(clickedProduct.imageUrls))
                // for location map:
                putExtra("latitude", clickedProduct.location?.get("latitude"))
                putExtra("longitude", clickedProduct.location?.get("longitude"))
            }
            startActivity(intent)
        }

        binding.productRecyclerview.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = productAdapter
        }
    }


    private fun fetchProducts(category: String? = null) {
        val userId = auth.currentUser?.uid ?: return

        // 1. Fetch user location
        db.collection("users").document(userId).get()
            .addOnSuccessListener { userDoc ->
                val uLocMap = userDoc.get("location") as? Map<*, *> ?: return@addOnSuccessListener
                val uLat = uLocMap["latitude"] as? Double ?: return@addOnSuccessListener
                val uLng = uLocMap["longitude"] as? Double ?: return@addOnSuccessListener

                // 2. Build query by category if any
                var query: Query = db.collection("products")
                if (category != null) {
                    query = query.whereEqualTo("category", category)
                }

                // 3. Execute product query
                query.get()
                    .addOnSuccessListener { snap ->
                        // 4. Map docs to pairs of (ProductData, distance)
                        val listWithDist = snap.documents.mapNotNull { doc ->
                            val prod = doc.toObject(ProductData::class.java) ?: return@mapNotNull null

                            // ❌ Skip if current user owns the product
                            if (prod.postedBy == userId) return@mapNotNull null

                            val pLoc = doc.get("location") as? Map<*, *> ?: return@mapNotNull null
                            val pLat = pLoc["latitude"] as? Double ?: return@mapNotNull null
                            val pLng = pLoc["longitude"] as? Double ?: return@mapNotNull null

                            // Calculate distance
                            val dist = android.location.Location("").apply {
                                latitude = uLat
                                longitude = uLng
                            }.distanceTo(android.location.Location("").apply {
                                latitude = pLat
                                longitude = pLng
                            })

                            prod to dist
                        }

                        // 5. Sort by distance, extract products
                        val sorted = listWithDist.sortedBy { it.second }.map { it.first }

                        // 6. Update adapter
                        productList.clear()
                        productList.addAll(sorted)
                        productAdapter.notifyDataSetChanged()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            requireContext(),
                            "Failed to fetch products: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
    }


}
