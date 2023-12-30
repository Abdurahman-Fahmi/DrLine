package com.wecareapp.android.ui.activity

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.wecareapp.android.R
import com.wecareapp.android.adapter.ChatMessagesAdapter
import com.wecareapp.android.model.Conversation
import com.wecareapp.android.model.Doctor
import com.wecareapp.android.model.InboxItem
import com.wecareapp.android.model.SlotResponse
import com.wecareapp.android.utilities.FileUtils2
import com.wecareapp.android.utilities.Utility
import com.wecareapp.android.webservices.viewmodels.PatientViewModel
import kotlinx.android.synthetic.main.activity_chat_patient.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.File
import java.util.*


class ChatActivity : BaseActivity(), ChatMessagesAdapter.Listener,
    ChatMessagesAdapter.OnLoadMoreListener {
    private var send: Boolean = false
    private var bookStatus: String = "0"
    private var selectedOption: String = ""
    private var callerName: String = ""
    private var youUserFirebaseToken: String? = null
    private var agoraToken: String? = null
    private var channelName: String? = null
    private var chatId: String? = null
    private var doctor: Doctor? = null
    private var contact: InboxItem? = null
    private var senderId: String? = null
    private var receiverId: String? = null
    private lateinit var userTypeChat: String
    private var list = arrayListOf<Conversation?>()
    private var handler: Handler? = Handler(Looper.getMainLooper())
    private lateinit var viewModel: PatientViewModel
    private lateinit var adapter: ChatMessagesAdapter
    var senderImage = ""
    var senderName = ""
    var callerImage = ""

    private var dialog: Dialog? = null
    private lateinit var selectedImageUri: Uri
    private lateinit var selectedDocumentUri: Uri
    private var selectedDocumentPath: String? = null
    private var selectedImagePath: String? = null
    private var imageBitmap: Bitmap? = null
    private var isImageSelected = false

    private var pageStart: Int = 0
    private val PAGE_COUNT = 10
    private var noList = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_patient)

        if (!isLogged)
            openLoginActivity()

        viewModel = ViewModelProvider(this).get(PatientViewModel::class.java)

        contact =
            if (intent.extras?.getSerializable("contact") == null) null else intent.extras?.getSerializable(
                "contact"
            ) as InboxItem?

        doctor =
            if (intent.extras?.getSerializable("doctor") == null) null else intent.extras?.getSerializable(
                "doctor"
            ) as Doctor?

//        actorProfile = intent.extras?.getSerializable("actorProfile") as ActorProfile?
//        contact = intent.extras?.getSerializable("contact") as InboxItem?
//        contactCustomer = intent.extras?.getSerializable("contactCustomer") as DataItem?

        if (userTypeStr.contentEquals("patient")) {
            senderId = user?.contactId
            callerImage = user?.profileImage.toString()
            callerName = user?.userName.toString()

            tvAudioCallPrice.visibility = View.VISIBLE
            tvVideoCallPrice.visibility = View.VISIBLE
        } else {
            senderId = user?.contactId
            callerImage = user?.profileImage.toString()
            callerName = user?.userName.toString()

            tvAudioCallPrice.visibility = View.GONE
            tvVideoCallPrice.visibility = View.GONE
        }

        if (contact != null) {
            receiverId = contact?.youId ?: ""
            senderImage = contact?.profileImage ?: ""
            tvClinicName.text = contact?.userName
            senderName = contact?.userName ?: ""
//                tvClinicLocation.text =contact?.userName
        } else if (doctor != null) {
            receiverId = doctor?.doctorId ?: ""
            senderImage = doctor?.image ?: ""
            tvClinicName.text = doctor?.userName
            tvClinicLocation.text = doctor?.address
            senderName = doctor?.userName ?: ""
        }

        val requestOptions: RequestOptions = RequestOptions()
            .placeholder(R.drawable.ic_clinic_doctor)
            .error(R.drawable.ic_clinic_doctor)
            .fitCenter()

        Glide.with(ivProfile.context)
            .load(senderImage)
            .fitCenter()
            .apply(requestOptions)
            .into(ivProfile)

        val layoutManager = LinearLayoutManager(this)
