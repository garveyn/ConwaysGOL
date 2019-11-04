package com.nick.conwaygameoflife

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView

class ConwayAdapter(var cellArr: ArrayList<Cell>, val context: Context)
    : RecyclerView.Adapter<ConwayAdapter.ConwayHolder>() {

    val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    inner class ConwayHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val cellImage: ImageView = itemView.findViewById(R.id.cell_imageView)
        private lateinit var cell: Cell

        fun bind(cell: Cell){
            this.cell = cell
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConwayHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.holder_cell, parent, false)
        return ConwayHolder(view)
    }

    override fun getItemCount(): Int = cellArr.size

    override fun onBindViewHolder(holder: ConwayHolder, position: Int) {
        val cell = cellArr[position]
        holder.bind(cell)
    }


}