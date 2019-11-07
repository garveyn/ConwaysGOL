package com.nick.conwaygameoflife

import android.animation.ArgbEvaluator
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView

class ConwayAdapter(var cellArr: Array<Cell>, var size: Int, fragment: ConwayFragment)
    : RecyclerView.Adapter<ConwayAdapter.ConwayHolder>() {

    val context: Context = fragment.requireContext()
    val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    inner class ConwayHolder(itemView: View)
        : RecyclerView.ViewHolder(itemView) {

        val cellImage: ImageView = itemView.findViewById(R.id.cell_imageView)
        private lateinit var cell: Cell

        fun bind(cellToBind: Cell){
            this.cell = cellToBind

            val lifespan = preferences.getInt(context.getString(R.string.lifespan_key),
                GameBoard.IMMORTAL)

            if (cell.isLiving) {
                cellImage.setImageResource(R.drawable.ic_cell)
            } else {
                cellImage.setImageResource(R.drawable.ic_settings)
            }

            cellImage.setColorFilter(getCellColor(lifespan))

            // TODO Setup cell Animation
        }

        private fun getCellColor(lifespan: Int) : Int {
            val backgroundColor = preferences.getInt(context.getString(R.string.cc_dead_key),
                ContextCompat.getColor(context, R.color.default_dark))
            val livingColor = preferences.getInt(context.getString(R.string.cc_live_key),
                ContextCompat.getColor(context, R.color.default_light))
            val oldColor = preferences.getInt(context.getString(R.string.cc_old_key),
                ContextCompat.getColor(context, R.color.default_light_faded))

            return when {
                !cell.isLiving -> backgroundColor

                cell.age == 0 || lifespan == GameBoard.IMMORTAL -> livingColor

                else -> {
                    val relAge = cell.age.toFloat()/lifespan
                    ArgbEvaluator().evaluate(relAge, livingColor, oldColor) as Int
                }
            }
        }

        fun cellClicked() {
            val newCell = Cell(true, 0)
            cellArr[adapterPosition] = newCell
            notifyItemChanged(adapterPosition)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConwayHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.holder_cell, parent, false)

        view.apply {
            minimumHeight = parent.height/size
            minimumWidth = parent.width/size

        }

        return ConwayHolder(view)
    }


    override fun getItemCount(): Int = cellArr.size

    override fun onBindViewHolder(holder: ConwayHolder, position: Int) {
        val cell = cellArr[position]
        holder.bind(cell)
        holder.itemView.setOnClickListener {
            holder.cellClicked()
        }
    }


}