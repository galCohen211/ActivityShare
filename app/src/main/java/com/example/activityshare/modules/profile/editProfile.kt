package com.example.activityshare.modules.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.activityshare.R

class editProfile : Fragment() {

    private lateinit var passwordEditText: EditText
    private lateinit var updateButton: Button
    private lateinit var usernameEditText: EditText
    private lateinit var viewModel: EditProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        passwordEditText = view.findViewById(R.id.fragment_edit_profile_password)
        updateButton = view.findViewById(R.id.fragment_edit_profile_update_button)
        usernameEditText = view.findViewById(R.id.fragment_edit_profile_username)

        viewModel = ViewModelProvider(this).get(EditProfileViewModel::class.java)

        updateButton.setOnClickListener {
            updatePassword()
            updateUsername()
        }

        return view
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
