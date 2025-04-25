package com.example.ecoswap.Activities.Fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ecoswap.Adapters.ImageAdapter
import com.example.ecoswap.DataClass.ProductData
import com.example.ecoswap.databinding.FragmentAddProductBinding
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class AddProductFragment : Fragment() {
    private val LOCATION_PERMISSION = 1001
    private val CAMERA_PERMISSION   = 1002

    private lateinit var binding: FragmentAddProductBinding
    private lateinit var fusedClient: FusedLocationProviderClient
    private lateinit var imageAdapter: ImageAdapter
    private val images = mutableListOf<Uri>()
    private var locMap: Map<String, Double>? = null

    private val pickImages =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            if (uris.size > 4) {
                Toast.makeText(requireContext(), "Max 4 images", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }
            images.clear(); images.addAll(uris)
            imageAdapter.notifyDataSetChanged()
        }

    override fun onCreateView(
        i: LayoutInflater, c: ViewGroup?, s: Bundle?
    ) = FragmentAddProductBinding.inflate(i, c, false).also {
        binding = it
        fusedClient = LocationServices.getFusedLocationProviderClient(requireContext())
        setupCategories()
        setupRecyclerView()
        setupClicks()
    }.root

    private fun setupCategories() {
        val cats = listOf("Books","Shoes","Shirt","T-Shirt","Toy")
        binding.productType.apply {
            setAdapter(ArrayAdapter(requireContext(),
                android.R.layout.simple_list_item_1, cats))
            threshold = 1
            setOnFocusChangeListener { _, f -> if (f) showDropDown() }
        }
    }

    private fun setupRecyclerView() {
        imageAdapter = ImageAdapter(images)
        binding.productRecyclerview.apply {
            layoutManager = LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = imageAdapter
        }
    }

    private fun setupClicks() {
        binding.currentLocations.setOnClickListener { checkLocation() }
        binding.selectFileBtn.setOnClickListener   { checkCamera() }
        binding.uploadBtn.setOnClickListener       { uploadProduct() }
    }

    private fun checkLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION
            )
        } else fetchLocation()
    }

    @SuppressLint("MissingPermission")
    private fun fetchLocation() {
        fusedClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).addOnSuccessListener { loc ->
            if (loc != null) {
                locMap = mapOf(
                    "latitude"  to loc.latitude,
                    "longitude" to loc.longitude
                )
                binding.currentLocations.text =
                    "Location: ${loc.latitude}, ${loc.longitude}"
            } else {
                Toast.makeText(requireContext(),
                    "Cannot fetch location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION
            )
        } else pickImages.launch("image/*")
    }

    override fun onRequestPermissionsResult(
        req: Int, per: Array<out String>, res: IntArray
    ) {
        super.onRequestPermissionsResult(req, per, res)
        when (req) {
            LOCATION_PERMISSION -> if (res.getOrNull(0)==PackageManager.PERMISSION_GRANTED)
                fetchLocation()
            CAMERA_PERMISSION   -> if (res.getOrNull(0)==PackageManager.PERMISSION_GRANTED)
                pickImages.launch("image/*")
        }
    }

    private fun uploadProduct() {
        val title = binding.productTitle.text.toString().trim()
        val desc  = binding.productDiscriptions.text.toString().trim()
        val cat   = binding.productType.text.toString().trim()
        val uid   = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db    = FirebaseFirestore.getInstance()

        if (title.isEmpty()||desc.isEmpty()||cat.isEmpty()
            || images.isEmpty()||locMap==null
        ) {
            Toast.makeText(requireContext(),
                "Fill all & get location", Toast.LENGTH_SHORT).show()
            return
        }

        binding.uploadBtn.isEnabled=false
        binding.uploadBtn.text="Uploading..."

        val pid = db.collection("products").document().id
        // upload images
        val store = FirebaseStorage.getInstance()
        val urls = mutableListOf<String>()
        var done=0
        images.forEachIndexed { idx, uri ->
            store.reference.child("product_images/$pid/img_$idx.jpg")
                .putFile(uri)
                .continueWithTask { it.result!!.storage.downloadUrl }
                .addOnSuccessListener {
                    urls.add(it.toString())
                    if (++done==images.size) {
                        saveProduct(pid, title, desc, cat, uid, urls)
                    }
                }
        }
    }

    private fun saveProduct(
        pid:String, title:String, desc:String,
        cat:String, uid:String, urls:List<String>
    ){
        val db = FirebaseFirestore.getInstance()
        db.collection("products").document(pid)
            .set(ProductData(
                productId=pid,
                title=title,
                description=desc,
                category=cat,
                imageUrls=urls,
                location=locMap!!,
                postedBy=uid,
                userName="",
                timestamp=System.currentTimeMillis(),
                availability="Available",
                swapType="Free"
            ))
            .addOnSuccessListener {
                Toast.makeText(requireContext(),
                    "Uploaded!", Toast.LENGTH_SHORT).show()
                binding.uploadBtn.text="Upload Product"
                binding.uploadBtn.isEnabled=true
                reset()
            }
    }

    private fun reset(){
        binding.productTitle.text.clear()
        binding.productDiscriptions.text.clear()
        binding.productType.text!!.clear()
        images.clear(); imageAdapter.notifyDataSetChanged()
        locMap=null; binding.currentLocations.text="Current Location"
    }
}