//        layoutManager.isAutoMeasureEnabled = true
//        layoutManager.reverseLayout = true
//        layoutManager.stackFromEnd = true
        recyclerView.layoutManager = layoutManager
        adapter = ChatMessagesAdapter(recyclerView.context, list, this, userTypeStr, recyclerView)
        //        binding.recyclerView.setLayoutAnimation(animation);
        adapter.setOnLoadMoreListener(this)
        recyclerView.adapter = adapter
        adapter.setMyUserId(userId, userCustomerType)
        adapter.setImages(callerImage, senderImage)

        swipeRefresh.setOnRefreshListener {
            loadMessages()
        }
        ivBack.setOnClickListener {
            onBackPressed()
        }
        ivSend.setOnClickListener {
            sendMessage("text")
        }

        ivGallery.setOnClickListener {
            sendImage()
        }

        ivVideoCall.setOnClickListener {
            selectedOption = "video"
            calling()
        }

        ivAudioCall.setOnClickListener {
            selectedOption = "audio"
            calling()
        }

        loadMessages()
        autoRefresh()
    }

    override fun onLoadMore() {
        try {
            Handler().post {
                try {
                    if (noList) return@post
                    var f = false
                    if (list != null) {
                        for (g in list) {
                            if (g == null) f = true
                        }
                    }
                    if (!f) {
                        if (list != null) {
//                            list.add(null)
//                            adapter.notifyItemInserted(list.size - 1)
                        }
                        loadMessages()
                    }

//            new Handler().postDelayed(() ->
//                    loadFeeds(dimensionIds), 3000);
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun sendImage() {
        Dexter.withActivity(this)
            .withPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        openDialog()
                    }
                    if (report.isAnyPermissionPermanentlyDenied) {
                        showSettingsDialog()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).check()
    }

    private fun openDialog() {
        dialog = Dialog(this)
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog!!.setContentView(R.layout.layout_popup_select_image)
        dialog!!.setCancelable(true)

        val btnCamera =
            dialog!!.findViewById<View>(R.id.btn_camera) as Button
        val btnGallery =
            dialog!!.findViewById<View>(R.id.btn_gallery) as Button


        btnCamera.setOnClickListener {
            dialog!!.dismiss()
            selectImage()
        }

        btnGallery.setOnClickListener {
            dialog!!.dismiss()
            openGallery()
        }

        dialog!!.show()
    }

    private fun selectImage() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_CAMERA)
    }

    private fun openGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, getString(R.string.select_picture)), REQUEST_GALLERY
        )
    }

    private fun calling() {
        if (bookStatus.contentEquals("0")) {
            Snackbar.make(
                this.findViewById(android.R.id.content),
                getString(R.string.err_msg_no_appointment_on_this_time),
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }

        if (!(checkSelfPermission(
                Manifest.permission.RECORD_AUDIO,
                PERMISSION_REQ_ID_RECORD_AUDIO
            ) && checkSelfPermission(
                Manifest.permission.CAMERA,
                PERMISSION_REQ_ID_CAMERA
            ))
        ) {
            return
        }

        val jsonObject = JSONObject()
        jsonObject.put("action", "sendCallNotification")
        jsonObject.put("user_id", userId)
        jsonObject.put("customer_type", userId)
        Utility.showLog("jsonObject", "" + jsonObject)

        val request = MultipartBody.Builder()

        request.setType(MultipartBody.FORM)
            .addFormDataPart("action", "sendCallNotification")
            .addFormDataPart("customer_type", userCustomerType.toString() ?: "")
            .addFormDataPart("user_id", userId ?: "")
            .addFormDataPart("User_name", user?.userName ?: "")
            .addFormDataPart("user_name", user?.userName ?: "")

        request.addFormDataPart("body", "call")
        request.addFormDataPart("caller_name", callerName)
        request.addFormDataPart("callee_name", user?.userName ?: "")
        request.addFormDataPart("channelName", channelName ?: "")
        request.addFormDataPart("agoraToken", agoraToken ?: "")
        request.addFormDataPart("tokens", agoraToken ?: "")
        request.addFormDataPart("senderName", senderName)
        request.addFormDataPart("senderAva", senderImage)
        request.addFormDataPart("channelId", channelName ?: "")
        request.addFormDataPart("callerName", callerName)
        request.addFormDataPart("callerAva", callerImage)
        request.addFormDataPart("callerId", senderId ?: "")
        request.addFormDataPart("senderId", receiverId ?: "")
        request.addFormDataPart("data", "data")
        request.addFormDataPart("content_available", "true")
        if (checkingAccess()) {
            progressBar.visibility = View.VISIBLE
            if (selectedOption.contentEquals("video")) {
                request.addFormDataPart("title", "video_call")
                request.addFormDataPart("nType", "videoCall")
                request.addFormDataPart("type", "video_call")
            } else {
                request.addFormDataPart("title", "audio_call")
                request.addFormDataPart("type", "audio_call")
                request.addFormDataPart("nType", "voiceCall")
            }

            val requestBody = request.build()

            viewModel.commonRequest(requestBody).observe(this, Observer {
                Utility.hideLoadingDialog()
                progressBar.visibility = View.GONE
                if (it == null)
                    return@Observer
                if (it.status == "200") {
                    if (selectedOption.contentEquals("video")) {
                        startActivity(Intent(this, VideoCallActivity::class.java).apply {
                            putExtra("chat", "chat")
                            putExtra("doctor", doctor)
                            putExtra("contact", contact)
                            putExtra("channelName", channelName)
                            putExtra("agoraToken", agoraToken)
                            putExtra("chatId", chatId)
                            putExtra("caller_name", callerName)
                            putExtra("callee_name", user?.userName)
                            putExtra("type", "video_call")
                            putExtra("callerId", senderId ?: "")
                            putExtra("senderId", receiverId ?: "")
                        })
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    } else {
                        startActivity(Intent(this, VoiceCallActivity::class.java).apply {
                            putExtra("chat", "chat")
                            putExtra("doctor", doctor)
                            putExtra("contact", contact)
                            putExtra("channelName", channelName)
                            putExtra("agoraToken", agoraToken)
                            putExtra("chatId", chatId)
                            putExtra("caller_name", callerName)
                            putExtra("callee_name", user?.userName)
                            putExtra("type", "audio_call")
                            putExtra("callerId", senderId ?: "")
                            putExtra("senderId", receiverId ?: "")
                        })
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    }
                } else {
                    Snackbar.make(
                        this.findViewById(android.R.id.content),
                        it.message ?: getString(R.string.err_msg_oops_something_went_to_wrong),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            })
        }

    }

    private val runnable = Runnable {
        loadMessages()
        autoRefresh()
    }

    private fun autoRefresh() {
        handler?.postDelayed(runnable, 10 * 1000)
    }

    private fun loadMessages() {
        swipeRefresh.isRefreshing = true
//        Utility.showLoadingDialog(this, getString(R.string.loading), false)
        val jsonObject = JSONObject()
        jsonObject.put("action", "loadConversation")
        jsonObject.put("user_id", "" + senderId)
        jsonObject.put("start", "" + pageStart)
        jsonObject.put("receiver_id", "" + receiverId)
        jsonObject.put("my_id", "" + senderId)
        jsonObject.put("you_id", "" + receiverId)

        Utility.showLog("jsonObject", "" + jsonObject)
        val request = MultipartBody.Builder()
        request.setType(MultipartBody.FORM)
            .addFormDataPart("action", "loadConversation")
            .addFormDataPart("user_id", senderId ?: "")
            .addFormDataPart("receiverId", receiverId ?: "")
            .addFormDataPart("my_id", senderId ?: "")
            .addFormDataPart("you_id", receiverId ?: "")
            .addFormDataPart("customer_type", userCustomerType.toString() ?: "1")
            .addFormDataPart("start", pageStart.toString())

//        if (!TextUtils.isEmpty(searchKey)) {
//            searchKey?.let { request.addFormDataPart("search", it) }
//        }

        val requestBody = request.build()

        viewModel.loadMessages(requestBody).observe(this, Observer {
            try {
                Utility.hideLoadingDialog()
                swipeRefresh.isRefreshing = false
                if (it.status == "200") {
                    adapter.setOnLoadMoreListener(this)
                    adapter.setLoaded()
                    if (it.conversation == null)
                        return@Observer
                    if (list.isNotEmpty()) {
                        if (send) {
                            list.removeAt(list.size - 1)
                            send = false
                        }
                        for (item in it.conversation) {
                            if (item?.id?.let { it1 -> list[list.size - 1]?.id?.contains(it1) } == true) { //list[0]?.id?.contains(it1)
                                return@Observer
                            } else {
                                break
                            }
                        }
                    }
                    if (it.conversation.isEmpty()) {
                        noList = true
                        adapter.setLoaded()
                    } else {
                    }
                    adapter.setBaseUrl(it.baseUrl)

                    val s: Int = list.size
                    var e = 0
                    var added = false
                    if (it.conversation != null) {
                        val it1 = it.conversation;
                        if (it1.size < PAGE_COUNT)
                            noList = true
                        else
                            pageStart++
                        for (item in it1) {
                            var f = false
                            for (i2 in list) {
                                if (i2 == item) {
                                    f = true
                                    break
                                }
                            }
                            if (!f && item != null) {
                                list.add(item)
                                added = true
                            }
//                                list.add(0, item)
                        }
                    }

                    e = list.size
                    if (added) {
//                    adapter.notifyItemRangeInserted(s, e)
                        adapter.notifyDataSetChanged()
//                    recyclerView.smoothScrollToPosition(0)
                        recyclerView.smoothScrollToPosition(adapter.itemCount - 1)
                    }
                    if (!TextUtils.isEmpty(it.channelName))
                        channelName = it.channelName
                    if (!TextUtils.isEmpty(it.agoraToken))
                        agoraToken = it.agoraToken
                    if (!TextUtils.isEmpty(it.chatId))
                        chatId = it.chatId
                    if (!TextUtils.isEmpty(it.youUserFirebaseToken))
                        youUserFirebaseToken = it.youUserFirebaseToken

                    if (it?.youOnlineStatus?.contentEquals("online") == true)
                        viewOnline.visibility = View.VISIBLE
                    else
                        viewOnline.visibility = View.GONE

                    tvAudioCallPrice.text = (it?.currency ?: "") + " " + (it?.slotPrice ?: "")
                    tvVideoCallPrice.text = (it?.currency ?: "") + " " + (it?.slotPrice ?: "")

                    bookStatus = it?.bookedStatus ?: "0"
                    adapter.setLoaded()
                } else {
                    Utility.setSnackBarEnglish(
                        this, findViewById(android.R.id.content), it.message
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
    }

    private fun sendMessage(type: String) {
        val msg = etMessage.text.toString()

        if (msg.trim().isEmpty() && type.contentEquals("text")) {
            Utility.setSnackBarEnglish(
                this,
                findViewById(android.R.id.content),
                getString(R.string.err_please_enter_message)
            )
            return
        }

        swipeRefresh.isRefreshing = true
//        Utility.showLoadingDialog(this, getString(R.string.loading), false)
        val jsonObject = JSONObject()
        jsonObject.put("action", "sendMessage")
        jsonObject.put("user_id", "" + senderId)
        jsonObject.put("start", "" + pageStart)
        jsonObject.put("sender_id", "" + senderId)
        jsonObject.put("receiver_id", "" + receiverId)
        jsonObject.put("you_id", "" + receiverId)
        jsonObject.put("message", "" + msg)
        jsonObject.put("type", "message")

        Utility.showLog("jsonObject", "" + jsonObject)

//        val body: RequestBody =
//            jsonObject.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        val request = MultipartBody.Builder()
        request.setType(MultipartBody.FORM)
            .addFormDataPart("action", "sendMessage")
            .addFormDataPart("user_id", senderId ?: "")
            .addFormDataPart("sender_id", senderId ?: "")
            .addFormDataPart("receiverId", receiverId ?: "")
            .addFormDataPart("my_id", senderId ?: "")
            .addFormDataPart("you_id", receiverId ?: "")
            .addFormDataPart("message", msg ?: "")
            .addFormDataPart("customer_type", userCustomerType.toString() ?: "1")
            .addFormDataPart("start", pageStart.toString())

        if (type.contentEquals("text")) {
            request.addFormDataPart("type", "message")
        } else {
            request.addFormDataPart("type", "attachment")
            if (selectedImagePath != null) {
                val imageFile = File(selectedImagePath)
                val requestIImageFile: RequestBody =
                    RequestBody.create("multipart/form-data".toMediaTypeOrNull(), imageFile)
                request.addFormDataPart(
                    "attachment",
                    "${System.currentTimeMillis()}.jpg",
                    requestIImageFile
                )
            }
        }

//        if (!TextUtils.isEmpty(searchKey)) {
//            searchKey?.let { request.addFormDataPart("search", it) }
//        }

        val requestBody = request.build()

        viewModel.sendMessage(requestBody).observe(this, Observer {
            try {
                Utility.hideLoadingDialog()
                swipeRefresh.isRefreshing = false

                if (it == null)
                    return@Observer

                if (it.status == "200") {
                    send = true
                    etMessage.clearText()
                    if (type.contentEquals("text")) {
                        list.add(
                            Conversation(
                                type = "message",
                                message = msg,
                                myimage = user?.profileImage,
                                sendingType = "sent"
                            )
                        )
                    } else {
                        list.add(
                            Conversation(
                                type = "image",
                                message = FileUtils2.getRealPath(this, selectedImageUri),
                                myimage = user?.profileImage,
                                sendingType = "sent"
                            )
                        )
                    }
                    /*list.add(
                        0,
                        Conversation(
                            message = msg,
                            myimage = user?.profileImage,
                            sendingType = "sent"
                        )
                    )*/
                    adapter.notifyItemInserted(adapter.itemCount - 1)
//                    adapter.notifyDataSetChanged()
//                    recyclerView.smoothScrollToPosition(0)
                    recyclerView.smoothScrollToPosition(adapter.itemCount - 1)
                    recyclerView.postDelayed(
                        { recyclerView.smoothScrollToPosition(adapter.itemCount - 1) },
                        1000
                    )
                } else {
                    Utility.setSnackBarEnglish(
                        this, findViewById(android.R.id.content), it.message
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
    }

    override fun onActionAccept(contactsModel: Conversation?) {
        swipeRefresh.isRefreshing = true
        progressBar.visibility = View.VISIBLE

        val request = MultipartBody.Builder()
        request.setType(MultipartBody.FORM)
            .addFormDataPart("action", "doctorSlotStatus")
            .addFormDataPart("user_id", "" + senderId)
            .addFormDataPart("sender_id", "" + senderId)
            .addFormDataPart("receiver_id", "" + receiverId)
            .addFormDataPart("booking_id", contactsModel?.slotBookingId ?: "0")
            .addFormDataPart("status", "accept")
            .addFormDataPart("offer_status", "accept")
            .addFormDataPart("start", "0")

        if (userCustomerType == 0) {
            request.addFormDataPart("doctor_id", "" + receiverId)
            request.addFormDataPart("patient_id", "" + senderId)
        } else {
            request.addFormDataPart("doctor_id", "" + senderId)
            request.addFormDataPart("patient_id", "" + receiverId)
        }

        val requestBody = request.build()

        viewModel.commonRequest(requestBody).observe(this, Observer {
            Utility.hideLoadingDialog()
            swipeRefresh.isRefreshing = false
            progressBar.visibility = View.GONE

            if (it.status == "200") {
                etMessage.clearText()
                list.clear()
                loadMessages()
            } else {
                Utility.setSnackBarEnglish(
                    this, findViewById(android.R.id.content), it.message
                )
            }
        })
    }

    override fun onActionReject(contactsModel: Conversation?) {
        swipeRefresh.isRefreshing = true
        progressBar.visibility = View.VISIBLE

        val request = MultipartBody.Builder()
        request.setType(MultipartBody.FORM)
            .addFormDataPart("action", "doctorSlotStatus")
            .addFormDataPart("user_id", "" + senderId)
            .addFormDataPart("sender_id", "" + senderId)
            .addFormDataPart("receiver_id", "" + receiverId)
            .addFormDataPart("booking_id", contactsModel?.slotBookingId ?: "0")
            .addFormDataPart("status", "decline")
            .addFormDataPart("offer_status", "decline")

        if (userCustomerType == 1) {
            request.addFormDataPart("doctor_id", "" + receiverId)
            request.addFormDataPart("patient_id", "" + userId)
        } else {
            request.addFormDataPart("doctor_id", "" + userId)
            request.addFormDataPart("patient_id", "" + receiverId)
        }

        val requestBody = request.build()

        viewModel.commonRequest(requestBody).observe(this, Observer {
            Utility.hideLoadingDialog()
            swipeRefresh.isRefreshing = false
            progressBar.visibility = View.GONE

            if (it.status == "200") {
                etMessage.clearText()
                list.clear()
                loadMessages()
            } else {
                Utility.setSnackBarEnglish(
                    this, findViewById(android.R.id.content), it.message
                )
            }
        })
    }

    override fun onActionPay(contactsModel: Conversation?) {
        payNow(contactsModel?.slotBookingId)
    }

    private fun payNow(bookingId: String?) {
        if (checkingAccess()) {
            progressBar.visibility = View.VISIBLE

            val request = MultipartBody.Builder()
            request.setType(MultipartBody.FORM)
                .addFormDataPart("action", "slotPaymentNow")
                .addFormDataPart("user_id", userId.toString())
                .addFormDataPart("booking_id", bookingId ?: "")
            val requestBody = request.build()

            viewModel.slotRequest2(requestBody).observe(this, Observer {
                Utility.hideLoadingDialog()
                progressBar.visibility = View.GONE
                if (it == null) {
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        getString(R.string.err_msg_oops_something_went_to_wrong),
                        Snackbar.LENGTH_SHORT
                    ).show()
                    return@Observer
                }
                val str = it.body()?.string() ?: return@Observer;

                if (str.contains("html")) {
                    startActivity(Intent(this, WebViewActivity::class.java).apply {
                        putExtra("content", str)
                        putExtra("payment_url", it.raw().request.url.toString())
                    })
                } else {
                    val slotResponse = Gson().fromJson(str, SlotResponse::class.java)
                    /*if (slotResponse.status == "200") {

                    } else {*/
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        slotResponse.message
                            ?: getString(R.string.err_msg_oops_something_went_to_wrong),
                        Snackbar.LENGTH_SHORT
                    ).show()
//                    }
                }
            })
        }
    }

    private fun openWeb(redirectTo: String?) {
        finish()
        startActivity(Intent(this, WebViewActivity::class.java).apply {
            putExtra("link", redirectTo)
        })
    }

    override fun onStop() {
        super.onStop()
        stopMessages()
    }

    private fun stopMessages() {
        handler?.removeCallbacksAndMessages(null)
    }

    private fun showSettingsDialog() {
        val builder =
            AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.dialog_permission_title))
        builder.setMessage(getString(R.string.dialog_permission_message))
        builder.setPositiveButton(
            getString(R.string.go_to_settings)
        ) { dialog: DialogInterface, _: Int ->
            dialog.cancel()
            openSettings()
        }
        builder.setNegativeButton(
            getString(android.R.string.cancel)
        ) { dialog: DialogInterface, _: Int -> dialog.cancel() }
        builder.show()
    }

    // navigating user to app settings
    private fun openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivityForResult(intent, 101)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200) {
            if (resultCode === Activity.RESULT_OK) {
//                getCategoriesList()
            }
            return
        }
        if (resultCode != Activity.RESULT_CANCELED) {
            when (requestCode) {
                REQUEST_CAMERA -> if (resultCode == Activity.RESULT_OK && data != null) {
                    val selectedImage = data.extras!!["data"] as Bitmap?
                    if (selectedImage != null) {
                        isImageSelected = true
                        imageBitmap = selectedImage
//                        ivProfile.setImageBitmap(selectedImage)

//                        Glide.with(this).asBitmap()
//                            .load(imageBitmap)
//                            .into(ivProfile);

                        sendMessage("")
                    }

                }
                REQUEST_GALLERY -> if (resultCode == Activity.RESULT_OK && data != null) {
                    val selectedImgUri = data.data
                    Utility.showLog("selectedImageUri", "selectedImageUri : $selectedImgUri")
                    selectedImagePath =
                        FileUtils2.getRealPath(this, selectedImgUri) ?: ""
                    selectedImageUri = selectedImgUri!!
                    imageBitmap =
                        MediaStore.Images.Media.getBitmap(
                            contentResolver,
                            selectedImageUri
                        )

                    if (imageBitmap != null) {
                        isImageSelected = true
//                        ivProfile.setImageBitmap(imageBitmap)

//                        Glide.with(this).asBitmap()
//                            .load(imageBitmap)
//                            .into(ivProfile);
                    }
                    sendMessage("")
                }
                REQUEST_DOCUMENT -> if (resultCode == Activity.RESULT_OK && data != null) {

                    val selectedDocUri = data.data

                    // OI FILE Manager
                    var filemanagerstring = selectedDocUri!!.path

                    // MEDIA GALLERY
                    selectedDocumentPath =
                        FileUtils2.getRealPath(this, selectedDocUri) ?: ""
                    selectedDocumentUri = selectedDocUri

//                    etCertificate.setText(selectedDocumentPath)

                    Utility.showLog("selectedImageUri", "selectedImageUri : $selectedDocUri")
                    Utility.showLog("filemanagerstring", "filemanagerstring : $filemanagerstring")
                    Utility.showLog(
                        "selectedImagePath",
                        "selectedImagePath : $selectedDocumentPath"
                    )
//                    callAPIForUploadVideo(selectedImageUri);
                }
            }
        }
    }
}

private fun TextView.clearText() {
    this.text = ""
}