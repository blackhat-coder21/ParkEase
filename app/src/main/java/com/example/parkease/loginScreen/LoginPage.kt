package com.example.parkease.loginScreen

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.parkease.HomeScreen.Home
import com.example.parkease.MainActivity
import com.example.parkease.R
import com.example.parkease.databinding.ActivityLoginPageBinding
import com.example.parkease.registerScreen.Register
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginPage : AppCompatActivity() {

    lateinit var auth: FirebaseAuth
    lateinit var binding: ActivityLoginPageBinding
    private lateinit var progressDialog: ProgressDialog

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        supportActionBar?.hide()
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        binding = ActivityLoginPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)


        val login = findViewById<Button>(R.id.btnlogin)
        val sign_up = findViewById<ConstraintLayout>(R.id.register_layout)
        val gmail_page = findViewById<ImageView>(R.id.gmail_page)
        val forgetpwd = findViewById<TextView>(R.id.forgetpwd)


        login.setOnClickListener {

            val email = findViewById<EditText>(R.id.email)
            val password = findViewById<EditText>(R.id.pwd)

            if(email.text.toString().isEmpty())
            {
                email.error = "Please Enter an email"
                email.requestFocus()
                return@setOnClickListener
            }
            else if(!email.text.toString().contains("@")) {
                email.error = "Please enter a valid email"
                email.requestFocus()
                return@setOnClickListener
            }
            else if(password.text.toString().isEmpty())
            {
                password.error = "Please Enter a password"
                password.requestFocus()
                return@setOnClickListener
            }
            else if(password.text.toString().length < 8) {
                password.error = "Password should be at least 8 characters long"
                password.requestFocus()
                return@setOnClickListener
            }
            else{
                loginUser()
            }
        }


        sign_up.setOnClickListener{
            intent = Intent(applicationContext, Register::class.java)
            startActivity(intent)
            finish()
        }

        forgetpwd.setOnClickListener{
            intent = Intent(applicationContext, ForgotPassword::class.java)
            startActivity(intent)
            finish()
        }

        gmail_page.setOnClickListener{
            signIn()
        }

        var fade_in = AnimationUtils.loadAnimation(this,R.anim.fade_in)
        var bottom_down = AnimationUtils.loadAnimation(this,R.anim.bottom_down)


        binding.topLinearLayout!!.animation = bottom_down

        var handler = Handler()
        var runnable = Runnable{
            binding.cardView!!.animation = fade_in
            binding.textView3!!.animation = fade_in
            binding.registerLayout!!.animation = fade_in
            binding.textView!!.animation = fade_in
            binding.textView2!!.animation = fade_in
            binding.logoView!!.animation = fade_in
        }

        handler.postDelayed(runnable,1000)
    }

    private fun signIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, LoginPage.RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        progressDialog.setMessage("Logging In...")
        progressDialog.show()
        if (requestCode == LoginPage.RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val uid = user?.uid
                    val timestamp: String = System.currentTimeMillis().toString()
                    val hashMap: HashMap<String, String> = HashMap()
                    hashMap["uid"] = uid.toString()
                    hashMap["email"] = user?.email.toString()
                    hashMap["name"] = user?.displayName.toString()
                    hashMap["profileImage"] = ""
                    hashMap["userType"] = "user"
                    hashMap["timestamp"] = timestamp

                    val ref = FirebaseDatabase.getInstance().getReference("Users")
                    val userRef = ref.child(uid.toString())

                    userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists()) {
                                progressDialog.dismiss()
                                Toast.makeText(this@LoginPage, "Login Success", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this@LoginPage, Home::class.java)
                                startActivity(intent)
                                finish()
                            }
                            else {
                                userRef.setValue(hashMap)
                                    .addOnSuccessListener {
                                        progressDialog.dismiss()
                                        Toast.makeText(this@LoginPage, "Login Success", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(this@LoginPage, Home::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                                    .addOnFailureListener {
                                        progressDialog.dismiss()
                                        Toast.makeText(this@LoginPage, "SignIn Failed", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                        override fun onCancelled(databaseError: DatabaseError) {
                            progressDialog.dismiss()
                            Toast.makeText(this@LoginPage, "Error checking user data", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
                else {
                    Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }


    private fun loginUser() {
        progressDialog.setMessage("Logging In...")
        progressDialog.show()
        val email = findViewById<EditText>(R.id.email)
        val password = findViewById<EditText>(R.id.pwd)

        auth.signInWithEmailAndPassword(email.text.toString(), password.text.toString())
            .addOnSuccessListener(this) {
                checkUser()
            }
            .addOnFailureListener(this){
                progressDialog.dismiss()
                Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show()
            }
    }
    private fun checkUser() {
        progressDialog.setMessage("Checking User...")

        val firebaseUser = auth.currentUser
        val ref = FirebaseDatabase.getInstance().getReference("Users")

        ref.child(firebaseUser!!.uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userName = snapshot.child("name").getValue(String::class.java)
                val userType = snapshot.child("userType").getValue(String::class.java)

                if (userType == "user") {
                    progressDialog.dismiss()
                    Toast.makeText(this@LoginPage, "Signed in as $userName", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@LoginPage, Home::class.java)
                    startActivity(intent)
                    finish()
                }
//                else if(userType == "admin"){
//                    progressDialog.dismiss()
//                    val txt = "Welcome Admin<br/>Signed in as $userName"
//                    Toast.makeText(this@LoginPage, Html.fromHtml(txt), Toast.LENGTH_SHORT).show()
//                    val intent = Intent(this@LoginPage, AdminActivity::class.java)
//                    startActivity(intent)
//                    finish()
//                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled event
            }
        })
    }
}