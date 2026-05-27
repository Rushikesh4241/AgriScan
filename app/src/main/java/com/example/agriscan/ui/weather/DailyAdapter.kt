package com.example.agriscan.ui.weather

import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.agriscan.R

class DailyAdapter(private val list: List<DailyModel>) :
    RecyclerView.Adapter<DailyAdapter.ViewHolder>() {

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val day: TextView = v.findViewById(R.id.day)
        val temp: TextView = v.findViewById(R.id.temp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_daily, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.day.text = item.day
        holder.temp.text = "${item.temp}°"
    }

    override fun getItemCount() = list.size
}