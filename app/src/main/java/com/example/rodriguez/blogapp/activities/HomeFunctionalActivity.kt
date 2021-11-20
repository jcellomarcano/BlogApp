package com.example.rodriguez.blogapp.activities

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.example.rodriguez.blogapp.fragments.HomeFragment
import com.example.rodriguez.blogapp.fragments.ProfileFragment
import com.example.rodriguez.blogapp.fragments.SettingsFragment
import com.example.rodriguez.blogapp.models.Post
import com.example.rodriguez.blogapp.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class HomeFunctionalActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    var mAuth: FirebaseAuth? = null
    var currentUser: FirebaseUser? = null
    var popAddPost: Dialog? = null
    var popupUserImage: ImageView? = null
    var popupPostImage: ImageView? = null
    var popupAddBtn: ImageView? = null
    var popupTitle: TextView? = null
    var popupDescription: TextView? = null
    var popupClickProgress: ProgressBar? = null
    private var pickedImgUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home2)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // ini
        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth!!.currentUser

        // ini popup
        iniPopup()
        setupPopupImageClick()
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener { popAddPost!!.show() }
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer.addDrawerListener(toggle)
        toggle.syncState()
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
        updateNavHeader()

        // set the home fragment as the default one
        supportFragmentManager.beginTransaction().replace(R.id.container, HomeFragment()).commit()
    }

    private fun setupPopupImageClick() {
        popupPostImage!!.setOnClickListener {
            // here when image clicked we need to open the gallery
            // before we open the gallery we need to check if our app have the access to user files
            // we did this before in register activity I'm just going to copy the code to save time ...
            checkAndRequestForPermission()
        }
    }

    private fun checkAndRequestForPermission() {
        if (ContextCompat.checkSelfPermission(this@HomeFunctionalActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this@HomeFunctionalActivity,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                Toast.makeText(
                    this@HomeFunctionalActivity,
                    "Please accept for required permission",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                ActivityCompat.requestPermissions(
                    this@HomeFunctionalActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PReqCode
                )
            }
        } else // everything goes well : we have permission to access user gallery
            openGallery()
    }

    private fun openGallery() {
        // TODO: open gallery intent and wait for user to pick an image !
        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
        galleryIntent.type = "image/*"
        startActivityForResult(galleryIntent, REQUESCODE)
    }

    // when user picked an image ...
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == REQUESCODE && data != null) {

            // the user has successfully picked an image
            // we need to save its reference to a Uri variable
            pickedImgUri = data.data
            popupPostImage!!.setImageURI(pickedImgUri)
        }
    }

    private fun iniPopup() {
        popAddPost = Dialog(this)
        popAddPost!!.setContentView(R.layout.popup_add_post)
        popAddPost!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popAddPost!!.window!!
            .setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT)
        popAddPost!!.window!!.attributes.gravity = Gravity.TOP

        // ini popup widgets
        popupUserImage = popAddPost!!.findViewById(R.id.popup_user_image)
        popupPostImage = popAddPost!!.findViewById(R.id.popup_img)
        popupTitle = popAddPost!!.findViewById(R.id.popup_title)
        popupDescription = popAddPost!!.findViewById(R.id.popup_description)
        popupAddBtn = popAddPost!!.findViewById(R.id.popup_add)
        popupClickProgress = popAddPost!!.findViewById(R.id.popup_progressBar)

        // load Current user profile photo
        Glide.with(this@HomeFunctionalActivity).load(currentUser!!.photoUrl).into(popupUserImage!!)

        // Add post click Listener
        popupAddBtn!!.setOnClickListener {
            popupAddBtn!!.visibility = View.INVISIBLE
            popupClickProgress!!.visibility = View.VISIBLE

            // we need to test all input fields (Title and description ) and post image
            if (popupTitle!!.text.toString().isNotEmpty() &&
                popupDescription!!.text.toString().isNotEmpty() &&
                pickedImgUri != null
            ) {

                // everything is okey no empty or null value
                // TODO Create Post Object and add it to firebase database
                // first we need to upload post Image
                // access firebase storage
                val storageReference = FirebaseStorage.getInstance().reference.child("blog_images")
                val imageFilePath = storageReference.child(pickedImgUri!!.lastPathSegment!!)
                imageFilePath.putFile(pickedImgUri!!)
                    .addOnSuccessListener {
                        imageFilePath.downloadUrl.addOnSuccessListener { uri: Uri ->
                            val imageDownloadLink = uri.toString()
                            // create post Object
                            val post = Post(
                                popupTitle!!.text.toString(),
                                popupDescription!!.text.toString(),
                                imageDownloadLink,
                                currentUser!!.uid,
                                Objects.requireNonNull(currentUser!!.photoUrl).toString()
                            )

                            // Add post to firebase database
                            addPost(post)
                        }.addOnFailureListener { e: Exception ->
                            // something goes wrong uploading picture
                            showMessage(e.message)
                            popupClickProgress!!.visibility = View.INVISIBLE
                            popupAddBtn!!.visibility = View.VISIBLE
                        }
                    }
            } else {
                showMessage("Please verify all input fields and choose Post Image")
                popupAddBtn!!.visibility = View.VISIBLE
                popupClickProgress!!.visibility = View.INVISIBLE
            }
        }
    }

    private fun addPost(post: Post) {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("Posts").push()

        // get post unique ID and upadte post key
        val key = myRef.key
        post.postKey = key

        // add post data to firebase database
        myRef.setValue(post).addOnSuccessListener {
            showMessage("Post Added successfully")
            popupClickProgress!!.visibility = View.INVISIBLE
            popupAddBtn!!.visibility = View.VISIBLE
            popAddPost!!.dismiss()
        }
    }

    private fun showMessage(message: String?) {
        Toast.makeText(this@HomeFunctionalActivity, message, Toast.LENGTH_LONG).show()
    }

    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        return if (id == R.id.action_settings) {
            true
        } else super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_home -> {
                supportActionBar!!.title = "Home"
                supportFragmentManager.beginTransaction().replace(R.id.container, HomeFragment())
                    .commit()
            }
            R.id.nav_profile -> {
                supportActionBar!!.title = "Profile"
                supportFragmentManager.beginTransaction().replace(R.id.container, ProfileFragment())
                    .commit()
            }
            R.id.nav_settings -> {
                supportActionBar!!.title = "Settings"
                supportFragmentManager.beginTransaction().replace(R.id.container, SettingsFragment())
                    .commit()
            }
            R.id.nav_signout -> {
                FirebaseAuth.getInstance().signOut()
                val loginActivity = Intent(applicationContext, LoginActivity::class.java)
                startActivity(loginActivity)
                finish()
            }
        }
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    private fun updateNavHeader() {
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val headerView = navigationView.getHeaderView(0)
        val navUsername = headerView.findViewById<TextView>(R.id.nav_username)
        val navUserMail = headerView.findViewById<TextView>(R.id.nav_user_mail)
        val navUserPhoto = headerView.findViewById<ImageView>(R.id.nav_user_photo)
        navUserMail.text = currentUser!!.email
        navUsername.text = currentUser!!.displayName

        // now we will use Glide to load user image
        // first we need to import the library
        Glide.with(this).load(currentUser!!.photoUrl).into(navUserPhoto)
    }

    companion object {
        private const val PReqCode = 2
        private const val REQUESCODE = 2
    }
}
