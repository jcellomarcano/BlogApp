package com.example.rodriguez.blogapp.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.rodriguez.blogapp.R
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.util.*

class RegisterActivity : AppCompatActivity() {
    var ImgUserPhoto: ImageView? = null
    var pickedImgUri: Uri? = null
    private var userEmail: EditText? = null
    private var userPassword: EditText? = null
    private var userPAssword2: EditText? = null
    private var userName: EditText? = null
    private var loadingProgress: ProgressBar? = null
    private var regBtn: Button? = null
    private var mAuth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // ini views
        userEmail = findViewById(R.id.regMail)
        userPassword = findViewById(R.id.regPassword)
        userPAssword2 = findViewById(R.id.regPassword2)
        userName = findViewById(R.id.regName)
        loadingProgress = findViewById(R.id.regProgressBar)
        regBtn = findViewById(R.id.regBtn)
        loadingProgress!!.visibility = View.INVISIBLE
        mAuth = FirebaseAuth.getInstance()
        regBtn!!.setOnClickListener {
            regBtn!!.visibility = View.INVISIBLE
            loadingProgress!!.visibility = View.VISIBLE
            val email = userEmail!!.text.toString()
            val password = userPassword!!.text.toString()
            val password2 = userPAssword2!!.text.toString()
            val name = userName!!.text.toString()
            if (email.isEmpty() || name.isEmpty() || password.isEmpty() || password != password2) {

                // something goes wrong : all fields must be filled
                // we need to display an error message
                showMessage("Please Verify all fields")
                regBtn!!.visibility = View.VISIBLE
                loadingProgress!!.visibility = View.INVISIBLE
            } else {
                // everything is ok and all fields are filled now we can start creating user account
                // CreateUserAccount method will try to create the user if the email is valid
                CreateUserAccount(email, name, password)
            }
        }
        ImgUserPhoto = findViewById(R.id.regUserPhoto)
        ImgUserPhoto!!.setOnClickListener { checkAndRequestForPermission() }
    }

    private fun CreateUserAccount(email: String, name: String, password: String) {

        // this method create user account with specific email and password
        mAuth!!.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task: Task<AuthResult?> ->
                if (task.isSuccessful) {

                    // user account created successfully
                    showMessage("Account created")
                    // after we created user account we need to update his profile picture and name
                    updateUserInfo(name, pickedImgUri, mAuth!!.currentUser)
                } else {

                    // account creation failed
                    Log.i(
                        "RegisterActivity",
                        "CreateUserAccount: " + task.exception!!.message
                    )
                    Log.i(
                        "RegisterActivity",
                        "CreateUserAccount: " + task.exception!!.cause
                    )
                    Log.i(
                        "RegisterActivity",
                        "CreateUserAccount: " + task.exception!!.stackTrace
                    )
                    showMessage("account creation failed " + task.exception!!.message)
                    regBtn!!.visibility = View.VISIBLE
                    loadingProgress!!.visibility = View.INVISIBLE
                }
            }
    }

    // update user photo and name
    private fun updateUserInfo(name: String, pickedImgUri: Uri?, currentUser: FirebaseUser?) {

        // first we need to upload user photo to firebase storage and get url
        val mStorage = FirebaseStorage.getInstance().reference.child("users_photos")
        val imageFilePath = mStorage.child(pickedImgUri!!.lastPathSegment!!)
        imageFilePath.putFile(pickedImgUri)
            .addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot? ->

                // image uploaded succesfully
                // now we can get our image url
                imageFilePath.downloadUrl.addOnSuccessListener { uri: Uri? ->

                    // uri contain user image url
                    val profleUpdate = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .setPhotoUri(uri)
                        .build()
                    currentUser!!.updateProfile(profleUpdate)
                        .addOnCompleteListener { task: Task<Void?> ->
                            if (task.isSuccessful) {
                                // user info updated successfully
                                showMessage("Register Complete")
                                updateUI()
                            }
                        }
                }
            }
    }

    private fun updateUI() {
        val homeActivity = Intent(applicationContext, HomeFunctionalActivity::class.java)
        startActivity(homeActivity)
        finish()
    }

    // simple method to show toast message
    private fun showMessage(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }

    private fun openGallery() {
        // TODO: open gallery intent and wait for user to pick an image !
        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
        galleryIntent.type = "image/*"
        startActivityForResult(galleryIntent, REQUESCODE)
    }

    private fun checkAndRequestForPermission() {
        if (ContextCompat.checkSelfPermission(
                this@RegisterActivity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this@RegisterActivity,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                Toast.makeText(
                    this@RegisterActivity,
                    "Please accept for required permission",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                ActivityCompat.requestPermissions(
                    this@RegisterActivity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PReqCode
                )
            }
        } else openGallery()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == REQUESCODE && data != null) {

            // the user has successfully picked an image
            // we need to save its reference to a Uri variable
            pickedImgUri = data.data
            ImgUserPhoto!!.setImageURI(pickedImgUri)
        }
    }

    companion object {
        var PReqCode = 1
        var REQUESCODE = 1
    }
}
