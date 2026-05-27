package com.example.agriscan.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserProfile(

    @PrimaryKey
    val uid: String,        // Firebase UID (unique user ID)

    val name: String,       // Farmer name
    val email: String,      // Gmail login email
    val phone: String,      // Phone number (OTP login)
    val location: String    // Village / District / State
)
