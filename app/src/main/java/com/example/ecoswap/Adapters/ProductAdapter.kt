import android.content.Context
import android.location.Location
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ecoswap.DataClass.ProductData
import com.example.ecoswap.databinding.HomeProductItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProductAdapter(
    private val data: List<ProductData>,
    private val onItemClick: (ProductData) -> Unit
) : RecyclerView.Adapter<ProductAdapter.Viewholder>() {

    inner class Viewholder(val binding: HomeProductItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ProductData) {
            val context = binding.root.context

            binding.productTitle.text = item.title
            binding.status.text = item.availability

            if (item.imageUrls.isNotEmpty()) {
                Glide.with(context)
                    .load(item.imageUrls[0])
                    .into(binding.productImage)
            }

            // Invoke callback on click
            binding.root.setOnClickListener {
                onItemClick(item)
            }






            // Distance calculation...
            fetchUserLocationAndCalculateDistance(context, item.location)
        }
        private fun fetchUserLocationAndCalculateDistance(context: Context, productLocation: Map<String, Double>?) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val firestore = FirebaseFirestore.getInstance()

            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val userLocMap = document.get("location") as? Map<*, *>
                    val userLat = (userLocMap?.get("latitude") as? Double)
                    val userLng = (userLocMap?.get("longitude") as? Double)

                    if (userLat != null && userLng != null && productLocation != null) {
                        val prodLat = productLocation["latitude"] ?: return@addOnSuccessListener
                        val prodLng = productLocation["longitude"] ?: return@addOnSuccessListener

                        val distance = straightLineDistanceKm(
                            userLat, userLng,
                            prodLat, prodLng
                        )
                        val formattedDistance = String.format("%.2f", distance)

                        binding.distance.text = "$formattedDistance km"
                    } else {
                        Toast.makeText(context, "User or product location not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to fetch user location: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // ensure this signature
        fun straightLineDistanceKm(
            lat1: Double, lon1: Double,
            lat2: Double, lon2: Double
        ): Double {
            val locA = Location("").apply {
                latitude  = lat1
                longitude = lon1
            }
            val locB = Location("").apply {
                latitude  = lat2
                longitude = lon2
            }
            return locA.distanceTo(locB) / 1000.0 // kilometers
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        val binding = HomeProductItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return Viewholder(binding)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size
}
