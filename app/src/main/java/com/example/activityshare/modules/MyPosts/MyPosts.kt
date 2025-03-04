package com.example.activityshare.modules.myPosts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.activityshare.R
import com.example.activityshare.model.Post
import com.example.activityshare.modules.addActivitySharePost.PostsAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyPostsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var myPostsAdapter: PostsAdapter
    private val myPostsList = mutableListOf<Post>()
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var progressBar: ProgressBar
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_posts, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewMyPosts)
        progressBar = view.findViewById(R.id.progressBarMyPosts)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        myPostsAdapter = PostsAdapter(myPostsList)
        recyclerView.adapter = myPostsAdapter

        fetchMyPosts()

        return view
    }

    private fun fetchMyPosts() {
        progressBar.visibility = View.VISIBLE

        firestore.collection("posts")
            .whereEqualTo("userId", currentUserId) // ✅ Filter only posts from the logged-in user
            .get()
            .addOnSuccessListener { documents ->
                progressBar.visibility = View.GONE
                myPostsList.clear()
                for (document in documents) {
                    val post = document.toObject(Post::class.java)
                    myPostsList.add(post)
                }
                myPostsAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                progressBar.visibility = View.GONE
                Toast.makeText(
                    requireContext(),
                    "Failed to load posts: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}
