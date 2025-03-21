package com.example.activityshare.modules.homePage

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
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

class homePage : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var postsAdapter: PostsAdapter
    private val postList = mutableListOf<Post>()
    private lateinit var progressBar: ProgressBar
    private lateinit var repository: PostRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home_page, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewHome)
        progressBar = view.findViewById(R.id.fragment_home_page_progress_bar_home)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        postsAdapter = PostsAdapter(postList)
        recyclerView.adapter = postsAdapter

        repository = PostRepository(requireContext())

        fetchPostsFromRoom()

        return view
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
