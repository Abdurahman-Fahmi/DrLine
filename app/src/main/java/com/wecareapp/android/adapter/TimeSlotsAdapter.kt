package com.wecareapp.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.wecareapp.android.R
import com.wecareapp.android.model.TimeSlot
import kotlinx.android.synthetic.main.list_item_time_slot.view.*

class TimeSlotsAdapter(
    private val list: MutableList<TimeSlot>,
    private val listener: Listener?
) :
    RecyclerView.Adapter<TimeSlotsAdapter.ViewHolder>() {

    interface Listener {
        fun onItemSelected(timeSlot: TimeSlot?, adapterPosition: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_time_slot, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textView: AppCompatTextView = view.textView
        fun bind(timeSlot: TimeSlot?) {
            textView.text = timeSlot?.timeSlot?.replace(" ", "")

            if (timeSlot?.selected == true) {
                itemView.background =
                    ContextCompat.getDrawable(itemView.context, R.drawable.slot_selected)
                textView.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
            } else {
                itemView.background =
                    ContextCompat.getDrawable(itemView.context, R.drawable.slot)
                textView.setTextColor(ContextCompat.getColor(itemView.context, R.color.time_slot))
            }

            itemView.setOnClickListener {
                listener?.onItemSelected(timeSlot, adapterPosition)
            }
        }
    }
}