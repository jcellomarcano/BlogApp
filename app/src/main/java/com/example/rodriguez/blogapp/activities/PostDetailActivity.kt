package com.example.rodriguez.blogapp.activities

import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rodriguez.blogapp.R
import com.example.rodriguez.blogapp.adapters.CommentAdapter
import com.example.rodriguez.blogapp.models.Comment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class PostDetailActivity : AppCompatActivity() {
    var imgPost: ImageView? = null
    var imgUserPost: ImageView? = null
    var imgCurrentUser: ImageView? = null
    var txtPostDesc: TextView? = null
    var txtPostDateName: TextView? = null
    var txtPostTitle: TextView? = null
    var editTextComment: EditText? = null
    var btnAddComment: Button? = null
    var PostKey: String? = null
    var firebaseAuth: FirebaseAuth? = null
    var firebaseUser: FirebaseUser? = null
    var firebaseDatabase: FirebaseDatabase? = null
    var RvComment: RecyclerView? = null
    var commentAdapter: CommentAdapter? = null
    var listComment: MutableList<Comment?>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)

        // let's set the statue bar to transparent
        val w = window
        w.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        supportActionBar!!.hide()

        // ini Views
        RvComment = findViewById(R.id.rv_comment)
        imgPost = findViewById(R.id.post_detail_img)
        imgUserPost = findViewById(R.id.post_detail_user_img)
        imgCurrentUser = findViewById(R.id.post_detail_currentuser_img)
        txtPostTitle = findViewById(R.id.post_detail_title)
        txtPostDesc = findViewById(R.id.post_detail_desc)
        txtPostDateName = findViewById(R.id.post_detail_date_name)
        editTextComment = findViewById(R.id.post_detail_comment)
        btnAddComment = findViewById(R.id.post_detail_add_comment_btn)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth!!.currentUser
        firebaseDatabase = FirebaseDatabase.getInstance()

        // add Comment button click listner
        btnAddComment!!.setOnClickListener {
            btnAddComment!!.visibility = View.INVISIBLE
            val commentReference = firebaseDatabase!!.getReference(COMMENT_KEY).child(
                PostKey!!
            ).push()
            val comment_content = editTextComment!!.text.toString()
            val uid = firebaseUser!!.uid
            val uname = firebaseUser!!.displayName
            val uimg = firebaseUser!!.photoUrl.toString()
            val comment = Comment(comment_content, uid, uimg, uname)
            commentReference.setValue(comment).addOnSuccessListener { aVoid: Void? ->
                showMessage("comment added")
                editTextComment!!.setText("")
                btnAddComment!!.visibility = View.VISIBLE
            }.addOnFailureListener { e -> showMessage("fail to add comment : " + e.message) }
        }

        // now we need to bind all data into those views
        // firt we need to get post data
        // we need to send post detail data to this activity first ...
        // now we can get post data
        val postImage = intent.extras!!.getString("postImage")
        Glide.with(this).load(postImage).into(imgPost!!)
        val postTitle = intent.extras!!.getString("title")
        txtPostTitle!!.text = postTitle
        val userpostImage = intent.extras!!.getString("userPhoto")
        Glide.with(this).load(userpostImage).into(imgUserPost!!)
        val postDescription = intent.extras!!.getString("description")
        txtPostDesc!!.text = postDescription

        // setcomment user image
        Glide.with(this).load(firebaseUser!!.photoUrl).into(imgCurrentUser!!)
        // get post id
        PostKey = intent.extras!!.getString("postKey")
        val date = timestampToString(intent.extras!!.getLong("postDate"))
        txtPostDateName!!.text = date

        // ini Recyclerview Comment
        iniRvComment()
    }

    private fun iniRvComment() {
        RvComment!!.layoutManager = LinearLayoutManager(this)
        val commentRef = firebaseDatabase!!.getReference(COMMENT_KEY).child(
            PostKey!!
        )
        commentRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                listComment = ArrayList()
                for (snap in dataSnapshot.children) {
                    val comment = snap.getValue(
                        Comment::class.java
                    )
                    listComment!!.add(comment)
                }
                commentAdapter = CommentAdapter(applicationContext, listComment)
                RvComment!!.adapter = commentAdapter
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun timestampToString(time: Long): String {
        val calendar =
            Calendar.getInstance(Locale.ENGLISH)
        calendar.timeInMillis = time
        return DateFormat.format("dd-MM-yyyy", calendar).toString()
    }

    companion object {
        var COMMENT_KEY = "Comment"
    }
}
