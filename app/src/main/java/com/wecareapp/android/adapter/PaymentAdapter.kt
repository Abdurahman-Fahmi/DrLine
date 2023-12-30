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
import kotlinx.android.synthetic.main.list_item_payments.view.*

class PaymentAdapter(
    private val list: MutableList<Appointment?>,
    private val listener: Listener?
) :
    RecyclerView.Adapter<PaymentAdapter.ViewHolder>() {

    var currency: String = "SAR"

    interface Listener {
        fun onItemSelected(clinic: Appointment?, adapterPosition: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_payments, parent, false)
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
        val tvPaid: AppCompatTextView = view.tvPaid
        private val ivImage: AppCompatImageView = view.ivImage
        fun bind(clinic: Appointment?) {
            tvClinicName.text = clinic?.patientName
            tvPaid.text = currency + " " + clinic?.slotPrice
            itemView.setOnClickListener {
                listener?.onItemSelected(clinic, adapterPosition)
            }

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