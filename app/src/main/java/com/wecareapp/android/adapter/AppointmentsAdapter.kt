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
import com.wecareapp.android.model.Appointment
import kotlinx.android.synthetic.main.list_item_appointments.view.*

class AppointmentsAdapter(
    private val list: MutableList<Appointment?>,
    private val listener: Listener?
) :
    RecyclerView.Adapter<AppointmentsAdapter.ViewHolder>() {

    private var userCustomerType: Int? = 0

    interface Listener {
        fun onItemSelected(clinic: Appointment?, adapterPosition: Int)
        fun addReminder(appointment: Appointment?, adapterPosition: Int)
        fun endReview(appointment: Appointment?, adapterPosition: Int)
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

    fun setUserType(userCustomerType: Int?) {
        this.userCustomerType = userCustomerType
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvClinicName: AppCompatTextView = view.tvClinicName
        val tvMessage: AppCompatTextView = view.tvMessage
        val tvReview: AppCompatTextView = view.tvReview
        private val ivImage: AppCompatImageView = view.ivImage
        private val ivEndReview: View = view.ivEndReview
        private val viewAddReminder: View = view.viewAddReminder

        fun bind(appointment: Appointment?) {
            tvClinicName.text = appointment?.name
            tvMessage.text = appointment?.text
            ivEndReview.setOnClickListener {
                listener?.endReview(appointment, adapterPosition)
            }
            itemView.setOnClickListener {
                listener?.onItemSelected(appointment, adapterPosition)
            }
            viewAddReminder.setOnClickListener {
                listener?.addReminder(appointment, adapterPosition)
            }

            if (appointment?.reminderStatus?.contentEquals("show") == true) {
                viewAddReminder.visibility = View.VISIBLE
            } else {
                viewAddReminder.visibility = View.GONE
            }

            if (userCustomerType == 1) {
                if (appointment?.userReviewStatus?.contentEquals("0") == true) {
                    tvReview.text = itemView.context.getString(R.string.end_amp_review)
                } else {
                    tvReview.text = itemView.context.getString(R.string.done)
                    ivEndReview.setOnClickListener(null)
                }
            } else {
                if (appointment?.doctorReviewStatus?.contentEquals("0") == true) {
                    tvReview.text = itemView.context.getString(R.string.end_amp_review)
                } else {
                    tvReview.text = itemView.context.getString(R.string.done)
                    ivEndReview.setOnClickListener(null)
                }
            }

            val requestOptions: RequestOptions = RequestOptions()
                .placeholder(R.drawable.ic_clinic_doctor)
                .error(R.drawable.ic_clinic_doctor)
                .fitCenter()

            val img = appointment?.image
            Glide.with(itemView.context)
                .load(img)
                .fitCenter()
                .apply(requestOptions)
                .into(ivImage)
        }
    }
}