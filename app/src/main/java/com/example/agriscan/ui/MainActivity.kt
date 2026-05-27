package com.example.agriscan.ui

import com.example.agriscan.ui.profile.ProfileActivity
import com.google.android.material.button.MaterialButton
import com.example.agriscan.ui.history.HistoryActivity
import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.agriscan.R
import com.example.agriscan.data.*
import com.example.agriscan.ml.TFLiteHelper
import com.example.agriscan.ui.auth.LoginActivity
import com.example.agriscan.ui.result.ResultActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.auth.api.signin.*
import org.json.JSONObject
import kotlin.concurrent.thread



class MainActivity : AppCompatActivity() {

    private lateinit var imgPreview: ImageView
    private lateinit var btnPredict: Button
    private lateinit var btnChangeImage: Button
    private lateinit var tfliteHelper: TFLiteHelper

    private var selectedBitmap: Bitmap? = null
    private var imageUri: Uri? = null

    private val IMAGE_SIZE = 224
    private val CAMERA_REQUEST_CODE = 101
    private val CAMERA_PERMISSION_CODE = 102
    private val GALLERY_REQUEST_CODE = 201

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imgPreview = findViewById(R.id.imgPreview)
        btnPredict = findViewById(R.id.btnPredict)
        btnChangeImage = findViewById(R.id.btnChangeImage)

        val imgProfile = findViewById<ImageView>(R.id.imgProfile)
        imgProfile.setOnClickListener {
            it.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(100)
                .withEndAction {
                    it.animate().scaleX(1f).scaleY(1f).duration = 100
                    startActivity(Intent(this, ProfileActivity::class.java))
                }
        }

        btnPredict.visibility = View.GONE
        btnChangeImage.visibility = View.GONE

        tfliteHelper = TFLiteHelper(this)

        handleUserProfile()

        imgProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        findViewById<Button>(R.id.btnCamera).setOnClickListener {
            checkCameraPermission()
        }

        findViewById<Button>(R.id.btnGallery).setOnClickListener {
            openGallery()
        }

        findViewById<Button>(R.id.btnHistory).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        btnPredict.setOnClickListener {
            runPrediction()
        }

        btnChangeImage.setOnClickListener {
            imgPreview.setImageDrawable(null)
            imgPreview.visibility = View.GONE
            btnPredict.visibility = View.GONE
            btnChangeImage.visibility = View.GONE
            selectedBitmap = null
        }

        val btnWeather = findViewById<Button>(R.id.btnWeather)

        btnWeather.setOnClickListener {
            startActivity(Intent(this, com.example.agriscan.ui.weather.WeatherActivity::class.java))
        }

        val btnAbout = findViewById<MaterialButton>(R.id.btnAbout)

