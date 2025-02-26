package com.example.activityshare.modules.profile

import android.app.Activity
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class editProfileViewModel : ViewModel() {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

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

    fun updateUsername(newUsername: String, activity: Activity, callback: (Boolean) -> Unit) {
        val userId = firebaseAuth.currentUser?.uid

        if (userId != null && newUsername.isNotEmpty()) {
            db.collection("users").document(userId).update("username", newUsername)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        updateUsernameInPosts(newUsername,callback)
                    } else {
                        Log.e("EditProfileViewModel", "Error updating username: ${task.exception?.message}")
                        callback(false)
                    }
                }
        } else {
            Log.e("EditProfileViewModel", "User is null or username is empty.")
            callback(false)
        }
    }

    fun uploadProfileImage(imageUri: Uri, activity: Activity, callback: (Boolean) -> Unit) {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            Log.e("EditProfileViewModel", "User is not authenticated")
            callback(false)
            return
        }

        db.collection("users").document(userId)
            .update("avatar", imageUri)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("EditProfileViewModel", "Profile image URL updated successfully: $imageUri")
                    updateAvatarInPosts(imageUri,callback)
                } else {
                    Log.e("EditProfileViewModel", "Error updating profile image URL: ${task.exception?.message}")
                    callback(false)
                }
            }
    }

    fun updateUsernameInPosts(newUsername: String, callback: (Boolean) -> Unit) {
        val userId = firebaseAuth.currentUser?.uid

        if (userId != null) {
            db.collection("posts")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        document.reference.update("username", newUsername)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d("EditProfileViewModel", "Username updated successfully in posts")
                                } else {
                                    Log.e("EditProfileViewModel", "Error updating username in posts: ${task.exception?.message}")
                                }
                            }
                    }
                    callback(true)
                }
                .addOnFailureListener { e ->
                    Log.e("EditProfileViewModel", "Error retrieving posts: ${e.message}")
                    callback(false)
                }
        } else {
            Log.e("EditProfileViewModel", "User is not authenticated")
            callback(false)
        }
    }

    fun updateAvatarInPosts(newAvatarUrl: Uri, callback: (Boolean) -> Unit) {
        val userId = firebaseAuth.currentUser?.uid

        if (userId != null) {
            db.collection("posts")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        document.reference.update("avatar", newAvatarUrl)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d("EditProfileViewModel", "Avatar updated successfully in posts")
                                } else {
                                    Log.e("EditProfileViewModel", "Error updating avatar in posts: ${task.exception?.message}")
                                }
                            }
                    }
                    callback(true)
                }
                .addOnFailureListener { e ->
                    Log.e("EditProfileViewModel", "Error retrieving posts: ${e.message}")
                    callback(false)
                }
        } else {
            Log.e("EditProfileViewModel", "User is not authenticated")
            callback(false)
        }
    }




}
