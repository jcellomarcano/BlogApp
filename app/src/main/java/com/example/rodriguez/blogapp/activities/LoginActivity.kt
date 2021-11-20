package com.example.rodriguez.blogapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.rodriguez.blogapp.R
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private var userMail: EditText? = null
    private var userPassword: EditText? = null
    private var btnLogin: Button? = null
    private var loginProgress: ProgressBar? = null
    private var mAuth: FirebaseAuth? = null
    private var HomeActivity: Intent? = null
    private var loginPhoto: ImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        userMail = findViewById(R.id.login_mail)
        userPassword = findViewById(R.id.login_password)
        btnLogin = findViewById(R.id.loginBtn)
        loginProgress = findViewById(R.id.login_progress)
        mAuth = FirebaseAuth.getInstance()
        HomeActivity = Intent(this, HomeFunctionalActivity::class.java)
        loginPhoto = findViewById(R.id.login_photo)
        loginPhoto!!.setOnClickListener {
            val registerActivity = Intent(applicationContext, RegisterActivity::class.java)
            startActivity(registerActivity)
            finish()
        }
        loginProgress!!.visibility = View.INVISIBLE
        btnLogin!!.setOnClickListener {
            loginProgress!!.visibility = View.VISIBLE
            btnLogin!!.visibility = View.INVISIBLE
            val mail = userMail!!.text.toString()
            val password = userPassword!!.text.toString()
            if (mail.isEmpty() || password.isEmpty()) {
                showMessage("Please Verify All Field")
                btnLogin!!.setVisibility(View.VISIBLE)
                loginProgress!!.setVisibility(View.INVISIBLE)
            } else {
                signIn(mail, password)
            }
        }
    }

    private fun signIn(mail: String, password: String) {
        mAuth!!.signInWithEmailAndPassword(mail, password)
            .addOnCompleteListener { task: Task<AuthResult?> ->
                if (task.isSuccessful) {
                    loginProgress!!.visibility = View.INVISIBLE
                    btnLogin!!.visibility = View.VISIBLE
                    updateUI()
                } else {
                    showMessage(task.exception!!.message)
                    btnLogin!!.visibility = View.VISIBLE
                    loginProgress!!.visibility = View.INVISIBLE
                }
            }
    }

    private fun updateUI() {
        startActivity(HomeActivity)
        finish()
    }

    private fun showMessage(text: String?) {
        Toast.makeText(applicationContext, text, Toast.LENGTH_LONG).show()
    }

    override fun onStart() {
        super.onStart()
        val user = mAuth!!.currentUser
        if (user != null) {
            // user is already connected  so we need to redirect him to home page
            updateUI()
        }
    }
}
