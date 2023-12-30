package com.wecareapp.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.wecareapp.android.R
import com.wecareapp.android.model.Category
import kotlinx.android.synthetic.main.list_item_clinic_services.view.*

class ServiceCategoryListAdapter(
    private val list: MutableList<Category?>,
    private val listener: Listener?
) :
    RecyclerView.Adapter<ServiceCategoryListAdapter.ViewHolder>() {

    interface Listener {
        fun onItemSelected(category: Category?, adapterPosition: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_clinic_services, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvClinicName: AppCompatTextView = view.tvCategoryName
        val ivImage: AppCompatImageView = view.ivServiceIcon
        private val ivServiceBackground: AppCompatImageView = view.ivServiceBackground

        fun bind(category: Category?) {

            var id = R.drawable.ic_service_bg
            val layoutParams = ivServiceBackground.layoutParams
            if (category?.selected == true) {
                layoutParams.width =
                    itemView.context.resources.getDimensionPixelSize(R.dimen._70sdp)
                layoutParams.height =
                    itemView.context.resources.getDimensionPixelSize(R.dimen._80sdp)
                id = R.drawable.ic_service_bg_2
            } else {
                layoutParams.width =
                    itemView.context.resources.getDimensionPixelSize(R.dimen._60sdp)
                layoutParams.height =
                    itemView.context.resources.getDimensionPixelSize(R.dimen._70sdp)
            }
            ivServiceBackground.layoutParams = layoutParams
            ivServiceBackground.setImageResource(id)

            itemView.setOnClickListener {
                listener?.onItemSelected(category, adapterPosition)
            }

            val requestOptions: RequestOptions = RequestOptions()
                .fitCenter()

            if (category == null) {
                Glide.with(itemView.context)
                    .load(R.drawable.ic_service_add_plus)
                    .fitCenter()
                    .apply(requestOptions)
                    .into(ivImage)

                tvClinicName.text = itemView.context.getString(R.string.add_new_service)
                return
            }

            tvClinicName.text = category.categoryName
            val img = category.icon
            Glide.with(itemView.context)
                .load(img)
                .fitCenter()
                .apply(requestOptions)
                .into(ivImage)
        }
    }
}