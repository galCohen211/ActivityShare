package com.example.activityshare.modules.network.ChatAi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.activityshare.R
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class AIChatFragment : Fragment() {

    private lateinit var editTextQuestion: EditText
    private lateinit var buttonAsk: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var textViewResponse: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ai_chat, container, false)

        // UI Elements
        editTextQuestion = view.findViewById(R.id.editTextQuestion)
        buttonAsk = view.findViewById(R.id.buttonAsk)
        progressBar = view.findViewById(R.id.progressBar)
        textViewResponse = view.findViewById(R.id.textViewResponse)

        buttonAsk.setOnClickListener {
            val question = editTextQuestion.text.toString().trim()
            if (question.isNotEmpty()) {
                makeApiRequest(question)
            } else {
                Toast.makeText(requireContext(), "Please enter a question", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        return view
    }

    private fun makeApiRequest(question: String) {
        val apiKey = "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"
        val model = "tiiuae/falcon-7b-instruct"

        val client = OkHttpClient.Builder()
            .connectionSpecs(listOf(ConnectionSpec.MODERN_TLS))
            .retryOnConnectionFailure(true)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val jsonRequest = JSONObject()
            .put("inputs", question)
            .toString()

        val requestBody =
            RequestBody.create("application/json; charset=utf-8".toMediaType(), jsonRequest)

        val request = Request.Builder()
            .url("https://api-inference.huggingface.co/models/$model")
            .post(requestBody)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    progressBar.visibility = View.GONE
                    textViewResponse.visibility = View.VISIBLE
                    textViewResponse.text = "Failed: ${e.message}"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val responseData = response.body?.string()
                    val jsonArray = JSONArray(responseData)
                    val generatedText = jsonArray.getJSONObject(0).getString("generated_text")

                    activity?.runOnUiThread {
                        progressBar.visibility = View.GONE
                        textViewResponse.visibility = View.VISIBLE
                        textViewResponse.text = generatedText.substring(question.length).trim() ?: "No response from AI"
                    }
                }
            }
        })
    }
}