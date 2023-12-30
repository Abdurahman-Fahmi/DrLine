package com.wecareapp.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatRatingBar
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.wecareapp.android.R
import com.wecareapp.android.model.Rating
import kotlinx.android.synthetic.main.list_item_reviews.view.*
import java.text.SimpleDateFormat

class ReviewsListAdapter(
    private val list: MutableList<Rating?>,
    private val listener: Listener?
) :
    RecyclerView.Adapter<ReviewsListAdapter.ViewHolder>() {

    interface Listener {
        fun onItemSelected(clinic: Rating?, adapterPosition: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_reviews, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvClinicName: AppCompatTextView = view.tvName
        val tvClinicLocation: AppCompatTextView = view.tvClinicLocation
        val tvDate: AppCompatTextView = view.tvDate
        private val ivImage: AppCompatImageView = view.ivImage
        private val appCompatRatingBar: AppCompatRatingBar = view.ratingBar
        fun bind(clinic: Rating?) {
            tvClinicName.text = clinic?.userName
            tvClinicLocation.text = clinic?.userName

            try {
                val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                val format2 = SimpleDateFormat("dd-MM-yyyy")
                val d = format.parse(clinic?.dateTime)
                tvDate.text = format2.format(d)
            } catch (e: Exception) {
                tvDate.text = clinic?.dateTime
            }

            itemView.setOnClickListener {
                listener?.onItemSelected(clinic, adapterPosition)
            }

            val requestOptions: RequestOptions = RequestOptions()
                .placeholder(R.drawable.ic_clinic_doctor)
                .error(R.drawable.ic_clinic_doctor)
                .fitCenter()

            val img = clinic?.profileImage
            Glide.with(itemView.context)
                .load(img)
                .fitCenter()
                .apply(requestOptions)
                .into(ivImage)

            appCompatRatingBar.rating = clinic?.rating?.toFloat() ?: 0F
        }
    }
}