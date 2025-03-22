package com.example.activityshare.modules.homePage

import android.app.DatePickerDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.activityshare.R
import com.example.activityshare.model.Post
import com.example.activityshare.model.toPost
import com.example.activityshare.modules.addActivitySharePost.PostsAdapter
import com.example.activityshare.repository.PostRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import okhttp3.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.ConnectionSpec
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class homePage : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var postsAdapter: PostsAdapter
    private val postList = mutableListOf<Post>()
    private lateinit var progressBar: ProgressBar
    private lateinit var repository: PostRepository
    private lateinit var filterFab: FloatingActionButton
    private var selectedDate: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home_page, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewHome)
        progressBar = view.findViewById(R.id.fragment_home_page_progress_bar_home)
        filterFab = view.findViewById(R.id.filterFab)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        postsAdapter = PostsAdapter(postList, findNavController())
        recyclerView.adapter = postsAdapter

        repository = PostRepository(requireContext())

        fetchPostsFromRoom()

        filterFab.setOnClickListener {
            showDatePicker()
        }

        return view
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selected = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)
                selectedDate = selected
                fetchPostsFromRoom()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun fetchPostsFromRoom() {
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val postsFromRoom = repository.fetchPosts()

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                postList.clear()
                postList.addAll(
                    postsFromRoom
                        .map { it.toPost() }
                        .sortedByDescending { sdf.parse(it.date) }
                        .filter { post ->
                            val postDate = sdf.parse(post.date)?.let { sdf.format(it) }
                            Log.d("FilterCheck", "Comparing: $postDate == $selectedDate")
                            selectedDate == null || postDate == selectedDate
                        }
                )
                postsAdapter.notifyDataSetChanged()
                Log.d("homePage", "Fetched ${postList.size} sorted posts from Room")

            } catch (e: Exception) {
                Log.e("homePage", "Full error", e)
                Toast.makeText(requireContext(), "Error loading posts", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
}
