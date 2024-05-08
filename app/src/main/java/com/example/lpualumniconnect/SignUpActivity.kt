package com.example.lpualumniconnect

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import com.example.lpualumniconnect.datamodal.User

import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {

    private lateinit var AutoCompleteTextView : AutoCompleteTextView
    private lateinit var name : TextInputEditText
    private lateinit var email : TextInputEditText
    private lateinit var pass : TextInputEditText
    private lateinit var cpass : TextInputEditText
    private lateinit var btn : MaterialButton
    private lateinit var tv : MaterialTextView

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference

    private lateinit var progressDialog: ProgressDialog


    @SuppressLint("MissingInflatedId", "ObsoleteSdkInt")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

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

        name = findViewById(R.id.etname)
        email = findViewById(R.id.etemal)
        pass = findViewById(R.id.etpass)
        cpass = findViewById(R.id.etcpass)
        btn = findViewById(R.id.SignUpButton)
        tv = findViewById(R.id.SignUpTextView)
        AutoCompleteTextView = findViewById(R.id.SignUpAutoCompleteTextView)

        mAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Sending verification email...")
        progressDialog.setCancelable(false)
        progressDialog.setCanceledOnTouchOutside(false)


        val items = listOf("Alumni", "Student")
        val adapter = ArrayAdapter(this, R.layout.sign_up_list_items, items)
        AutoCompleteTextView.setAdapter(adapter)

        btn.setOnClickListener {
            val emailText = email.text.toString().trim()
            val passwordText = pass.text.toString().trim()
            val confirmPasswordText = cpass.text.toString().trim()
            val nameText = name.text.toString().trim()
            val position = AutoCompleteTextView.text.toString().trim()

            if (emailText.isEmpty() || passwordText.isEmpty() || nameText.isEmpty() || confirmPasswordText.isEmpty() || position.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (passwordText != confirmPasswordText) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidPassword(passwordText)) {
                return@setOnClickListener
            }

            signup(nameText, emailText, passwordText, position)
        }

        tv.setOnClickListener{
            startActivity(Intent(this,SignInActivity::class.java))
        }

    }


    private fun isValidPassword(password: String): Boolean {
        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!password.any { it.isDigit() } || !password.any { it.isLowerCase() } || !password.any { it.isUpperCase() }) {
            Toast.makeText(this, "Password must contain at least one digit, one lower case, and one upper case letter", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }


    private fun signup(name: String, email: String, password: String, position: String) {
        progressDialog.show()

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = mAuth.currentUser
                    user?.sendEmailVerification()
                        ?.addOnCompleteListener { verificationTask ->
                            progressDialog.dismiss()

                            if (verificationTask.isSuccessful) {
                                Toast.makeText(this, "Email verification sent to $email", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, "Failed to send email verification", Toast.LENGTH_SHORT).show()
                            }
                        }

                    val uid = user?.uid ?: ""
                    saveToDatabase(name, email, uid, position)
                    startActivity(Intent(this, SignInActivity::class.java))
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                } else {
                    progressDialog.dismiss()
                    Log.w(ContentValues.TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }



    private fun saveToDatabase(name: String, email: String, uid: String, position: String) {
        mDbRef = FirebaseDatabase.getInstance().reference
        val user = User(name, email, uid, null, position, null, null, null, null, null, null, null, null,null, null, null,null)
        mDbRef.child("users").child(uid).setValue(user).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("DatabaseSave", "User data saved successfully")
            } else {
                Log.e("DatabaseSave", "Failed to save user data", task.exception)
                Toast.makeText(this, "Failed to save user data: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

}
