package com.example.activityshare.modules.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.activityshare.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class profile : Fragment() {

    private lateinit var profileImageView: ImageView
    private lateinit var usernameTextView: TextView
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Logout button
        val binding = inflater.inflate(R.layout.fragment_profile, container, false)
        val logoutButton = binding.findViewById<View>(R.id.fragment_profile_logout_button)
        val editProfile = binding.findViewById<View>(R.id.fragment_profile_edit_profile_button)
        val myActivitys = binding.findViewById<View>(R.id.fragment_profile_my_activities_button)
        profileImageView = binding.findViewById(R.id.fragment_profile_image_view)
        usernameTextView = binding.findViewById(R.id.fragment_profile_username)

        loadUserProfile()

        logoutButton.setOnClickListener {
            findNavController().navigate(R.id.login_Fragment)
        }
        editProfile.setOnClickListener{
            findNavController().navigate(R.id.editProfile)
        }
        myActivitys.setOnClickListener {
            findNavController().navigate(R.id.viewMyPosts)
        }
        return binding
    }

    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val username = document.getString("username") ?: "User"
                        usernameTextView.text = username
                    } else {
                        Log.d("Profile", "Document does not exist")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Profile", "Error loading user profile", e)
                }
        } else {
            Log.d("Profile", "User ID is null")
        }
    }


}
