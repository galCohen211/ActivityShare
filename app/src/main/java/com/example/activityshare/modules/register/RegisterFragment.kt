package com.example.activityshare.modules.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.activityshare.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class RegisterFragment : Fragment() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnRegister: MaterialButton
    private lateinit var auth: FirebaseAuth
    private lateinit var linkLogin: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        etEmail = view.findViewById(R.id.fragment_register_email)
        etPassword = view.findViewById(R.id.fragment_register_password)
        btnRegister = view.findViewById(R.id.fragment_register_btn_register)
        linkLogin = view.findViewById(R.id.fragment_register_login_link)

        auth = FirebaseAuth.getInstance()

        linkLogin.setOnClickListener(){
            Navigation.findNavController(view).navigate(R.id.login_Fragment)
        }

        btnRegister.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                if(password.length<6){
                    Toast.makeText(requireContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                }
                else{
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(requireActivity()) { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(requireContext(), "User Registered Successfully", Toast.LENGTH_SHORT).show()
                                Navigation.findNavController(view).navigate(R.id.homePage)
                            } else {
                                Toast.makeText(requireContext(), "Registration Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                }

            } else {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}
