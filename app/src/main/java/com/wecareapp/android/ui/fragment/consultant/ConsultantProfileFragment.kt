package com.wecareapp.android.ui.fragment.consultant

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
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.mcsoft.timerangepickerdialog.StartEndTimePickerDialog
import com.wecareapp.android.Constants
import com.wecareapp.android.R
import com.wecareapp.android.managers.LocaleManager
import com.wecareapp.android.model.Doctor
import com.wecareapp.android.ui.activity.CashoutActivity
import com.wecareapp.android.ui.activity.ConsultantReviewsActivity
import com.wecareapp.android.ui.activity.ProfileBoostActivity
import com.wecareapp.android.ui.activity.SplashActivity
import com.wecareapp.android.ui.fragment.BaseFragment
import com.wecareapp.android.ui.fragment.ICallBackToActivity
import com.wecareapp.android.utilities.DateUtils
import com.wecareapp.android.utilities.FileUtils2
import com.wecareapp.android.utilities.Utility
import com.wecareapp.android.webservices.viewmodels.ConsultantViewModel
import com.wecareapp.android.webservices.viewmodels.PatientViewModel
import kotlinx.android.synthetic.main.activity_clinic_balance.*
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.android.synthetic.main.fragment_clinic_profile.*
import kotlinx.android.synthetic.main.fragment_consultant_profile.*
import kotlinx.android.synthetic.main.fragment_consultant_profile.ivAddPicture
import kotlinx.android.synthetic.main.fragment_consultant_profile.ivAvailableTime
import kotlinx.android.synthetic.main.fragment_consultant_profile.ivLogout
import kotlinx.android.synthetic.main.fragment_consultant_profile.ivProfile
import kotlinx.android.synthetic.main.fragment_consultant_profile.ivSettings
import kotlinx.android.synthetic.main.fragment_consultant_profile.progressBar
import kotlinx.android.synthetic.main.fragment_consultant_profile.swipeRefresh
import kotlinx.android.synthetic.main.fragment_consultant_profile.tvClinicLocation
import kotlinx.android.synthetic.main.fragment_consultant_profile.tvClinicName
import kotlinx.android.synthetic.main.fragment_consultant_profile.tvLanguage
import kotlinx.android.synthetic.main.fragment_patient_profile.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class ConsultantProfileFragment(private val listener: ICallBackToActivity) : BaseFragment(),
    View.OnClickListener,
    StartEndTimePickerDialog.ISelectedTime {

    private val MY_FRAGMENT_ID: Int = 1314
    private var selectedStartTime: String? = null
    private var selectedEndTime: String? = null
    private var startTime: String? = null
    private var endTime: String? = null

    private var doctor: Doctor? = null
    private var profile: Doctor? = null
    private var editMode: Boolean = false
    private lateinit var viewModel: ConsultantViewModel

    private var dialog: Dialog? = null
    private lateinit var selectedImageUri: Uri
    private lateinit var selectedDocumentUri: Uri
    private var selectedDocumentPath: String? = null
    private var selectedImagePath: String? = null
    private var imageBitmap: Bitmap? = null
    private var isImageSelected = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(ConsultantViewModel::class.java)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_consultant_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            doctor =
                if (arguments?.getSerializable("doctor") == null) null else (arguments?.getSerializable(
                    "doctor"
                )) as Doctor

            ivLogout.setOnClickListener(this)
            ivAppointment.setOnClickListener(this)
            ivSettings.setOnClickListener(this)
            appCompatRatingBar.setOnClickListener(this)
            ibB.setOnClickListener(this)
            ibB.setOnClickListener(this)

            swipeRefresh.setOnRefreshListener {
                loadData()
            }

            loadData()

            appCompatRatingBar.setOnClickListener {
                activity?.startActivity(Intent(context, ConsultantReviewsActivity::class.java))
            }

            tvClinicName.setOnClickListener {
                activity?.startActivity(Intent(context, ConsultantReviewsActivity::class.java))
            }

            appCompatRatingBar.setOnTouchListener(View.OnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP && appCompatRatingBar.isEnabled) {
                    activity?.startActivity(Intent(context, ConsultantReviewsActivity::class.java))
                }
                return@OnTouchListener true
            })

            ivProfile.setOnClickListener {
                Dexter.withActivity(activity)
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

            if (TextUtils.isEmpty(lang) || lang.contentEquals("en")) {
                tvLanguage.text = getString(R.string.ar)
                tvLanguage.tag = true
            } else {
                tvLanguage.text = getString(R.string.en)
                tvLanguage.tag = false
            }

            tvLanguage.visibility = View.GONE

            tvLanguage.setOnClickListener {
                var locale = Locale("en")
                if (it.tag == true) {
                    tvLanguage.text = getString(R.string.en)
                    locale = Locale("ar")
                    Utility.setSharedPrefStringData(context, Constants.LANGUAGE, "ar")
                } else {
                    tvLanguage.text = getString(R.string.ar)
                    locale = Locale("en")
                    Utility.setSharedPrefStringData(context, Constants.LANGUAGE, "en")
                }
                LocaleManager.setNewLocale(context, locale)
                val intent = Intent(context, SplashActivity::class.java)
                startActivity(intent)
                activity?.finish()
            }
            closeProfile()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun openDialog() {
        dialog = Dialog(requireContext())
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

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivLogout -> {
                listener.onLogout()
            }
            R.id.ivBack -> {
                listener.onBackClicked(true)
            }
            R.id.ivSettings -> {
                if (editMode) {
                    closeProfile()
                } else {
                    editProfile()
                }
            }
            R.id.ivAppointment -> {
                val fragment = ConsultantMyAppointmentFragment(listener)
                val bundle = Bundle().apply {
//                    putSerializable("clinic", clinic)
                }
                fragment.arguments = bundle
                openFragment(fragment)
            }
            R.id.appCompatRatingBar -> {
                activity?.startActivity(Intent(context, ConsultantReviewsActivity::class.java))
            }
            R.id.ibB -> {
                activity?.startActivity(Intent(context, CashoutActivity::class.java))
            }
        }
    }

    private fun closeProfile() {
        editMode = false
        tvClinicName.isClickable = false
        tvClinicName.isEnabled = false
        ivProfile.isClickable = false
        ivProfile.isEnabled = false
        tvClinicLocation.isClickable = false
        tvClinicLocation.isEnabled = false
        tvAbout.isClickable = false
        tvAbout.isEnabled = false
        appCompatRatingBar.isClickable = true
        appCompatRatingBar.isEnabled = true
        ivAddPicture.visibility = View.GONE
        ivPerHourEdit.visibility = View.GONE
        ivPerCaseEdit.visibility = View.GONE
        tvPerHourEdit.visibility = View.GONE
        tvPerCaseEdit.visibility = View.GONE
        ivPerCase.visibility = View.VISIBLE
        tvPerHour.visibility = View.VISIBLE
        ivSwitch.visibility = View.VISIBLE
        ivSettings.setBackgroundResource(R.drawable.ic_settings_consultant)

        updateProfile()
        updateProfile2()
    }

    private fun editProfile() {
        editMode = true
        tvClinicName.isClickable = true
        tvClinicName.isEnabled = true
        ivProfile.isClickable = true
        ivProfile.isEnabled = true
        tvClinicLocation.isClickable = true
        tvClinicLocation.isEnabled = true
        tvAbout.isClickable = true
        tvAbout.isEnabled = true
        appCompatRatingBar.isClickable = false
        appCompatRatingBar.isEnabled = false
        ivAddPicture.visibility = View.VISIBLE
        ivPerHourEdit.visibility = View.VISIBLE
        tvPerHourEdit.visibility = View.VISIBLE
        tvPerCaseEdit.visibility = View.GONE
        ivPerCaseEdit.visibility = View.GONE
        ivPerCase.visibility = View.GONE
        tvPerHour.visibility = View.GONE
        ivSwitch.visibility = View.GONE
        ivSettings.setBackgroundResource(R.drawable.ic_done_patient)
        ivAvailableTime.setOnClickListener {
            openDialogAvailableTime()
        }
    }

    private fun openDialogAvailableTime() {
        val dialog = StartEndTimePickerDialog()
        dialog.newInstance()
        dialog.setRadiusDialog(20) // Set radius of dialog (default is 50)

        dialog.setIs24HourView(true) // Indicates if the format should be 24 hours

        dialog.setColorBackgroundHeader(R.color.colorPrimary) // Set Color of Background header dialog

        dialog.setColorTextButton(R.color.colorPrimaryDark) // Set Text color of button

        val fragmentManager: FragmentManager? = activity?.supportFragmentManager
        dialog.setTargetFragment(this, MY_FRAGMENT_ID);
        if (fragmentManager != null) {
            dialog.show(fragmentManager, "Time")
        }
    }

    override fun onSelectedTime(hourStart: Int, minuteStart: Int, hourEnd: Int, minuteEnd: Int) {
        val df = SimpleDateFormat("hh:mma", Locale.US)
        val df2 = SimpleDateFormat("HH:mm", Locale.US)
        val calendar = Calendar.getInstance()

        calendar.set(Calendar.HOUR_OF_DAY, hourStart)
        calendar.set(Calendar.MINUTE, minuteStart)
        calendar.set(Calendar.SECOND, 0)
        startTime = df.format(calendar.time)
        selectedStartTime = df2.format(calendar.time)

        calendar.set(Calendar.HOUR_OF_DAY, hourEnd)
        calendar.set(Calendar.MINUTE, minuteEnd)
        calendar.set(Calendar.SECOND, 0)
        endTime = df.format(calendar.time)
        selectedEndTime = df2.format(calendar.time)

        tvAvailableTime.text = "$startTime - $endTime"
    }

    private fun updateProfile() {
        if (checkingAccess()) {
            progressBar.visibility = View.VISIBLE

            val jsonObject = JSONObject()
            jsonObject.put("action", "updateConsultantProfile")
            jsonObject.put("user_id", userId)
            jsonObject.put("customer_type", userId)
            Utility.showLog("jsonObject", "" + jsonObject)

            val request = MultipartBody.Builder()

            request.setType(MultipartBody.FORM)
                .addFormDataPart("action", "updateConsultantProfile")
                .addFormDataPart("customer_type", userCustomerType.toString() ?: "")
                .addFormDataPart("user_id", userId ?: "")
                .addFormDataPart("User_name", tvClinicName.text.toString())
                .addFormDataPart("user_name", tvClinicName.text.toString())
                .addFormDataPart("name", tvClinicName.text.toString())
                .addFormDataPart("address", tvClinicLocation.text.toString())
                .addFormDataPart("about", tvAbout.text.toString())
                .addFormDataPart("per_hour", tvPerHourEdit.text.toString())
                .addFormDataPart("from_time", selectedStartTime.toString())
                .addFormDataPart("to_time", selectedEndTime.toString())

            if (selectedImagePath != null) {
                val imageFile = File(selectedImagePath)
                val requestIImageFile: RequestBody =
                    RequestBody.create("multipart/form-data".toMediaTypeOrNull(), imageFile)
                request.addFormDataPart(
                    "profile_pic",
                    "${System.currentTimeMillis()}.jpg",
                    requestIImageFile
                )
            }

            val requestBody = request.build()

            val viewModel: PatientViewModel =
                ViewModelProvider(this).get(PatientViewModel::class.java)
            viewModel.getProfile(requestBody).observe(viewLifecycleOwner, Observer {
                Utility.hideLoadingDialog()
                progressBar.visibility = View.GONE
                if (it == null)
                    return@Observer
                if (it.status == "200") {
                    val profile = it.data?.get(0)

                    tvClinicName.setText(profile?.userName)
                    tvClinicLocation.setText(profile?.address)
                    tvCategoryName.setText(if (lang.contentEquals("en")) profile?.categoryName else profile?.categoryNameAr)
//                    tvSpendsCount.text = profile?.totalSpent ?: "0"
//                    tvAppCmpCount.text = profile?.appointmentsCompleted ?: "0%"

                    /*val requestOptions: RequestOptions = RequestOptions()
                        .placeholder(R.drawable.ic_clinic_doctor)
                        .error(R.drawable.ic_clinic_doctor)
                        .fitCenter()

                    val img = profile?.profileImage
                    Glide.with(ivProfile.context)
                        .load(img)
                        .fitCenter()
                        .apply(requestOptions)
                        .into(ivProfile)*/

                    /*Snackbar.make(
                        requireActivity().findViewById(android.R.id.content),
                        it.message ?: getString(R.string.msg_profile_updated_successfully),
                        Snackbar.LENGTH_SHORT
                    ).show()*/
                } else {
                    Snackbar.make(
                        requireActivity().findViewById(android.R.id.content),
                        it.message ?: getString(R.string.err_msg_oops_something_went_to_wrong),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                loadData()
            })
        }
    }

    private fun updateProfile2() {
        if (checkingAccess()) {
            progressBar.visibility = View.VISIBLE

            val jsonObject = JSONObject()
            jsonObject.put("action", "update_profile")
            jsonObject.put("user_id", userId)
            jsonObject.put("customer_type", userId)
            Utility.showLog("jsonObject", "" + jsonObject)

            val request = MultipartBody.Builder()

            request.setType(MultipartBody.FORM)
                .addFormDataPart("action", "update_profile")
                .addFormDataPart("customer_type", userCustomerType.toString() ?: "")
                .addFormDataPart("user_id", userId ?: "")
                .addFormDataPart("User_name", tvClinicName.text.toString())
                .addFormDataPart("user_name", tvClinicName.text.toString())
                .addFormDataPart("name", tvClinicName.text.toString())
                .addFormDataPart("address", tvClinicLocation.text.toString())
                .addFormDataPart("about", tvAbout.text.toString())
                .addFormDataPart("per_hour", tvPerHourEdit.text.toString())
                .addFormDataPart("from_time", selectedStartTime.toString())
                .addFormDataPart("to_time", selectedEndTime.toString())

            if (selectedImagePath != null) {
                val imageFile = File(selectedImagePath)
                val requestIImageFile: RequestBody =
                    RequestBody.create("multipart/form-data".toMediaTypeOrNull(), imageFile)
                request.addFormDataPart(
                    "profile_pic",
                    "${System.currentTimeMillis()}.jpg",
                    requestIImageFile
                )
            }


            val requestBody = request.build()

            val viewModel: PatientViewModel =
                ViewModelProvider(this).get(PatientViewModel::class.java)
            viewModel.getProfile(requestBody).observe(viewLifecycleOwner, Observer {
                Utility.hideLoadingDialog()
                progressBar.visibility = View.GONE
                if (it == null)
                    return@Observer
                if (it.status == "200") {
                    val profile = it.data?.get(0)

                    tvClinicName.setText(profile?.userName)
                    tvClinicLocation.setText(profile?.address)
                    tvCategoryName.setText(if (lang.contentEquals("en")) profile?.categoryName else profile?.categoryNameAr)
//                    tvSpendsCount.text = profile?.totalSpent ?: "0"
//                    tvAppCmpCount.text = profile?.appointmentsCompleted ?: "0%"

                    /*val requestOptions: RequestOptions = RequestOptions()
                        .placeholder(R.drawable.ic_clinic_doctor)
                        .error(R.drawable.ic_clinic_doctor)
                        .fitCenter()

                    val img = profile?.profileImage
                    Glide.with(ivProfile.context)
                        .load(img)
                        .fitCenter()
                        .apply(requestOptions)
                        .into(ivProfile)*/

                    /*Snackbar.make(
                        requireActivity().findViewById(android.R.id.content),
                        it.message ?: getString(R.string.msg_profile_updated_successfully),
                        Snackbar.LENGTH_SHORT
                    ).show()*/
                } else {
                    Snackbar.make(
                        requireActivity().findViewById(android.R.id.content),
                        it.message ?: getString(R.string.err_msg_oops_something_went_to_wrong),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                loadData()
            })
        }
    }

    private fun loadData() {
        if (checkingAccess()) {
            progressBar.visibility = View.VISIBLE

            val jsonObject = JSONObject()
            jsonObject.put("action", "getConsultantProfile")
            jsonObject.put("user_id", userId)
            Utility.showLog("jsonObject", "" + jsonObject)

            val request = MultipartBody.Builder()

            request.setType(MultipartBody.FORM)
                .addFormDataPart("action", "getConsultantProfile")
                .addFormDataPart("user_id", userId ?: "")
            val requestBody = request.build()

            viewModel.getConsultantProfile(requestBody).observe(viewLifecycleOwner, Observer {
                Utility.hideLoadingDialog()
                progressBar.visibility = View.GONE
                swipeRefresh.isRefreshing = false

                if (it == null)
                    return@Observer
                if (it.status == "200") {

                    profile = it.profile?.get(0)

                    tvClinicName.setText(profile?.userName)
                    tvClinicLocation.setText(profile?.address)
                    tvCategoryName.setText(if (lang.contentEquals("en")) profile?.categoryName else profile?.categoryNameAr)
                    tvAbout.setText(profile?.about)
                    tvBalance.text = profile?.currency + " " + profile?.userBalance.toString()
                    tvTotalConsultations.text = profile?.totalBookings
                    tvPerHour.text = (profile?.currency ?: "SAR") + (profile?.perHour ?: "0") + "/h"
                    tvPerHourEdit.setText(profile?.perHour ?: "0")
                    tvAvailableTime.text =
                        DateUtils.displayTimeSlot2(profile?.fromAvailableTime) +
                                " - " + DateUtils.displayTimeSlot2(profile?.toAvailableTime)

                    selectedStartTime = profile?.fromAvailableTime
                    selectedEndTime = profile?.toAvailableTime

                    appCompatRatingBar.rating = profile?.rating?.toFloat() ?: 0F

                    val requestOptions: RequestOptions = RequestOptions()
                        .placeholder(R.drawable.ic_clinic_doctor)
                        .error(R.drawable.ic_clinic_doctor)
                        .fitCenter()

                    val img = profile?.profileImage
                    Glide.with(ivProfile.context)
                        .load(img)
                        .fitCenter()
                        .apply(requestOptions)
                        .into(ivProfile)

                    ivRemember.setOnClickListener {
                        try {
                            var expired = true
                            val sdf = SimpleDateFormat("yyyy-MM-dd")
                            val strDate = sdf.parse(profile?.membershipExpiry)
                            if (Date().after(strDate)) {
                                expired = false
                            }
                            if (expired) {
                                startActivity(Intent(context, ProfileBoostActivity::class.java))
                                activity?.overridePendingTransition(
                                    R.anim.slide_in_right,
                                    R.anim.slide_out_left
                                )
                            } else {
                                Snackbar.make(
                                    requireActivity().findViewById(android.R.id.content),
                                    String.format(
                                        getString(R.string.msg_membership_expiry),
                                        profile?.membershipExpiry
                                    ),
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else {
                    Snackbar.make(
                        requireActivity().findViewById(android.R.id.content),
                        it.message ?: getString(R.string.err_msg_oops_something_went_to_wrong),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }

    private fun showSettingsDialog() {
        val builder =
            AlertDialog.Builder(requireContext())
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
        val uri = Uri.fromParts("package", activity?.packageName, null)
        intent.data = uri
        startActivityForResult(intent, 101)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        /*if (requestCode == 200) {
            if (resultCode === Activity.RESULT_OK) {
//                getCategoriesList()
            }
            return
        }*/

        if (requestCode == MY_FRAGMENT_ID) {
            if (resultCode == Activity.RESULT_OK) {
                if (data?.extras?.containsKey(StartEndTimePickerDialog.HOUR_START) == true) {
                    val hourStart: Int =
                        data.extras?.getInt(StartEndTimePickerDialog.HOUR_START) ?: 0
                    val hourEnd: Int =
                        data.extras?.getInt(StartEndTimePickerDialog.HOUR_END) ?: 0
                    val minuteStart: Int =
                        data.extras?.getInt(StartEndTimePickerDialog.MINUTE_START) ?: 0
                    val minuteEnd: Int =
                        data.extras?.getInt(StartEndTimePickerDialog.MINUTE_END) ?: 0

                    onSelectedTime(hourStart, minuteStart, hourEnd, minuteEnd)
                }
            }
        }
        if (resultCode != Activity.RESULT_CANCELED) {
            when (requestCode) {
                REQUEST_CAMERA -> if (resultCode == Activity.RESULT_OK && data != null) {
                    val selectedImage = data.extras!!["data"] as Bitmap?
                    if (selectedImage != null) {
                        isImageSelected = true
                        imageBitmap = selectedImage
//                        ivProfile.setImageBitmap(selectedImage)

                        selectedImageUri = Utility.getImageUri(context, selectedImage)!!
                        selectedImagePath =
                            FileUtils2.getRealPath(requireContext(), selectedImageUri) ?: ""

                        Glide.with(this).asBitmap()
                            .load(imageBitmap)
                            .into(ivProfile);
                    }
                }
                REQUEST_GALLERY -> if (resultCode == Activity.RESULT_OK && data != null) {
                    val selectedImgUri = data.data
                    Utility.showLog("selectedImageUri", "selectedImageUri : $selectedImgUri")
                    selectedImagePath =
                        FileUtils2.getRealPath(requireContext(), selectedImgUri) ?: ""
                    selectedImageUri = selectedImgUri!!
                    imageBitmap =
                        MediaStore.Images.Media.getBitmap(
                            activity?.contentResolver,
                            selectedImageUri
                        )

                    if (imageBitmap != null) {
                        isImageSelected = true
//                        ivProfile.setImageBitmap(imageBitmap)

                        Glide.with(this).asBitmap()
                            .load(imageBitmap)
                            .into(ivProfile);
                    }
                }
                REQUEST_DOCUMENT -> if (resultCode == Activity.RESULT_OK && data != null) {

                    val selectedDocUri = data.data

                    // OI FILE Manager
                    var filemanagerstring = selectedDocUri!!.path

                    // MEDIA GALLERY
                    selectedDocumentPath =
                        FileUtils2.getRealPath(requireContext(), selectedDocUri) ?: ""
                    selectedDocumentUri = selectedDocUri

//                    etCertificate.setText(selectedDocumentPath)

                    Utility.showLog("selectedImageUri", "selectedImageUri : " + selectedDocUri)
                    Utility.showLog("filemanagerstring", "filemanagerstring : " + filemanagerstring)
                    Utility.showLog(
                        "selectedImagePath",
                        "selectedImagePath : " + selectedDocumentPath
                    )
//                    callAPIForUploadVideo(selectedImageUri);
                }
            }
        }
    }
}