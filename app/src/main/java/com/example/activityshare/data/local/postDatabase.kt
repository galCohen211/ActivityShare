import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [postEntity::class], version = 1, exportSchema = false)
abstract class postDatabase : RoomDatabase() {
    abstract fun postDao(): postDao

    companion object {
        @Volatile
        private var INSTANCE: postDatabase? = null

        fun getDatabase(context: Context): postDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    postDatabase::class.java,
                    "post_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
