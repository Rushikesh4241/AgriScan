package com.example.agriscan.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Prediction")
data class Prediction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val uid: String,                 // ✅ MUST ADD
    val diseaseName: String,
    val confidence: Float,
    val imageUri: String,
    val timestamp: Long
)
