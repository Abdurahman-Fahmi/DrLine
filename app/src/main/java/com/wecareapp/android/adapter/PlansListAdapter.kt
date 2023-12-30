package com.wecareapp.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.wecareapp.android.R
import com.wecareapp.android.model.PlansItem
import kotlinx.android.synthetic.main.list_item_price.view.*

class PlansListAdapter(
    private val list: MutableList<PlansItem?>,
    private val listener: Listener?
) :
    RecyclerView.Adapter<PlansListAdapter.ViewHolder>() {

    interface Listener {
        fun onItemSelected(clinic: PlansItem?, adapterPosition: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_price, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvClinicName: AppCompatTextView = view.tvClinicName
        val tvClinicLocation: AppCompatTextView = view.tvClinicLocation
        fun bind(plansItem: PlansItem?) {
            tvClinicName.text = plansItem?.currency + " " + plansItem?.planPrice
            tvClinicLocation.text = plansItem?.planName
            itemView.setOnClickListener {
                listener?.onItemSelected(plansItem, adapterPosition)
            }

            val layoutParams = itemView.layoutParams
            val id = if (plansItem?.selected == true) {
                R.drawable.pricing_bg_selected
            } else {
                R.drawable.pricing_bg
            }
            itemView.setBackgroundResource(id)
            itemView.layoutParams = layoutParams
            itemView.setPadding(50, 80, 50, 80)
        }
    }
}