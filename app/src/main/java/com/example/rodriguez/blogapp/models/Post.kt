package com.example.rodriguez.blogapp.models

import com.google.firebase.database.ServerValue

data class Post(
    var title: String?,
    var description: String?,
    var picture: String?,
    var userId: String?,
    var userPhoto: String?
) {
    var postKey: String? = null
    var timeStamp: Any? = null

    init {
        timeStamp = ServerValue.TIMESTAMP
    }
}
