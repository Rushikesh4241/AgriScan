package com.example.agriscan.ui.history

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.agriscan.R
import com.example.agriscan.data.DatabaseProvider
import com.google.firebase.auth.FirebaseAuth
import kotlin.concurrent.thread


class HistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        recyclerView = findViewById(R.id.recyclerHistory)
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadHistory()
    }

    private fun loadHistory() {

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        thread {

            val predictions = DatabaseProvider
                .getDatabase(this)
                .appDao()
                .getPredictions(uid)

            runOnUiThread {
                adapter = HistoryAdapter(predictions)
                recyclerView.adapter = adapter
            }
        }
    }
}
