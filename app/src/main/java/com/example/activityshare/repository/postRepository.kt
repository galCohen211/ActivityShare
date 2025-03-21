import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class postRepository(context: Context) {
    private val postDao = postDatabase.getDatabase(context).postDao()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun fetchPosts(): List<postEntity> {
        return withContext(Dispatchers.IO) {
            val cachedPosts = postDao.getAllPosts()
            if (cachedPosts.isNotEmpty()) {
                cachedPosts
            } else {
                val posts = fetchFromFirestore()
                postDao.insertPosts(posts)
                posts
            }
        }
    }

    private suspend fun fetchFromFirestore(): List<postEntity> {
        return withContext(Dispatchers.IO) {
            val snapshot = firestore.collection("posts").get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(postEntity::class.java)
            }
        }
    }
}
