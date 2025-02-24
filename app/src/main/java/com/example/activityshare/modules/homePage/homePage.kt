package com.example.activityshare.modules.homePage

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.activityshare.R
import com.example.activityshare.model.Post
import com.example.activityshare.modules.addActivitySharePost.PostsAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class homePage : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var postsAdapter: PostsAdapter
    private val postList = mutableListOf<Post>()
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home_page, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewHome)
        progressBar = ProgressBar(requireContext())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        postsAdapter = PostsAdapter(postList)
        recyclerView.adapter = postsAdapter

        fetchUserPosts()

        return view
    }

    private fun fetchUserPosts() {
        progressBar.visibility = View.VISIBLE

        firestore.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                progressBar.visibility = View.GONE
                postList.clear()
                for (document in documents) {
                    val post = document.toObject(Post::class.java)
                    Toast.makeText(requireContext(), post.content, Toast.LENGTH_SHORT).show()
                    postList.add(post)
                }
                postsAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Failed to load posts: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
