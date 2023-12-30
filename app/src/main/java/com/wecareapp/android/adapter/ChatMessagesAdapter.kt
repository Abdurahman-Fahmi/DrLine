package com.wecareapp.android.adapter

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bolts.Task.cancelled
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.wecareapp.android.R
import com.wecareapp.android.model.Conversation
import java.io.File

class ChatMessagesAdapter(
    private var context: Context,
    private var contactsModelArrayList: List<Conversation?>,
    private var listener: Listener,
    private var userTypeChat: String,
    recyclerView: RecyclerView
) : RecyclerView.Adapter<ChatMessagesAdapter.MyHolder>() {

    init {
        if (recyclerView.layoutManager is LinearLayoutManager) {
            val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    try {
                        super.onScrolled(recyclerView, dx, dy)
                        totalItemCount = linearLayoutManager.itemCount
                        lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition()
                        if (loading) return
                        if (!loading && totalItemCount <= lastVisibleItem + visibleThreshold) {
                            if (onLoadMoreListener != null) {
                                onLoadMoreListener!!.onLoadMore()
                            }
                            loading = true
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
        }
    }

    private var callerImage: String = ""
    private var senderImage: String = ""
    private var userCustomerType: Int? = 0
    private var baseUrl: String? = ""
    private var myUserId: String? = null

    private val visibleThreshold = 5
    private var lastVisibleItem = 0
    private var totalItemCount: Int = 0
    private var loading = false
    private var onLoadMoreListener: OnLoadMoreListener? = null

    fun setLoaded() {
        loading = false
    }

    interface OnLoadMoreListener {
        fun onLoadMore()
    }

    fun setOnLoadMoreListener(onLoadMoreListener: OnLoadMoreListener?) {
        this.onLoadMoreListener = onLoadMoreListener
    }

    interface Listener {
        fun onActionAccept(contactsModel: Conversation?)
        fun onActionReject(contactsModel: Conversation?)
        fun onActionPay(contactsModel: Conversation?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return if (viewType == ChatType.SENDER.ordinal) {
            MyHolder(
                LayoutInflater.from(context).inflate(R.layout.chat_message_right, parent, false)
            )
        } else {
            MyHolder(
                LayoutInflater.from(context).inflate(R.layout.chat_message_left, parent, false)
            )
        }
    }

    override fun getItemCount(): Int {
        return contactsModelArrayList.size
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val conversation = contactsModelArrayList[position]

        holder.bind(conversation)

    }

    override fun getItemViewType(position: Int): Int {
        return if (contactsModelArrayList[position]?.sendingType?.contains("sent") == true ||
            contactsModelArrayList[position]?.myId?.contentEquals(myUserId ?: "") == true
        )
            ChatType.SENDER.ordinal
        else
            ChatType.RECEIVER.ordinal
    }

    fun setMyUserId(userId: String?, userCustomerType: Int?) {
        myUserId = userId
        this.userCustomerType = userCustomerType
    }

    fun setBaseUrl(baseUrl: String?) {
        this.baseUrl = baseUrl
    }

    fun setImages(senderImage: String, callerImage: String) {
        this.senderImage = senderImage
        this.callerImage = callerImage
    }

    enum class ChatType {
        SENDER, RECEIVER
    }

    inner class MyHolder(view: View) : RecyclerView.ViewHolder(view) {

        //        val nameContact = view.txt_name
//        val tvAmount = view.tvAmount
//        val imageContact = view.iv_contacts
        private val ivDelete: TextView = view.findViewById(R.id.ivDelete)
        private val ivOk: TextView = view.findViewById(R.id.ivOk)
        private val clHire: View = view.findViewById(R.id.clHire)
        private val clMsg: View = view.findViewById(R.id.clMsg)
        private val tvMessage: TextView? = view.findViewById(R.id.tvMessage)
        private val tvTime: TextView? = view.findViewById(R.id.tvTime)
        private val tvDescription: TextView? = view.findViewById(R.id.tv_description)
        private val tvAmount: TextView? = view.findViewById(R.id.tvAmount)
        private val tvAmount2: TextView? = view.findViewById(R.id.tvAmount2)
        val ivImage: ImageView? = view.findViewById(R.id.ivImage)
        val ivContactsImage: ImageView? = view.findViewById(R.id.iv_contacts)

        //        val ivOk = view.ivOk
//        val ivDelete = view.ivDelete
//        val tvStatus = view.tvStatus
//        val tvPay = view.tvPay
        fun bind(conversation: Conversation?) {

            val requestOptions: RequestOptions = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(R.drawable.ic_clinic_doctor)
                .error(R.drawable.ic_clinic_doctor)
                .fitCenter()
            val image =
                if (contactsModelArrayList[position]?.sendingType?.contains("sent") == true ||
                    contactsModelArrayList[position]?.myId?.contentEquals(myUserId ?: "") == true
                ) senderImage else callerImage

            Glide.with(ivContactsImage?.context!!)
                .load(image)
                .fitCenter()
                .apply(requestOptions)
                .into(ivContactsImage)

            tvTime?.text = conversation?.dateOfMessage
            tvTime?.visibility = View.VISIBLE

            if (conversation?.type?.contentEquals("message") == true
                || conversation?.type?.contentEquals("image") == true
            ) {
                if (conversation.type.contentEquals("image")) {
                    val requestOptions: RequestOptions = RequestOptions()
                        .placeholder(R.drawable.ic_placeholder_1)
                        .error(R.drawable.ic_placeholder_1)
                        .fitCenter()

                    val url = if (TextUtils.isEmpty(conversation.dateOfMessage))
                        conversation.message ?: ""
                    else
                        baseUrl + conversation.message

                    if (TextUtils.isEmpty(conversation.dateOfMessage)) {
                        Glide.with(ivImage?.context!!)
                            .load(Uri.fromFile(File(url)))
                            .fitCenter()
                            .apply(requestOptions)
                            .into(ivImage)
                    } else {
                        Glide.with(ivImage?.context!!)
                            .load(url)
                            .fitCenter()
                            .apply(requestOptions)
                            .into(ivImage)
                    }

                    ivImage.visibility = View.VISIBLE
                    tvMessage?.visibility = View.GONE
                } else {
                    ivImage?.visibility = View.GONE
                    tvMessage?.visibility = View.VISIBLE
                }
                clMsg.visibility = View.VISIBLE
                clHire.visibility = View.GONE
                tvMessage?.text = conversation.message
//            holder.nameContact.visibility = View.VISIBLE
            } else {
                clHire.visibility = View.VISIBLE
                clMsg.visibility = View.GONE
                tvDescription?.text = context.getString(R.string.appointment_request)
                tvAmount?.text =
                    conversation?.bookDate + ", \n" + conversation?.bookTimeStart + "-" + conversation?.bookTimeEnd

                if (conversation?.slotPaymentStatus?.contentEquals("0") == true) {
                    tvAmount2?.text =
                        context.getString(R.string.pay) + " " + conversation.currency + " " + conversation.slotPrice
                } else {
                    tvAmount2?.text =
                        context.getString(R.string.paid) + " " + conversation?.currency + " " + conversation?.slotPrice
                }

                ivDelete.visibility = View.VISIBLE
                if (userCustomerType == 1) {
                    ivDelete.text = context.getString(R.string.cancel)
                    if (conversation?.slotRequestStatus?.contentEquals("pending") == true)
                        ivOk.text = context.getString(R.string.pending)
                    else if (conversation?.slotRequestStatus?.contentEquals("decline") == true) {
                        ivDelete.text = context.getString(R.string.cancelled)
                        ivOk.visibility = View.GONE
                        ivDelete.visibility = View.GONE
                    } else {
                        ivDelete.visibility = View.VISIBLE
                        ivOk.isEnabled = true
                        ivOk.isClickable = true
                        if (conversation?.slotPaymentStatus?.contentEquals("0") == true) {
                            ivOk.text = context.getString(R.string.pay_now)
                        } else {
                            ivOk.text = context.getString(R.string.paid)
                        }
                    }
                } else {
                    ivDelete.text = context.getString(R.string.decline)
                    if (conversation?.slotRequestStatus?.contentEquals("pending") == true) {
                        ivOk.text = context.getString(R.string.accept)
                        ivOk.isEnabled = true
                        ivOk.isClickable = true
                    } else if (conversation?.slotRequestStatus?.contentEquals("decline") == true) {
                        ivDelete.text = context.getString(R.string.cancelled)
                        ivOk.visibility = View.GONE
                        ivDelete.visibility = View.GONE
                        ivDelete.isEnabled = false
                        ivDelete.isClickable = false
                    } else {
                        ivDelete.isEnabled = false
                        ivDelete.isClickable = false
                        ivOk.isEnabled = false
                        ivOk.isClickable = false
                        ivDelete.visibility = View.GONE
                        if (conversation?.slotPaymentStatus?.contentEquals("0") == true)
                            ivOk.text = context.getString(R.string.accepted)
                        else
                            ivOk.text = context.getString(R.string.paid)
                    }
                }

                ivOk.setOnClickListener {
                    if (userCustomerType == 1) {
                        if (conversation?.slotRequestStatus?.contentEquals("pending") == true) {
//                            Toast.makeText(context, context.getString(R.string.pay))
                        } else {
                            listener.onActionPay(conversation)
                        }
                    } else {
                        listener.onActionAccept(conversation)
                    }
                }

                ivDelete.setOnClickListener {
                    listener.onActionReject(conversation)
                }

//            holder.nameContact.visibility = View.GONE
//            holder.tvAmount.text =
//                context.getString(R.string.currency) + " " + contactsModel?.hireAmount.toString()
            }
        }
    }
}