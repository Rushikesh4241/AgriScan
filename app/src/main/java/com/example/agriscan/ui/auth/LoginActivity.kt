package com.example.agriscan.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.agriscan.R
import com.example.agriscan.ui.MainActivity
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit
import com.google.android.gms.common.SignInButton

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var verificationId: String? = null

    // Google Sign-In
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // AUTO LOGIN
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // ===============================
        // GOOGLE SIGN-IN CONFIG
        // ===============================
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        findViewById<SignInButton>(R.id.btnGoogle)
            .setOnClickListener {
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)
            }

        // ===============================
        // PHONE OTP LOGIN
        // ===============================
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etOtp = findViewById<EditText>(R.id.etOtp)
        val btnSendOtp = findViewById<MaterialButton>(R.id.btnSendOtp)
        val btnVerifyOtp = findViewById<MaterialButton>(R.id.btnVerifyOtp)

        btnSendOtp.setOnClickListener {
            val phone = etPhone.text.toString().trim()

            if (phone.isEmpty()) {
                toast("Enter phone number")
                return@setOnClickListener
            }

            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(callbacks)
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)

            etOtp.visibility = View.VISIBLE
            btnVerifyOtp.visibility = View.VISIBLE

            toast("OTP Sent")
        }

        btnVerifyOtp.setOnClickListener {
            val otp = etOtp.text.toString()

            if (otp.isEmpty()) {
                toast("Enter OTP")
                return@setOnClickListener
            }

            if (verificationId == null) {
                toast("OTP not sent yet")
                return@setOnClickListener
            }

            val credential = PhoneAuthProvider.getCredential(verificationId!!, otp)
            signIn(credential)
        }

        // ===============================
        // 🔥 ANIMATION (CARD ENTRY)
        // ===============================
        val card = findViewById<CardView>(R.id.mainCard)

        card.alpha = 0f
        card.translationY = 100f

        card.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(700)
            .start()
    }

    // ===============================
    // PHONE AUTH CALLBACKS
    // ===============================
    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(cred: PhoneAuthCredential) {
            signIn(cred)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            toast(e.message)
        }

        override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
            verificationId = id
        }
    }

    // ===============================
    // GOOGLE RESULT
    // ===============================
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                toast("Google sign-in failed")
            }
        }
    }

    // ===============================
    // FIREBASE GOOGLE AUTH
    // ===============================
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                goHome()
            }
            .addOnFailureListener {
                toast(it.message)
            }
    }

    // ===============================
    // COMMON SIGN-IN
    // ===============================
    private fun signIn(credential: AuthCredential) {
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                goHome()
            }
            .addOnFailureListener {
                toast("Authentication failed")
            }
    }

    private fun goHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun toast(msg: String?) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}