        btnAbout.setOnClickListener {
            startActivity(
                Intent(this, com.example.agriscan.ui.about.AboutActivity::class.java)
            )
        }
    }

    // ===============================
    // 👤 STEP 6 — USER PROFILE HANDLER
    // ===============================
    private fun handleUserProfile() {

        val firebaseUser = FirebaseAuth.getInstance().currentUser ?: return
        val uid = firebaseUser.uid

        thread {

            val dao = DatabaseProvider
                .getDatabase(this)
                .appDao()

            val existingUser = dao.getUser(uid)

            if (existingUser == null) {

                // First time login → Save profile
                val newUser = UserProfile(
                    uid = uid,
                    name = firebaseUser.displayName ?: "Farmer",
                    email = firebaseUser.email ?: "",
                    phone = firebaseUser.phoneNumber ?: "",
                    location = "Unknown"
                )

                dao.insertUser(newUser)

                runOnUiThread {
                    Toast.makeText(
                        this,
                        "Welcome ${newUser.name}",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } else {

                // Returning user → Load profile
                runOnUiThread {
                    Toast.makeText(
                        this,
                        "Welcome back ${existingUser.name}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }


    // ---------------- CAMERA ----------------

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            openCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        }
    }

    private fun openCamera() {

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "AgriScan_${System.currentTimeMillis()}")
            put(MediaStore.Images.Media.DESCRIPTION, "High quality image")
        }

        imageUri = contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        )

        if (imageUri == null) {
            Toast.makeText(this, "Failed to create image", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    // ---------------- GALLERY ----------------

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    // ---------------- PERMISSION RESULT ----------------

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            openCamera()
        }
    }

    // ---------------- ACTIVITY RESULT ----------------

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) return

        try {

            when (requestCode) {

                CAMERA_REQUEST_CODE -> {
                    if (imageUri != null) {

                        val inputStream = contentResolver.openInputStream(imageUri!!)
                        val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)

                        inputStream?.close()

                        if (bitmap != null) {
                            handleImage(bitmap)
                        } else {
                            Toast.makeText(this, "Camera image failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                GALLERY_REQUEST_CODE -> {
                    val uri = data?.data
                    imageUri = uri

                    val inputStream = contentResolver.openInputStream(uri!!)
                    val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)

                    inputStream?.close()

                    if (bitmap != null) {
                        handleImage(bitmap)
                    }
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Image load failed", Toast.LENGTH_SHORT).show()
        }
    }

    // ---------------- IMAGE HANDLING ----------------

    private fun handleImage(bitmap: Bitmap) {

        selectedBitmap = bitmap

        val previewBitmap = Bitmap.createScaledBitmap(bitmap, 600, 600, true)

        // IMAGE FADE ANIMATION
        imgPreview.alpha = 0f
        imgPreview.setImageBitmap(previewBitmap)
        imgPreview.visibility = View.VISIBLE

        imgPreview.animate()
            .alpha(1f)
            .setDuration(500)
            .start()

        // BUTTON ANIMATION
        btnPredict.alpha = 0f
        btnPredict.visibility = View.VISIBLE
        btnPredict.animate().alpha(1f).setDuration(400)

        btnChangeImage.alpha = 0f
        btnChangeImage.visibility = View.VISIBLE
        btnChangeImage.animate().alpha(1f).setDuration(400)
    }

    // ---------------- PREDICTION ----------------

    // ONLY MODEL + PREDICTION IMPROVED (rest untouched)

    private fun runPrediction() {

        val bitmap = selectedBitmap
        if (bitmap == null) {
            Toast.makeText(this, "Select an image first", Toast.LENGTH_SHORT).show()
            return
        }

        // 🔥 SAFE LOADING UI (no deprecated dialog crash)
        val loading = android.app.AlertDialog.Builder(this)
            .setView(layoutInflater.inflate(R.layout.loading_layout, null))
            .setCancelable(false)
            .create()

        loading.show()

        Thread {
            try {

                // 🔥 PREPROCESS
                val input = preprocessBitmap(bitmap)

                // 🔥 RUN MODEL
                val result = runInference(input)
                val index = result.first
                val confidence = result.second

                // 🔥 LOAD LABELS SAFELY
                val labels = loadClassLabels()
                val diseaseName = labels[index] ?: "Unknown"

                runOnUiThread {

                    loading.dismiss()

                    try {
                        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

                        val prediction = Prediction(
                            uid = uid,
                            imageUri = imageUri?.toString() ?: "",   // ✅ FIXED
                            diseaseName = diseaseName,
                            confidence = confidence,
                            timestamp = System.currentTimeMillis()
                        )

                        Thread {
                            DatabaseProvider
                                .getDatabase(this)
                                .appDao()
                                .insertPrediction(prediction)
                        }.start()

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    val intent = Intent(this, ResultActivity::class.java)
                    intent.putExtra("disease_name", diseaseName)
                    intent.putExtra("confidence", confidence)
                    intent.putExtra("image_uri", imageUri?.toString() ?: "")

                    startActivity(intent)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }

            } catch (e: Exception) {

                e.printStackTrace()

                runOnUiThread {
                    loading.dismiss()
                    Toast.makeText(
                        this,
                        "Prediction failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }.start()
    }

    // ---------------- PREPROCESSING ----------------

    private fun preprocessBitmap(bitmap: Bitmap): Array<Array<Array<FloatArray>>> {

        val resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true)

        val input = Array(1) { Array(224) { Array(224) { FloatArray(3) } } }

        for (y in 0 until 224) {
            for (x in 0 until 224) {

                val pixel = resized.getPixel(x, y)

                input[0][y][x][0] = ((pixel shr 16) and 0xFF) / 255f
                input[0][y][x][1] = ((pixel shr 8) and 0xFF) / 255f
                input[0][y][x][2] = (pixel and 0xFF) / 255f
            }
        }

        return input
    }

    // ---------------- INFERENCE ----------------

    private fun runInference(input: Array<Array<Array<FloatArray>>>): Pair<Int, Float> {

        val interpreter = tfliteHelper.loadModel()

        val numClasses = interpreter.getOutputTensor(0).shape()[1]
        val output = Array(1) { FloatArray(numClasses) }

        interpreter.run(input, output)

        var maxIndex = 0
        var maxValue = output[0][0]

        for (i in output[0].indices) {
            if (output[0][i] > maxValue) {
                maxValue = output[0][i]
                maxIndex = i
            }
        }

        return Pair(maxIndex, maxValue * 100f)
    }

    // ---------------- LABEL LOADING ----------------

    private fun loadClassLabels(): Map<Int, String> {

        val json = assets.open("class_labels.json")
            .bufferedReader().use { it.readText() }

        val jsonObject = JSONObject(json)

        val map = mutableMapOf<Int, String>()
        jsonObject.keys().forEach { key ->
            map[key.toInt()] = jsonObject.getString(key)
        }

        return map
    }
}
