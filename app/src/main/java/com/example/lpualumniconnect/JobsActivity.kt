package com.example.lpualumniconnect

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.lpualumniconnect.fragments.FragmentCreateJob
import com.example.lpualumniconnect.fragments.FragmentFindJob
import com.google.android.material.bottomnavigation.BottomNavigationView

class JobsActivity : AppCompatActivity() {

    private lateinit var progressDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_jobs)

        setupProgressDialog()

        Handler().postDelayed({
            progressDialog.dismiss()
        }, 3000)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            var newVisibility = visibility
            newVisibility = visibility or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_FULLSCREEN
            window.decorView.systemUiVisibility = newVisibility
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_create_job -> {
                    replaceFragment(FragmentCreateJob())
                    true
                }
                R.id.navigation_find_job -> {
                    replaceFragment(FragmentFindJob())
                    true
                }
                else -> false
            }
        }

        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.navigation_find_job
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .commit()
    }

    private fun setupProgressDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(50, 50, 50, 50)
        }

        val progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleLarge).apply {
            isIndeterminate = true
        }
        val textView = TextView(this).apply {
            text = "Loading..."
            textSize = 18f
            gravity = Gravity.CENTER
        }

        layout.addView(progressBar)
        layout.addView(textView)

        val builder = AlertDialog.Builder(this)
        builder.setView(layout)
        builder.setCancelable(false)
        progressDialog = builder.create()
        progressDialog.show()
    }
}