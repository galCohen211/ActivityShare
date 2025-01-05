package com.example.activityshare.modules.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import androidx.navigation.Navigation
import com.example.activityshare.R

class LoginFragment : Fragment() {

    private lateinit var Email: EditText
    private lateinit var Password: EditText
    private lateinit var btnLogin: Button
    private lateinit var linkRegister : TextView

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        Email = view.findViewById(R.id.fragment_login_email)
        Password = view.findViewById(R.id.fragment_login_password)
        btnLogin = view.findViewById(R.id.fragment_login_btn_login)
        linkRegister = view.findViewById(R.id.fragment_login_signup_link)

        linkRegister.setOnClickListener{
            Navigation.findNavController(view).navigate(R.id.register_Fragment)
        }

        btnLogin.setOnClickListener {
            val email = Email.text.toString()
            val password = Password.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                // Perform login with Firebase Auth
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Login successful
                            Toast.makeText(requireContext(), "Welcome $email", Toast.LENGTH_LONG).show()
                            Navigation.findNavController(view).navigate(R.id.homePage)
                        } else {
                            // Login failed
                            Toast.makeText(requireContext(), "Login failed: invalid password or email", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(requireContext(), "Please enter both email and password", Toast.LENGTH_LONG).show()
            }
        }

        return view
    }
}
