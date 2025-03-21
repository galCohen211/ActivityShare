import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface postDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<postEntity>)

    @Query("SELECT * FROM posts ORDER BY date DESC")
    suspend fun getAllPosts(): List<postEntity>

    @Query("DELETE FROM posts")
    suspend fun clearAllPosts()
}
