package com.example.activityshare.model
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val postId: String = "",
    val userId: String = "",
    val content: String = "",
    val date: String = "",
    val time: String = "",
    val imageUri: String = "",
    val username: String = "",
    val avatar: String = ""
)

