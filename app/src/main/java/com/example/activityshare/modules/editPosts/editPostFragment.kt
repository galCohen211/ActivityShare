package com.example.activityshare.modules.editPost

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.activityshare.R
import com.example.activityshare.model.Post
import com.example.activityshare.modules.addActivitySharePost.ImgurClient
//import com.example.activityshare.network.ImgurClient
//import com.example.activityshare.network.ImgurResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class EditPostFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var postId: String? = null
    private var currentImageUri: Uri? = null
    private var oldImageUrl: String? = null

    private lateinit var existingPostImageView: ImageView
    private lateinit var editPostContent: EditText
    private lateinit var selectNewImageButton: Button
    private lateinit var saveChangesButton: Button
    private lateinit var cancelButton: Button

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                currentImageUri = it
                existingPostImageView.setImageURI(it)
                existingPostImageView.visibility = View.VISIBLE
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        postId = arguments?.getString("postId") // Retrieve postId from navigation arguments
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_edit_post, container, false)

        existingPostImageView = view.findViewById(R.id.existingPostImageView)
        editPostContent = view.findViewById(R.id.editPostContent)
        selectNewImageButton = view.findViewById(R.id.selectNewImageButton)
        saveChangesButton = view.findViewById(R.id.saveChangesButton)
        cancelButton = view.findViewById(R.id.cancelButton)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postId?.let {
            loadPostData(it)
        }

        selectNewImageButton.setOnClickListener {
            pickImageFromGallery()
        }

        saveChangesButton.setOnClickListener {
            saveUpdatedPost()
        }

        cancelButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun loadPostData(postId: String) {
        firestore.collection("posts").document(postId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val post = document.toObject(Post::class.java)
                    post?.let {
                        editPostContent.setText(it.content)
                        oldImageUrl = it.imageUri
                        if (!it.imageUri.isNullOrEmpty()) {
                            existingPostImageView.visibility = View.VISIBLE
                            Glide.with(requireContext()).load(it.imageUri)
                                .into(existingPostImageView)
                        }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load post", Toast.LENGTH_SHORT).show()
            }
    }

    private fun pickImageFromGallery() {
        imagePickerLauncher.launch("image/*")
    }

    private fun saveUpdatedPost() {
        val newContent = editPostContent.text.toString().trim()

        if (newContent.isEmpty()) {
            Toast.makeText(requireContext(), "Content cannot be empty!", Toast.LENGTH_SHORT).show()
            return
        }

        postId?.let { id ->
            if (currentImageUri != null) {
                uploadImageToImgur(id, newContent)
            } else {
                updatePostInFirestore(id, newContent, oldImageUrl)
            }
        }
    }

    private fun getFileFromUri(uri: Uri): File? {
        val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return null
        val tempFile = File.createTempFile("temp_image", ".jpg", requireContext().cacheDir)
        tempFile.outputStream().use { output ->
            inputStream.copyTo(output)
        }
        return tempFile
    }

    private fun uploadImageToImgur(postId: String, newContent: String) {
        val imgurApi = ImgurClient.create()

        val imageFile = getFileFromUri(currentImageUri ?: return) ?: return
        val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)

        val clientId = "Client-ID 58f3986e4ad864f"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = imgurApi.uploadImage(clientId, body)
                if (response.isSuccessful && response.body()?.success == true) {
                    val imgurLink = response.body()?.data?.link
                    withContext(Dispatchers.Main) {
                        updatePostInFirestore(postId, newContent, imgurLink)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Imgur upload failed", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error uploading image", Toast.LENGTH_SHORT)
                        .show()

                    Log.e("EditProfileViewModel", "Error updating username in posts: ${e.message}")
                }
            }
        }
    }

    private fun updatePostInFirestore(postId: String, newContent: String, newImageUrl: String?) {
        val postUpdates = mapOf(
            "content" to newContent,
            "imageUri" to (newImageUrl ?: oldImageUrl ?: "")
        )

        firestore.collection("posts").document(postId)
            .update(postUpdates)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Post updated successfully", Toast.LENGTH_SHORT)
                    .show()
                findNavController().navigateUp()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update post", Toast.LENGTH_SHORT).show()
            }
    }
}
