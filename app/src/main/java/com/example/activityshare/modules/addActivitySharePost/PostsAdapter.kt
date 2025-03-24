package com.example.activityshare.modules.addActivitySharePost

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.example.activityshare.R
import com.bumptech.glide.Glide
import com.example.activityshare.model.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PostsAdapter(
    private val postList: MutableList<Post>,
    private val navController: NavController,
    private val showEditButton: Boolean
) : RecyclerView.Adapter<PostsAdapter.PostViewHolder>() {

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postImage: ImageView = itemView.findViewById(R.id.postImage)
        val content: TextView = itemView.findViewById(R.id.content)
        val date: TextView = itemView.findViewById(R.id.date)
        val time: TextView = itemView.findViewById(R.id.time)
        val userImage: ImageView = itemView.findViewById(R.id.post_user_image)
        val username: TextView = itemView.findViewById(R.id.post_username)
        val editButton: ImageButton = itemView.findViewById(R.id.edit_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.post_item, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        holder.content.text = post.content
        holder.date.text = post.date
        holder.time.text = post.time
        holder.username.text = post.username
        Glide.with(holder.userImage.context)
            .load(post.avatar)
            .circleCrop()
            .into(holder.userImage)

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        // Load image using Glide
        if (post.imageUri.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(post.imageUri)
                .into(holder.postImage)
        } else {
            holder.postImage.setImageResource(R.drawable.add_photo) // Add a default image
        }

        val deleteButton = holder.itemView.findViewById<ImageButton>(R.id.delete_button)

        // Show edit and delete buttons only if the current user is the post owner
        if (showEditButton && post.userId == currentUserId) {
            holder.editButton.visibility = View.VISIBLE
            holder.editButton.setOnClickListener {
                val action =
                    com.example.activityshare.modules.myPosts.MyPostsFragmentDirections.actionMyPostsFragmentToEditPost(
                        post.postId
                    )

                navController.navigate(action)
            }
            deleteButton.visibility = View.VISIBLE
            deleteButton.setOnClickListener {
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("posts").document(post.postId)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(holder.itemView.context, "Post deleted", Toast.LENGTH_SHORT)
                            .show()
                        val position = holder.adapterPosition
                        if (position != RecyclerView.NO_POSITION) {
                            postList.removeAt(position)
                            notifyItemRemoved(position)
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            holder.itemView.context,
                            "Failed to delete",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        } else {
            holder.editButton.visibility = View.GONE
            deleteButton.visibility = View.GONE

        }
    }

    override fun getItemCount(): Int = postList.size

}