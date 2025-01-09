package com.example.activityshare

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {

    private lateinit var bottomBar: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomBar = findViewById(R.id.bottom_navigation)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Set up navigation for bottom bar items
        bottomBar.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.item_home -> {
                    // Navigate to home page
                    navController.navigate(R.id.homePage)
                    true
                }
                R.id.item_add -> {
                    navController.navigate(R.id.addPost)
                    true
                }
                R.id.item_profile -> {
                    // Navigate to Profile page
                    navController.navigate(R.id.profile)
                    true
                }
                else -> false
            }
        }

        // Observe NavController to listen for fragment changes
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Hide bottom bar if the current fragment is Login or Register
            if (destination.id == R.id.login_Fragment || destination.id == R.id.register_Fragment || destination.id == R.id.forgotPassword) {
                bottomBar.visibility = View.GONE
            } else {
                bottomBar.visibility = View.VISIBLE
            }
        }

        // Integration with firestore
        val db = Firebase.firestore
        // Create a new user with a first and last name
        val user = hashMapOf(
            "first" to "Ada",
            "last" to "Lovelace",
            "born" to 1815,
        )
        // Add a new document with a generated ID
        db.collection("users")
            .add(user)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }

    }
}
