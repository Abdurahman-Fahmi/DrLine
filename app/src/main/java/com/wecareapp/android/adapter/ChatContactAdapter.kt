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
import com.wecareapp.android.model.InboxItem
import kotlinx.android.synthetic.main.list_item_clinic.view.ivImage
import kotlinx.android.synthetic.main.list_item_clinic.view.tvClinicLocation
import kotlinx.android.synthetic.main.list_item_clinic.view.tvClinicName
import kotlinx.android.synthetic.main.list_item_clinic.view.viewOnline
import kotlinx.android.synthetic.main.list_item_contacts.view.*

class ChatContactAdapter(
    private val list: MutableList<InboxItem?>,
    private val listener: Listener?
) :
    RecyclerView.Adapter<ChatContactAdapter.ViewHolder>() {

    interface Listener {
        fun onItemSelected(clinic: InboxItem?, adapterPosition: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_contacts, parent, false)
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
        val tvCount: AppCompatTextView = view.tvCount
        val tvClinicLocation: AppCompatTextView = view.tvClinicLocation
        val ivImage: AppCompatImageView = view.ivImage
        private val viewOnline: View = view.viewOnline

        fun bind(inboxItem: InboxItem?) {
            tvClinicName.text = inboxItem?.userName
            if (inboxItem?.lastMessage?.contains(".jpg") == true)
                tvClinicLocation.text = itemView.context.getString(R.string.image)
            else
                tvClinicLocation.text = inboxItem?.lastMessage
            itemView.setOnClickListener {
                listener?.onItemSelected(inboxItem, adapterPosition)
            }

            val n = inboxItem?.unreadCount ?: "0"
            if (n.isEmpty() || n.contentEquals("0")) {
                tvCount.visibility = View.GONE
            } else {
                tvCount.visibility = View.VISIBLE
                tvCount.text = n
            }

            if (inboxItem?.isOnline?.contentEquals("online") == true)
                viewOnline.visibility = View.VISIBLE
            else
                viewOnline.visibility = View.GONE

            val requestOptions: RequestOptions = RequestOptions()
                .placeholder(R.drawable.ic_clinic_doctor)
                .error(R.drawable.ic_clinic_doctor)
                .fitCenter()

            val img = inboxItem?.profileImage
            Glide.with(itemView.context)
                .load(img)
                .fitCenter()
                .apply(requestOptions)
                .into(ivImage)

            when {
                inboxItem?.customerType?.contentEquals("1") == true -> {
                    tvClinicLocation.text = itemView.context.getString(R.string.patient)
                }
                inboxItem?.customerType?.contentEquals("2") == true -> {
                    tvClinicLocation.text = itemView.context.getString(R.string.consultant)
                }
                inboxItem?.customerType?.contentEquals("3") == true -> {
                    tvClinicLocation.text = itemView.context.getString(R.string.clinic)
                }
            }
        }
    }
}