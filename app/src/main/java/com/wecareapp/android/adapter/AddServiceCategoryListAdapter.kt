package com.wecareapp.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.wecareapp.android.R
import com.wecareapp.android.model.Category
import kotlinx.android.synthetic.main.list_item_clinic_services.view.*

class AddServiceCategoryListAdapter(
    private val list: MutableList<Category?>,
    private val listener: Listener?
) :
    RecyclerView.Adapter<AddServiceCategoryListAdapter.ViewHolder>() {

    interface Listener {
        fun onItemSelected(category: Category?, adapterPosition: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_add_service_category, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImage: AppCompatImageView = view.ivServiceIcon
        fun bind(category: Category?) {

            itemView.setOnClickListener {
                listener?.onItemSelected(category, adapterPosition)
            }

            val requestOptions: RequestOptions = RequestOptions()
//                .placeholder(R.drawable.ic_service_add_plus)
//                .error(R.drawable.ic_service_add_plus)
                .fitCenter()

            val img = category?.icon
            Glide.with(itemView.context)
                .load(img)
                .fitCenter()
                .apply(requestOptions)
                .into(ivImage)
        }
    }
}