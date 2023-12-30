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
import com.wecareapp.android.model.Doctor
import kotlinx.android.synthetic.main.list_item_clinic_doctor.view.*

class DoctorListAdapter(
    private val list: MutableList<Doctor?>,
    private val listener: Listener?,
    private val patient: Boolean
) :
    RecyclerView.Adapter<DoctorListAdapter.ViewHolder>() {

    interface Listener {
        fun onItemSelected(doctor: Doctor?, adapterPosition: Int)
        fun bookAppointment(doctor: Doctor?, adapterPosition: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_clinic_doctor, parent, false)
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
        val ivImage: AppCompatImageView = view.ivImage
        private val appCompatRatingBar: AppCompatRatingBar = view.appCompatRatingBar
        private val ivAppointment: AppCompatImageView = view.ivAppointment
        private val viewOnline: View = view.viewOnline

        fun bind(doctor: Doctor?) {
            tvClinicName.text = doctor?.userName
            tvClinicLocation.text = doctor?.address
            appCompatRatingBar.rating = doctor?.rating?.toFloat() ?: 0F

            if (doctor?.isOnline?.contentEquals("online") == true)
                viewOnline.visibility = View.VISIBLE
            else
                viewOnline.visibility = View.GONE


            ivAppointment.setOnClickListener {
                listener?.bookAppointment(doctor, adapterPosition)
            }

            ivAppointment.visibility = if (patient) View.VISIBLE else View.GONE

            val requestOptions: RequestOptions = RequestOptions()
                .placeholder(R.drawable.ic_clinic_doctor)
                .error(R.drawable.ic_clinic_doctor)
                .fitCenter()

            val img = doctor?.image
            Glide.with(itemView.context)
                .load(img)
                .fitCenter()
                .apply(requestOptions)
                .into(ivImage)

            itemView.setOnClickListener {
                listener?.onItemSelected(doctor, adapterPosition)
            }
        }
    }
}