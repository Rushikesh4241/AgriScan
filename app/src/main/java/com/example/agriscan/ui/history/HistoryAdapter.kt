package com.example.agriscan.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.agriscan.R
import com.example.agriscan.data.Prediction
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(private val list: List<Prediction>) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val disease: TextView = view.findViewById(R.id.tvDisease)
        val confidence: TextView = view.findViewById(R.id.tvConfidence)
        val date: TextView = view.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = list[position]

        // Disease name
        holder.disease.text = item.diseaseName

        // Confidence
        holder.confidence.text =
            "Confidence: %.1f%%".format(item.confidence)

        // Format date
        val sdf = SimpleDateFormat(
            "dd MMM yyyy, hh:mm a",
            Locale.getDefault()
        )

        holder.date.text = sdf.format(Date(item.timestamp))
    }

    override fun getItemCount() = list.size
}
