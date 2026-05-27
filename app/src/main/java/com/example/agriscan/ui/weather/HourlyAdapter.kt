package com.example.agriscan.ui.weather

import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.agriscan.R

class HourlyAdapter(private val list: List<HourlyModel>) :
    RecyclerView.Adapter<HourlyAdapter.ViewHolder>() {

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val time: TextView = v.findViewById(R.id.time)
        val temp: TextView = v.findViewById(R.id.temp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hourly, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.time.text = item.time
        holder.temp.text = "${item.temp}°"
    }

    override fun getItemCount() = list.size
}