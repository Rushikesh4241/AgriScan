package com.example.agriscan.ui.result

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.agriscan.R
import org.json.JSONObject

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val tvDisease = findViewById<TextView>(R.id.tvDisease)
        val tvConfidence = findViewById<TextView>(R.id.tvConfidence)
        val tvTreatment = findViewById<TextView>(R.id.tvTreatment)
        val tvPrevention = findViewById<TextView>(R.id.tvPrevention)
        val imgResult = findViewById<ImageView>(R.id.imgResult)

        val diseaseName = intent.getStringExtra("disease_name") ?: "Unknown"
        val confidence = intent.getFloatExtra("confidence", 0f)
        val imageUri = intent.getStringExtra("image_uri")



        // 🔥 Show Image
        if (!imageUri.isNullOrEmpty()) {
            try {
                imgResult.setImageURI(Uri.parse(imageUri))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 🔥 Show Disease
        tvDisease.text = diseaseName.replace("___", " - ")
        tvConfidence.text = "Confidence: ${confidence.toInt()}%"

        // 🔥 Load Treatment + Prevention
        val (treatment, prevention) = loadTreatmentData(diseaseName)

        tvTreatment.text = treatment
        tvPrevention.text = prevention
    }

    // ---------------- LOAD DATA ----------------

    private fun loadTreatmentData(diseaseName: String): Pair<String, String> {

        return try {
            val json = assets.open("disease_treatment.json")
                .bufferedReader().use { it.readText() }

            val jsonObject = JSONObject(json)

            // 🔥 SAFE MATCH (handles spaces/issues)
            val key = jsonObject.keys().asSequence().firstOrNull {
                it.lowercase().replace(" ", "_") ==
                        diseaseName.lowercase().replace(" ", "_")
            } ?: return Pair(
                "No treatment available",
                "No prevention available"
            )

            val obj = jsonObject.getJSONObject(key)

            val treatmentArray = obj.getJSONArray("treatment")
            val preventionArray = obj.getJSONArray("prevention")

            val treatmentText = StringBuilder()
            val preventionText = StringBuilder()

            for (i in 0 until treatmentArray.length()) {
                treatmentText.append("• ").append(treatmentArray.getString(i)).append("\n")
            }

            for (i in 0 until preventionArray.length()) {
                preventionText.append("• ").append(preventionArray.getString(i)).append("\n")
            }

            Pair(treatmentText.toString(), preventionText.toString())

        } catch (e: Exception) {
            Pair(
                "No treatment information available.",
                "No prevention information available."
            )
        }
    }
}