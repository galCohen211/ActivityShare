package com.example.activityshare.modules.addActivitySharePost

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.activityshare.R
import com.bumptech.glide.Glide
import com.example.activityshare.model.Post

class PostsAdapter(
    private val postList: List<Post>
) : RecyclerView.Adapter<PostsAdapter.PostViewHolder>() {

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postImage: ImageView = itemView.findViewById(R.id.postImage)
        val content: TextView = itemView.findViewById(R.id.content)
        val date: TextView = itemView.findViewById(R.id.date)
        val time: TextView = itemView.findViewById(R.id.time)
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
        // Load image using Glide
        if (post.imageUri.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(post.imageUri)
                .into(holder.postImage)
        } else {
            holder.postImage.setImageResource(R.drawable.add_photo) // Add a default image
        }
    }

    override fun getItemCount(): Int = postList.size

}