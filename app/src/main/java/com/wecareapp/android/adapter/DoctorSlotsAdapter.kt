package com.wecareapp.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.wecareapp.android.R
import com.wecareapp.android.model.DoctorSlotDataItem
import kotlinx.android.synthetic.main.list_item_appointments.view.*

class DoctorSlotsAdapter(
    private val list: MutableList<DoctorSlotDataItem?>,
    private val listener: Listener?
) :
    RecyclerView.Adapter<DoctorSlotsAdapter.ViewHolder>() {

    interface Listener {
        fun onItemSelected(doctor: DoctorSlotDataItem?, adapterPosition: Int)
        fun onClickedEndReview(doctor: DoctorSlotDataItem?, adapterPosition: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_appointments, parent, false)
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
        val tvClinicLocation: AppCompatTextView = view.tvMessage
        val ivEndReview: AppCompatImageView = view.ivEndReview
        val ivImage: AppCompatImageView = view.ivImage
        fun bind(doctor: DoctorSlotDataItem?) {
            try {
                itemView.setOnClickListener {
                    listener?.onItemSelected(doctor, adapterPosition)
                }

                ivEndReview.setOnClickListener {
                    listener?.onClickedEndReview(doctor, adapterPosition)
                }
                tvClinicName.text = doctor?.userName
                tvClinicLocation.text = doctor?.userName
//            appCompatRatingBar.rating = doctor?.rating?.toFloat() ?: 0F

                val requestOptions: RequestOptions = RequestOptions()
                    .placeholder(R.drawable.ic_clinic_doctor)
                    .error(R.drawable.ic_clinic_doctor)
                    .fitCenter()

//            val img = doctor?.image
//            Glide.with(itemView.context)
//                .load(img)
//                .fitCenter()
//                .apply(requestOptions)
//                .into(ivImage)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}