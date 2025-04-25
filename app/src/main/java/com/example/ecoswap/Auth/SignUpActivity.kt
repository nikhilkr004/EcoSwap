package com.example.ecoswap.Auth

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.ecoswap.DataClass.UserData
import com.example.ecoswap.MainActivity
import com.example.ecoswap.Utils
import com.example.ecoswap.databinding.ActivitySignUpBinding
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private val binding by lazy { ActivitySignUpBinding.inflate(layoutInflater) }
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var userLatitude: Double? = null
    private var userLongitude: Double? = null

    private val LOCATION_PERMISSION_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.getLocationPermission.setOnClickListener {
            checkLocationPermissionAndFetch()
        }

        binding.signIntxt.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }

        binding.continuee.setOnClickListener {
            val name     = binding.name.text.toString().trim()
            val email    = binding.email.text.toString().trim()
            val phone    = binding.phone.text.toString().trim()
            val password = binding.password.text.toString().trim()

            when {
                name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() ->
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()

                userLatitude == null || userLongitude == null ->
                    Toast.makeText(this,
                        "Please click 'Get Location Permission' first",
                        Toast.LENGTH_SHORT).show()

                else -> signUpUser(name, email, phone, password)
            }
        }
    }

    private fun checkLocationPermissionAndFetch() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            fetchCurrentLocation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_CODE &&
            grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED
        ) {
            fetchCurrentLocation()
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchCurrentLocation() {
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).addOnSuccessListener { loc: Location? ->
            if (loc != null) {
                userLatitude  = loc.latitude
                userLongitude = loc.longitude
                Toast.makeText(this,
                    "Location saved: $userLatitude, $userLongitude",
                    Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this,
                    "Unable to fetch location", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this,
                "Location error: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signUpUser(
        name: String,
        email: String,
        phone: String,
        password: String
    ) {
        Utils.showLoadingDialog(this, "Signing up...")

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val locMap = mapOf(
                    "latitude"  to userLatitude!!,
                    "longitude" to userLongitude!!
                )

                val user = UserData(
                    uid             = auth.currentUser!!.uid,
                    name            = name,
                    email           = email,
                    phone           = phone,
                    password        = password,
                    profileImageUrl = "",
                    location        = locMap,
                    joinedAt        = Timestamp.now()
                )

                db.collection("users")
                    .document(user.uid!!)
                    .set(user)
                    .addOnSuccessListener {
                        Utils.hideLoadingDialog()
                        Toast.makeText(this,
                            "Sign Up Successful", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Utils.hideLoadingDialog()
                        Toast.makeText(this,
                            "Save failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Utils.hideLoadingDialog()
                Toast.makeText(this,
                    "Signup Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
