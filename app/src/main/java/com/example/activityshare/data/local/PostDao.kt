package com.example.activityshare.data.local
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.activityshare.model.PostEntity

@Dao
interface PostDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<PostEntity>)

    @Query("SELECT * FROM posts ORDER BY date DESC")
    suspend fun getAllPosts(): List<PostEntity>

    @Query("DELETE FROM posts")
    suspend fun clearAllPosts()
}
