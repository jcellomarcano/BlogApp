package com.example.rodriguez.blogapp.models

import com.google.firebase.database.ServerValue

data class Comment(var content: String?, var uid: String?, var uimg: String?, var uname: String?) {
    var timestamp: Any? = null

    init {
        timestamp = ServerValue.TIMESTAMP
    }
}
