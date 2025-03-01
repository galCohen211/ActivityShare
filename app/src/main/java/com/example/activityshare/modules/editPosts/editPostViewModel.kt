package com.example.activityshare.modules.posts

import android.app.Activity
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.example.activityshare.modules.addActivitySharePost.ImgurClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class EditPostViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    // Update the post content (Text, Date, Time) in Firestore
    fun updatePostContent(
        postId: String,
        newContent: String,
        newDate: String,
        newTime: String,
        activity: Activity,
        callback: (Boolean) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return

        val postRef = firestore.collection("posts").document(postId)
        postRef.update(
            mapOf(
                "content" to newContent,
                "date" to newDate,
                "time" to newTime
            )
        ).addOnSuccessListener {
            Toast.makeText(activity, "Post updated successfully!", Toast.LENGTH_SHORT).show()
            callback(true)
        }.addOnFailureListener {
            Toast.makeText(activity, "Failed to update post.", Toast.LENGTH_SHORT).show()
            callback(false)
        }
    }

    // Upload the new post image to Firebase Storage
    fun updatePostImage(
        postId: String,
        imageUri: Uri,
        activity: Activity,
        callback: (Boolean) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return
        val storageRef = storage.reference.child("post_images/$postId.jpg")

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    updatePostImageUrl(postId, uri.toString(), activity, callback)
                }
            }
            .addOnFailureListener {
                Toast.makeText(activity, "Image upload failed.", Toast.LENGTH_SHORT).show()
                callback(false)
            }
    }

    // Save the new image URL in Firestore
    private fun updatePostImageUrl(
        postId: String,
        imageUrl: String,
        activity: Activity,
        callback: (Boolean) -> Unit
    ) {
        val postRef = firestore.collection("posts").document(postId)
        postRef.update("imageUri", imageUrl)
            .addOnSuccessListener {
                Toast.makeText(activity, "Post image updated!", Toast.LENGTH_SHORT).show()
                callback(true)
            }
            .addOnFailureListener {
                Toast.makeText(activity, "Failed to update post image.", Toast.LENGTH_SHORT).show()
                callback(false)
            }
    }

    // Upload the new post image to Imgur (Alternative to Firebase Storage)
    fun uploadImageToImgur(
        postId: String,
        imageUri: Uri,
        activity: Activity,
        callback: (Boolean, String?) -> Unit
    ) {
        val imgurApi = ImgurClient.create()
        val imageFile = File(getRealPathFromURI(imageUri, activity))
        val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)

        val clientId = "Client-ID 58f3986e4ad864f"
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = imgurApi.uploadImage(clientId, body)
                if (response.isSuccessful && response.body()?.success == true) {
                    val imgurLink = response.body()?.data?.link
                    withContext(Dispatchers.Main) {
                        updatePostImageUrl(postId, imgurLink!!, activity) { success ->
                            callback(success, imgurLink)
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(activity, "Imgur upload failed", Toast.LENGTH_SHORT).show()
                        callback(false, null)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(activity, "Error uploading image", Toast.LENGTH_SHORT).show()
                    callback(false, null)
                }
            }
        }
    }

    private fun getRealPathFromURI(uri: Uri, activity: Activity): String {
        var path = ""
        val projection = arrayOf(android.provider.MediaStore.Images.Media.DATA)
        val cursor = activity.contentResolver.query(uri, projection, null, null, null)
        cursor?.let {
            if (it.moveToFirst()) {
                val columnIndex =
                    it.getColumnIndexOrThrow(android.provider.MediaStore.Images.Media.DATA)
                path = it.getString(columnIndex)
            }
            it.close()
        }
        return path
    }
}
