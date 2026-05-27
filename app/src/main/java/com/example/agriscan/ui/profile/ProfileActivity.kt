package com.example.agriscan.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.agriscan.R
import com.example.agriscan.data.DatabaseProvider
import com.example.agriscan.data.UserProfile
import com.example.agriscan.ui.auth.LoginActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import kotlin.concurrent.thread

class ProfileActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etLocation: EditText
    private lateinit var btnSave: MaterialButton
    private lateinit var btnLogout: MaterialButton

    private var uid: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        etLocation = findViewById(R.id.etLocation)
        btnSave = findViewById(R.id.btnSave)
        btnLogout = findViewById(R.id.btnLogout)

        uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        loadProfile()

        btnSave.setOnClickListener {
            saveProfile()
        }

        // 🔥 LOGOUT WITH CONFIRMATION + GOOGLE + FIREBASE
        btnLogout.setOnClickListener {

            AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes") { _, _ ->
                    logoutUser()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    // ===============================
    // 🔐 LOGOUT FUNCTION (GOOGLE + FIREBASE)
    // ===============================
    private fun logoutUser() {

        FirebaseAuth.getInstance().signOut()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleSignInClient.signOut().addOnCompleteListener {

            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
            finish()
        }
    }

    // ===============================
    // 👤 LOAD PROFILE
    // ===============================
    private fun loadProfile() {

        thread {

            val user = DatabaseProvider
                .getDatabase(this)
                .appDao()
                .getUser(uid)

            runOnUiThread {

                if (user != null) {
                    etName.setText(user.name)
                    etEmail.setText(user.email)
                    etPhone.setText(user.phone)
                    etLocation.setText(user.location)
                }
            }
        }
    }

    // ===============================
    // 💾 SAVE PROFILE
    // ===============================
    private fun saveProfile() {

        val updatedUser = UserProfile(
            uid = uid,
            name = etName.text.toString(),
            email = etEmail.text.toString(),
            phone = etPhone.text.toString(),
            location = etLocation.text.toString()
        )

        thread {

            DatabaseProvider
                .getDatabase(this)
                .appDao()
                .insertUser(updatedUser)

            runOnUiThread {
                Toast.makeText(
                    this,
                    "Profile Saved",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}