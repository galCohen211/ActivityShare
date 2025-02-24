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
    val imageUri: String = ""
) : Parcelable