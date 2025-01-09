package com.example.activityshare.modules.login
import android.util.Patterns
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.Navigation
import com.example.activityshare.R
import com.google.firebase.auth.FirebaseAuth

class forgotPassword : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var resetPasswordButton: Button
    private lateinit var linkRememberPassword: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_forgot_password, container, false)

        emailEditText = view.findViewById(R.id.fragment_forgot_password_email)
        resetPasswordButton = view.findViewById(R.id.fragment_forgot_password_btn_send)
        linkRememberPassword = view.findViewById(R.id.fragment_forgot_password_link)

        linkRememberPassword.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.login_Fragment)
        }

        resetPasswordButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()

            if (validateEmailUser(email)) {
                sendResetPasswordLink(email)
            }
        }
        return view
    }

    // Function to send the reset link to the email
    private fun sendResetPasswordLink(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Reset password link has been sent. Check your email.", Toast.LENGTH_SHORT).show()
                Navigation.findNavController(requireView()).navigate(R.id.login_Fragment)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // A function to validate the email address
    private fun validateEmailUser(email: String): Boolean {
        return if (email.isEmpty()) {
            emailEditText.error = "Email cannot be empty"
            false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.error = "Invalid email format"
            false
        } else {
            emailEditText.error = null
            true
        }
    }

}