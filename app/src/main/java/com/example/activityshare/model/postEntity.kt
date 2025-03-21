import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class postEntity(
    @PrimaryKey val postId: String,
    val userId: String,
    val content: String,
    val date: String,
    val time: String,
    val imageUri: String,
    val username: String,
    val avatar: String
)
