package com.example.activityshare.modules.profile

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.example.activityshare.R

class editProfile : Fragment() {

    private lateinit var passwordEditText: EditText
    private lateinit var updateButton: Button
    private lateinit var usernameEditText: EditText
    private lateinit var viewModel: editProfileViewModel
    private lateinit var profileImageView: ImageView
    private var imageUri: Uri? = null


    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
            profileImageView.setImageURI(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        passwordEditText = view.findViewById(R.id.fragment_edit_profile_password)
        updateButton = view.findViewById(R.id.fragment_edit_profile_update_button)
        usernameEditText = view.findViewById(R.id.fragment_edit_profile_username)
        profileImageView = view.findViewById(R.id.fragment_edit_profile_image)

        viewModel = ViewModelProvider(this).get(editProfileViewModel::class.java)

        updateButton.setOnClickListener {
            val newPassword = passwordEditText.text.toString().trim()
            val newUsername = usernameEditText.text.toString().trim()

            if (newPassword.isNotEmpty()) {
                updatePassword()
            }
            if (newUsername.isNotEmpty()) {
                updateUsername()
            }
            imageUri?.let { uri ->
                uploadImageToFirebase(uri)
            }
        }

        profileImageView.setOnClickListener {
            pickImageFromGallery()
        }

        return view
    }

    private fun pickImageFromGallery() {
        imagePickerLauncher.launch("image/*")
    }

    private fun uploadImageToFirebase(uri: Uri) {
        viewModel.uploadProfileImage(uri, requireActivity()) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Profile image updated successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Error updating profile image.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updatePassword() {
        val newPassword = passwordEditText.text.toString().trim()
        viewModel.updatePassword(newPassword, requireActivity()) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Password updated successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Error updating password.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUsername() {
        val newUsername = usernameEditText.text.toString().trim()
        viewModel.updateUsername(newUsername, requireActivity()) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Username updated successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Error updating username.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
