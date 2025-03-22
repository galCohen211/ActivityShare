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
            val posts = fetchFromFirestore()
            postDao.clearAllPosts()
            postDao.insertPosts(posts)
            Log.d("Repository", "Refreshed ${posts.size} posts from Firestore")
            posts
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

    suspend fun insertSinglePost(post: PostEntity) {
        withContext(Dispatchers.IO) {
            postDao.insertPosts(listOf(post))
        }
    }
}
