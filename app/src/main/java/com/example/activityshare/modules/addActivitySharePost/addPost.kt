package com.example.activityshare.modules.addActivitySharePost

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.activityshare.R
import com.example.activityshare.model.PostEntity
import com.example.activityshare.modules.network.imgur.ImgurClient
import com.example.activityshare.repository.PostRepository
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
import java.util.*


class addPost : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var content: TextView
    private lateinit var date: TextView
    private lateinit var time: TextView
    private lateinit var createButton: Button
    private lateinit var postImage: ImageView
    private var imageUri: Uri? = null
    private var profileImage: String? = null
    private var username: String? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            handleImageUri(it)
            imageUri = it
        } ?: Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_post, container, false)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Get references to the TextViews
        content = view.findViewById(R.id.fragment_add_post_content)
        date = view.findViewById(R.id.fragment_add_post_date)
        time = view.findViewById(R.id.fragment_add_post_time)
        createButton = view.findViewById(R.id.fragment_add_post_button)
        postImage = view.findViewById(R.id.fragment_add_post_image)

        postImage.setOnClickListener {
            pickImageFromGallery()
        }

        // Set onClickListeners to open Date and Time pickers
        date.setOnClickListener {
            openDatePicker()
        }

        time.setOnClickListener {
            openTimePicker()
        }


        createButton.setOnClickListener {
            val content = content.text.toString().trim()
            val date = date.text.toString().trim()
            val time = time.text.toString().trim()
            val imageUri = imageUri.toString().trim()

            if (content.isNotEmpty() && date.isNotEmpty() && time.isNotEmpty()) {
                if (imageUri != null) {
                    savePostWithImage(content, date, time)
                } else {
                    savePost(content, date, time)
                }

            } else {
                Toast.makeText(
                    requireContext(),
                    "Please fill in all the fields!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        return view
    }

    private fun pickImageFromGallery() {
        // Opens image picker
        imagePickerLauncher.launch("image/*")
    }

    private fun handleImageUri(uri: Uri) {
        postImage.setImageURI(uri)
        Log.d("ImagePicker", "Selected Image URI: $uri")
    }

    // Function to open the DatePickerDialog with restriction to future dates
    private fun openDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Set the date picker to allow only future dates
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                date.text = selectedDate
            },
            year, month, day
        )

        // Prevent selecting past dates
        datePickerDialog.datePicker.minDate = calendar.timeInMillis
        datePickerDialog.show()
    }

    // Function to open the TimePickerDialog with restriction to future times
    private fun openTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        // Create a TimePickerDialog with current time as the minimum time
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                val selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                time.text = selectedTime
            },
            hour, minute, true
        )

        // Prevent selecting past times
        timePickerDialog.updateTime(hour, minute)
        timePickerDialog.show()
    }

    private fun savePostWithImage(content: String, date: String, time: String) {
        val userId = auth.currentUser?.uid ?: return
        val postId = UUID.randomUUID().toString()

        val imgurApi = ImgurClient.create()

        val imageFile = File(getRealPathFromURI(imageUri!!))
        val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)

        val clientId = "Client-ID 58f3986e4ad864f"
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = imgurApi.uploadImage(clientId, body)
                if (response.isSuccessful && response.body()?.success == true) {
                    val imgurLink = response.body()?.data?.link
                    Log.d("ImgurUpload", "Image uploaded: $imgurLink")

                    // Save post to Firestore with Imgur image link
                    withContext(Dispatchers.Main) {
                        savePostToFirestore(postId, userId, content, date, time, imgurLink)
                        Toast.makeText(
                            requireContext(),
                            "Post Created with Imgur Image!",
                            Toast.LENGTH_SHORT
                        ).show()
                        findNavController().navigate(R.id.homePage)
                    }
                } else {
                    Log.e("ImgurUpload", "Upload failed: ${response.errorBody()?.string()}")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Imgur upload failed", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            } catch (e: Exception) {
                Log.e("ImgurUpload", "Exception: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error uploading image", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun getRealPathFromURI(uri: Uri): String {
        var path = ""
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = requireActivity().contentResolver.query(uri, projection, null, null, null)
        cursor?.let {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                path = it.getString(columnIndex)
            }
            it.close()
        }
        return path
    }

    private fun savePost(content: String, date: String, time: String) {
        val userId = auth.currentUser?.uid ?: return
        val postId = UUID.randomUUID().toString()

        savePostToFirestore(postId, userId, content, date, time, null)
    }

    private fun savePostToFirestore(
        postId: String,
        userId: String,
        content: String,
        date: String,
        time: String,
        imageUri: String?
    ) {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { userDocument ->
                val username = userDocument.getString("username")
                val avatar = userDocument.getString("avatar")

                val post = hashMapOf(
                    "postId" to postId,
                    "userId" to userId,
                    "content" to content,
                    "date" to date,
                    "time" to time,
                    "imageUri" to (imageUri ?: ""),
                    "timestamp" to System.currentTimeMillis(),
                    "username" to username,
                    "avatar" to avatar
                )

                firestore.collection("posts").document(postId)
                    .set(post)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Post Created!", Toast.LENGTH_SHORT).show()

                        val postEntity = PostEntity(
                            postId = postId,
                            userId = userId,
                            content = content,
                            date = date,
                            time = time,
                            imageUri = imageUri ?: "",
                            username = username ?: "",
                            avatar = avatar ?: ""
                        )

                        CoroutineScope(Dispatchers.IO).launch {
                            val repository = PostRepository(requireContext())
                            repository.insertSinglePost(postEntity)
                        }

                        findNavController().navigate(R.id.homePage)
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            requireContext(),
                            "Failed to create post",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
    }

}
