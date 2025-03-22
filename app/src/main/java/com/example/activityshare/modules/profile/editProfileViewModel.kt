package com.example.activityshare.modules.profile

import android.app.Activity
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.activityshare.modules.network.imgur.ImgurClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class editProfileViewModel : ViewModel() {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    fun updatePassword(newPassword: String, activity: Activity, callback: (Boolean) -> Unit) {
        val user: FirebaseUser? = firebaseAuth.currentUser

        if (user != null && newPassword.isNotEmpty()) {
            user.updatePassword(newPassword).addOnCompleteListener { task ->
                callback(task.isSuccessful)
                if (!task.isSuccessful) {
                    Log.e("EditProfileViewModel", "Error updating password: ${task.exception?.message}")
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
                        updateUsernameInPosts(newUsername, callback)
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
        val userId = firebaseAuth.currentUser?.uid ?: return callback(false)
        val imgurService = ImgurClient.create()

        val imageFile = File(getRealPathFromURI(activity, imageUri))
        val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)

        val clientId = "Client-ID 58f3986e4ad864f"

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = imgurService.uploadImage(clientId, body)
                if (response.isSuccessful && response.body()?.success == true) {
                    val imgurLink = response.body()?.data?.link
                    Log.d("ImgurUpload", "Profile image uploaded: $imgurLink")

                    if (imgurLink != null) {
                        db.collection("users").document(userId)
                            .update("avatar", imgurLink)
                            .addOnSuccessListener {
                                Log.d("EditProfileVM", "Avatar URL saved!")

                                updateAvatarInPosts(imgurLink) { updateSuccess ->
                                    callback(updateSuccess)
                                }
                            }
                            .addOnFailureListener {
                                Log.e("EditProfileVM", "Failed saving avatar: ${it.message}")
                                callback(false)
                            }
                    } else {
                        Log.e("ImgurUpload", "Imgur link was null")
                        callback(false)
                    }
                } else {
                    Log.e("ImgurUpload", "Imgur upload failed: ${response.errorBody()?.string()}")
                    callback(false)
                }
            } catch (e: Exception) {
                Log.e("ImgurUpload", "Exception: ${e.message}")
                callback(false)
            }
        }
    }

    fun updateUsernameInPosts(newUsername: String, callback: (Boolean) -> Unit) {
        val userId = firebaseAuth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val postsSnapshot = db.collection("posts").whereEqualTo("userId", userId).get().await()
                for (document in postsSnapshot.documents) {
                    document.reference.update("username", newUsername).await()
                }
                Log.d("EditProfileViewModel", "Username updated successfully in posts")
                callback(true)
            } catch (e: Exception) {
                Log.e("EditProfileViewModel", "Error updating username in posts: ${e.message}")
                callback(false)
            }
        }
    }

    fun updateAvatarInPosts(newAvatarUrl: String, callback: (Boolean) -> Unit) {
        val userId = firebaseAuth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val postsSnapshot = db.collection("posts")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                for (document in postsSnapshot.documents) {
                    document.reference.update("avatar", newAvatarUrl).await()
                }

                Log.d("EditProfileViewModel", "Avatar updated successfully in posts")
                callback(true)
            } catch (e: Exception) {
                Log.e("EditProfileViewModel", "Error updating avatar in posts: ${e.message}")
                callback(false)
            }
        }
    }


    private fun getRealPathFromURI(activity: Activity, uri: Uri): String {
        var path = ""
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = activity.contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                path = it.getString(columnIndex)
            }
        }
        return path
    }

}
