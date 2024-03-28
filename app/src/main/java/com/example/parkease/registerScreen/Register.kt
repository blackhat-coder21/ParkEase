package com.example.parkease.registerScreen

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.parkease.HomeScreen.Home
import com.example.parkease.MainActivity
import com.example.parkease.R
import com.example.parkease.databinding.ActivityRegisterBinding
import com.example.parkease.loginScreen.LoginPage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Register : AppCompatActivity() {

    lateinit var auth: FirebaseAuth
    lateinit var binding: ActivityRegisterBinding
    private lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)


        val login = findViewById<Button>(R.id.btnsignin)
        val email = findViewById<EditText>(R.id.email)
        val password = findViewById<EditText>(R.id.pwd1)
        val confirmPassword = findViewById<EditText>(R.id.pwd2)
        val sign_up = findViewById<ConstraintLayout>(R.id.register_layout)

        login.setOnClickListener {
            if(email.text.toString().isEmpty()) {
                email.error = "Please enter your email"
                email.requestFocus()
                return@setOnClickListener
            }
            else if(!email.text.toString().contains("@")) {
                email.error = "Please enter a valid email"
                email.requestFocus()
                return@setOnClickListener
            }
            else if(password.text.toString().isEmpty()) {
                password.error = "Please enter your password"
                password.requestFocus()
                return@setOnClickListener
            }
            else if(password.text.toString().length < 8) {
                password.error = "Password should be at least 8 characters long"
                password.requestFocus()
                return@setOnClickListener
            }
            else if(confirmPassword.text.toString().isEmpty()) {
                confirmPassword.error = "Please confirm your password"
                confirmPassword.requestFocus()
                return@setOnClickListener
            }
            else if(password.text.toString() != confirmPassword.text.toString()) {
                confirmPassword.error = "Passwords do not match"
                confirmPassword.requestFocus()
                return@setOnClickListener
            }
            else{
                createUserAccount()
            }
        }

        sign_up.setOnClickListener{
            intent = Intent(applicationContext, LoginPage::class.java)
            startActivity(intent)
            finish()
        }

        var fade_in = AnimationUtils.loadAnimation(this,R.anim.fade_in)
        var bottom_down = AnimationUtils.loadAnimation(this,R.anim.bottom_down)


        binding.topLinearLayout!!.animation = bottom_down

        var handler = Handler()
        var runnable = Runnable{
            binding.textView3!!.animation = fade_in
            binding.registerLayout!!.animation = fade_in
            binding.textView!!.animation = fade_in
            binding.textView2!!.animation = fade_in
            binding.logoView!!.animation = fade_in
        }

        handler.postDelayed(runnable,1000)
    }

    private fun createUserAccount() {
        progressDialog.setMessage("Creating account...")
        progressDialog.show()
        val email = findViewById<EditText>(R.id.email)
        val password = findViewById<EditText>(R.id.pwd1)

        auth.createUserWithEmailAndPassword(email.text.toString(), password.text.toString())
            .addOnCompleteListener(this){ task ->
                if(task.isSuccessful) {
                    progressDialog.setMessage("Saving user info...")
                    val currentUser = auth.currentUser
                    val uid = currentUser?.uid
                    val email = findViewById<EditText>(R.id.email).text.toString()
                    val password = findViewById<EditText>(R.id.pwd1).text.toString()
                    val timestamp: String = System.currentTimeMillis().toString()
                    val hashMap: HashMap<String, String> = HashMap()
                    hashMap["uid"] = uid.toString()
                    hashMap["email"] = email
                    hashMap["password"] = password
//                    hashMap["name"] = name
                    hashMap["profileImage"] = ""
                    hashMap["userType"] = "user"
                    hashMap["timestamp"] = timestamp

                    val ref = FirebaseDatabase.getInstance().getReference("Users")
                    ref.child(uid.toString()).setValue(hashMap)
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            Toast.makeText(this, "Account created...", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, Home::class.java)
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener {
                            progressDialog.dismiss()
                            Toast.makeText(this, "SignIn Failed", Toast.LENGTH_SHORT).show()
                        }
                }
                else {
                    Log.w("RegisterActivity", "createUserWithEmail:failure", task.exception)
                    progressDialog.dismiss()
                    if (task.exception is com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                        Toast.makeText(this, "Account already exists", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, LoginPage::class.java)
                        startActivity(intent)
                    }
                    else {
                        email.error = "Please enter a valid email"
                        email.requestFocus()
                    }
                }
            }
    }
}