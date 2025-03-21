package com.example.activityshare.repository
import android.content.Context
import android.util.Log
import com.example.activityshare.data.local.PostDatabase
import com.example.activityshare.model.PostEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class PostRepository(context: Context) {
    private val postDao = PostDatabase.getDatabase(context).postDao()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun fetchPosts(): List<PostEntity> {
        return withContext(Dispatchers.IO) {
            try {
                val cachedPosts = postDao.getAllPosts()
                Log.d("Repository", "Found ${cachedPosts.size} posts in Room")

                if (cachedPosts.isNotEmpty()) {
                    cachedPosts
                } else {
                    val posts = fetchFromFirestore()
                    Log.d("Repository", "Fetched ${posts.size} posts from Firestore")

                    postDao.insertPosts(posts)
                    Log.d("Repository", "Inserted posts into Room")

                    posts
                }
            } catch (e: Exception) {
                Log.e("Repository", "fetchPosts error", e)
                throw e
            }
        }
    }

    private suspend fun fetchFromFirestore(): List<PostEntity> {
        return withContext(Dispatchers.IO) {
            val snapshot = firestore.collection("posts").get().await()
            val posts = snapshot.documents.mapNotNull { doc ->
                doc.toObject(PostEntity::class.java)
            }
            posts
        }
    }
}
