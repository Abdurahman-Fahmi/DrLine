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
import com.wecareapp.android.model.Clinic
import kotlinx.android.synthetic.main.list_item_clinic.view.*

class ClinicListAdapter(
    private val list: MutableList<Clinic?>,
    private val listener: Listener?
) :
    RecyclerView.Adapter<ClinicListAdapter.ViewHolder>() {

    interface Listener {
        fun onItemSelected(clinic: Clinic?, adapterPosition: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_clinic, parent, false)
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
        private val appCompatRatingBar: AppCompatRatingBar = view.appCompatRatingBar
        private val viewOnline: View = view.viewOnline

        //        val ivAppointment: AppCompatImageView = view.ivAppointment
        val ivImage: AppCompatImageView = view.ivImage
        fun bind(clinic: Clinic?) {
            tvClinicName.text = clinic?.userName
            tvClinicLocation.text = clinic?.address
            appCompatRatingBar.visibility = View.GONE
            appCompatRatingBar.rating = clinic?.rating?.toFloat() ?: 0F
            itemView.setOnClickListener {
                listener?.onItemSelected(clinic, adapterPosition)
            }

            if (clinic?.isOnline?.contentEquals("online") == true)
                viewOnline.visibility = View.VISIBLE
            else
                viewOnline.visibility = View.GONE

            val requestOptions: RequestOptions = RequestOptions()
                .placeholder(R.drawable.ic_clinic_doctor)
                .error(R.drawable.ic_clinic_doctor)
                .fitCenter()

            val img = clinic?.image
            Glide.with(itemView.context)
                .load(img)
                .fitCenter()
                .apply(requestOptions)
                .into(ivImage)
        }
    }
}