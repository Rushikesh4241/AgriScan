package com.example.agriscan.data

import androidx.room.*

@Dao
interface AppDao {

    // 👤 USER PROFILE

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: UserProfile)

    @Query("SELECT * FROM UserProfile WHERE uid = :uid")
    fun getUser(uid: String): UserProfile?

    // 📊 PREDICTIONS

    @Insert
    fun insertPrediction(prediction: Prediction)

    @Query("SELECT * FROM Prediction WHERE uid = :uid ORDER BY timestamp DESC")
    fun getPredictions(uid: String): List<Prediction>

    @Query("DELETE FROM Prediction WHERE uid = :uid")
    fun deleteUserPredictions(uid: String)
}
