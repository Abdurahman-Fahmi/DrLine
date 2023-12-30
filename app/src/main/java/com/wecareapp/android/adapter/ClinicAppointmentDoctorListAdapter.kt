package com.wecareapp.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.wecareapp.android.R
import com.wecareapp.android.model.Doctor
import kotlinx.android.synthetic.main.list_item_clinic_appointment_doctor.view.*

class ClinicAppointmentDoctorListAdapter(
    private val list: MutableList<Doctor?>,
    private val listener: Listener?
) :
    RecyclerView.Adapter<ClinicAppointmentDoctorListAdapter.ViewHolder>() {

    interface Listener {
        fun onItemSelected(category: Doctor?, adapterPosition: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_clinic_appointment_doctor, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImage: ImageView = view.ivServiceIcon
        fun bind(category: Doctor?) {

            itemView.setOnClickListener {
                listener?.onItemSelected(category, adapterPosition)
            }

            val requestOptions: RequestOptions = RequestOptions()
                .placeholder(R.drawable.ic_service_add_plus)
                .error(R.drawable.ic_service_add_plus)
                .fitCenter()

            val img = category?.image
            Glide.with(itemView.context)
                .load(img)
                .fitCenter()
                .apply(requestOptions)
                .into(ivImage)
        }
    }
}