package com.example.lpualumniconnect

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() {

    private lateinit var email : TextInputEditText
    private lateinit var pass : TextInputEditText
    private lateinit var btn : MaterialButton
    private lateinit var tv : MaterialTextView
    private lateinit var forgotpasstv : MaterialTextView

    private lateinit var mAuth : FirebaseAuth
    private lateinit var progressDialog: ProgressDialog


    @SuppressLint("ObsoleteSdkInt", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.apply {
                clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                statusBarColor = Color.TRANSPARENT
            }
        }
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Signing in...")
        progressDialog.setCancelable(false)
        progressDialog.setCanceledOnTouchOutside(false)


        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Checking session...")
        progressDialog.setCancelable(false)
        progressDialog.setCanceledOnTouchOutside(false)



        mAuth = FirebaseAuth.getInstance()

        email = findViewById(R.id.signInName)
        pass = findViewById(R.id.signInPass)
        btn = findViewById(R.id.SignInButton)
        tv = findViewById(R.id.SignInTextView)
        forgotpasstv = findViewById(R.id.SignInForgotPassword)


        btn.setOnClickListener {
            signIn()
        }

        tv.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

        }

        forgotpasstv.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        if (mAuth.currentUser != null && mAuth.currentUser!!.isEmailVerified) {
            startActivity(Intent(this, NavigationActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            finish()
        }
    }

    private fun signIn() {
        val emailText = email.text.toString().trim()
        val passwordText = pass.text.toString().trim()

        if (emailText.isEmpty() || passwordText.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        progressDialog.show()

        mAuth.signInWithEmailAndPassword(emailText, passwordText)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = mAuth.currentUser
                    if (user != null && user.isEmailVerified) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            progressDialog.dismiss()
                            Toast.makeText(this, "Sign-in successful", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, NavigationActivity::class.java))
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                            finish()
                        }, 3000)
                    } else {
                        progressDialog.dismiss()
                        Toast.makeText(this, "Please verify your email address", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }



}