package com.example.ecoswap.Activities.Fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.ecoswap.Activities.SwapRequestedActivity
import com.example.ecoswap.DataClass.UserData
import com.example.ecoswap.R
import com.example.ecoswap.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class ProfileFragment : Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        db= FirebaseFirestore.getInstance()
        auth= FirebaseAuth.getInstance()

        binding.allRequestProduct.setOnClickListener {
            startActivity(Intent(requireContext(), SwapRequestedActivity::class.java))
        }

        fetchUserProfileData()
        return binding.root
    }

    private fun fetchUserProfileData() {

        val userId=auth.currentUser!!.uid

        if (userId!=null){
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val name = document.getString("name")
                        val email = document.getString("email")
//                        val ecoPoints=document.getString("ecoPoints")!!.toInt()
                        val profileImage = document.getString("profileImageUrl")

                        // Now you can use the data, for example:
                        binding.username.text = name.toString()
                        binding.email.text = email.toString()
//                        binding.ecoPointNumber.text=ecoPoints.toString()

                        // Load profile image using Glide
                        Glide.with(this)
                            .load(profileImage)
                            .placeholder(R.drawable.profile)
                            .into(binding.profileImage)

                    } else {
                        Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error fetching user: ${e.message}", Toast.LENGTH_SHORT).show()
                }

        }


    }


}