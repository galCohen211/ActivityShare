package com.example.activityshare.modules.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.activityshare.R

class profile : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Logout button
        val binding = inflater.inflate(R.layout.fragment_profile, container, false)
        val logoutButton = binding.findViewById<View>(R.id.fragment_profile_logout_button)
        val editProfile = binding.findViewById<View>(R.id.fragment_profile_edit_profile_button)

        logoutButton.setOnClickListener {
            findNavController().navigate(R.id.login_Fragment)
        }
        editProfile.setOnClickListener{
            findNavController().navigate(R.id.editProfile)
        }
        return binding
    }
}
