package com.example.activityshare.modules.profile

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class EditProfileViewModel : ViewModel() {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    fun updatePassword(newPassword: String, activity: Activity, callback: (Boolean) -> Unit) {
        val user: FirebaseUser? = firebaseAuth.currentUser

        if (user != null && newPassword.isNotEmpty()) {
            user.updatePassword(newPassword).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true)
                } else {
                    Log.e("EditProfileViewModel", "Error updating password: ${task.exception?.message}")
                    callback(false)
                }
            }
        } else {
            Log.e("EditProfileViewModel", "User is null or password is empty.")
            callback(false)
        }
    }
}
