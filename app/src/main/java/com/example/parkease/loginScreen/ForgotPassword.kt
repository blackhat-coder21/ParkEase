package com.example.parkease.loginScreen

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.example.parkease.R
import com.google.firebase.auth.FirebaseAuth

class ForgotPassword : AppCompatActivity() {

    private lateinit var forgetbtn: Button
    private lateinit var txtEmail: EditText
    private var email: String = ""
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        supportActionBar?.hide()
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);


        val back = findViewById<Button>(R.id.btnback)

        auth = FirebaseAuth.getInstance();
        txtEmail = findViewById<EditText>(R.id.email);
        forgetbtn = findViewById<Button>(R.id.btnreset);

        forgetbtn.setOnClickListener{
            validateData();
        }

        back.setOnClickListener{
            intent = Intent(applicationContext, LoginPage::class.java)
            startActivity(intent)
            finish()
        }

    }

    private fun validateData() {
        email = txtEmail.text.toString()
        if (email.isEmpty()) {
            txtEmail.error = "Required"
        } else {
            forgetPass()
        }
    }

    private fun forgetPass() {
        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Reset link sent to gmail", Toast.LENGTH_SHORT).show()
                val intent = Intent(applicationContext, LoginPage::class.java)
                startActivity(intent)
                finish()
            }
            else {
                Toast.makeText(this, "Error: " + task.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}