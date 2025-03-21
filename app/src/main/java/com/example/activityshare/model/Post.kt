package com.example.activityshare.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Post(
    val postId: String = "",
    val userId: String = "",
    val content: String = "",
    val date: String = "",
    val time: String = "",
    val imageUri: String = "",
    val username: String = "",
    val avatar: String = ""
) : Parcelable

fun PostEntity.toPost(): Post {
    return Post(
        postId = this.postId,
        userId = this.userId,
        content = this.content,
        date = this.date,
        time = this.time,
        imageUri = this.imageUri,
        username = this.username,
        avatar = this.avatar
    )
}