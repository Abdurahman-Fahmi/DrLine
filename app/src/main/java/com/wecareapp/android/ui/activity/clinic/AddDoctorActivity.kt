package com.wecareapp.android.ui.activity.clinic

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
import android.text.InputType
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.mcsoft.timerangepickerdialog.StartEndTimePickerDialog
import com.wecareapp.android.R
import com.wecareapp.android.adapter.ServiceCategoryListAdapter
import com.wecareapp.android.model.Category
import com.wecareapp.android.ui.activity.AddServiceCategoryActivity
import com.wecareapp.android.ui.activity.BaseActivity
import com.wecareapp.android.utilities.FileUtils2
import com.wecareapp.android.utilities.Utility
import com.wecareapp.android.webservices.viewmodels.PatientViewModel
import kotlinx.android.synthetic.main.activity_clinic_add_doctor.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class AddDoctorActivity : BaseActivity(), ServiceCategoryListAdapter.Listener,
    StartEndTimePickerDialog.ISelectedTime {
    private lateinit var viewModel: PatientViewModel

    private var selectedStartTime: String? = null
    private var selectedEndTime: String? = null
    private var startTime: String? = null
    private var endTime: String? = null
    private var selectedHourlyRate: String? = null
    private var selectedCategory: Category? = null
    private var serviceCategoryList: MutableList<Category?> = mutableListOf()
    private lateinit var serviceCategoryAdapter: ServiceCategoryListAdapter
    private lateinit var selectedImageUri: Uri
    private lateinit var selectedDocumentUri: Uri
    private var selectedDocumentPath: String? = null
    private var selectedDocumentExt: String = ""
    private var selectedImagePath: String? = null
    private var imageBitmap: Bitmap? = null
    private var isImageSelected = false
    private var dialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clinic_add_doctor)
        viewModel = ViewModelProvider(this).get(PatientViewModel::class.java)

        serviceRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        serviceCategoryAdapter = ServiceCategoryListAdapter(serviceCategoryList, this)
        serviceRecyclerView.adapter = serviceCategoryAdapter

        ivBack.setOnClickListener {
            onBackPressed()
        }

        etCertificate.setOnClickListener {
            uploadDocument()
        }

        getCategoriesList()

        ivProfile.setOnClickListener {
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

        viewAvailableTime.setOnClickListener {
            openDialogAvailableTime()
        }

        hourlyRateView.setOnClickListener {
            openDialogHourly()
        }

        ivAddDoctor.setOnClickListener {
            addDoctor()
        }
    }

    private fun addDoctor() {
        if (checkingAccess()) {
            if (!isValidData()) {
                return
            }
            progressBar.visibility = View.VISIBLE

            val jsonObject = JSONObject()
            jsonObject.put("action", "categories_list")
            jsonObject.put("user_id", userId ?: "")
            jsonObject.put("customer_type", "3")
            Utility.showLog("jsonObject", "" + jsonObject)


            val email = etEmail.text.toString().trim()
            val name = etName.text.toString()
            val contactType = if (email.contains("@")) "2" else "1"

            val request = MultipartBody.Builder()
            request.setType(MultipartBody.FORM)
                .addFormDataPart("action", "add_doctor")
                .addFormDataPart("user_id", userId ?: "")
                .addFormDataPart("customer_type", "3")
                .addFormDataPart("user_contact", email)
                .addFormDataPart("contact_type", contactType)
                .addFormDataPart("user_name", name)
                .addFormDataPart("user_password", etPassword.text.toString())
                .addFormDataPart("address", etLocation.text.toString())
                .addFormDataPart("about", etAbout.text.toString())
                .addFormDataPart("category_id", selectedCategory?.categoryId ?: "")
                .addFormDataPart("from_time", selectedStartTime ?: "")
                .addFormDataPart("to_time", selectedEndTime ?: "")
                .addFormDataPart("per_hour", selectedHourlyRate ?: "0")
                .addFormDataPart("latitude", gps?.latitude.toString())
                .addFormDataPart("longitude", gps?.longitude.toString())
                .addFormDataPart("user_latitude", gps?.latitude.toString())
                .addFormDataPart("user_longitude", gps?.longitude.toString())

            if (selectedImagePath != null) {
                val imageFile = File(selectedImagePath)
                val requestIImageFile: RequestBody =
                    RequestBody.create("multipart/form-data".toMediaTypeOrNull(), imageFile)
                request.addFormDataPart(
                    "profile_image",
                    "${System.currentTimeMillis()}.jpg",
                    requestIImageFile
                )
            }

            if (selectedDocumentPath != null) {
//                val file = File(selectedDocumentPath)
                val file = FileUtils2.getRealPath(applicationContext, selectedDocumentUri)
                val requestFile: RequestBody =
                    RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)
                request.addFormDataPart(
                    "certificate",
                    "${System.currentTimeMillis()}.$selectedDocumentExt",
                    requestFile
                )
            }

            val requestBody = request.build()

            viewModel.getCategoriesList(requestBody).observe(this, Observer {
                Utility.hideLoadingDialog()
                progressBar.visibility = View.GONE
                if (it == null)
                    return@Observer
                if (it.status == "200") {
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        it.message ?: getString(R.string.err_msg_oops_something_went_to_wrong),
                        Snackbar.LENGTH_SHORT
                    ).show()
                    finish()
                } else {
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        it.message ?: getString(R.string.err_msg_oops_something_went_to_wrong),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }

    private fun isValidData(): Boolean {
        val email = etEmail.text.toString().trim()
        if (!(isValidMail(email) || isValidMobile(email))) {
            Snackbar.make(
                findViewById(android.R.id.content),
                getString(R.string.err_msg_please_enter_email_contact),
                Snackbar.LENGTH_SHORT
            ).show()
            etEmail.requestFocus()
            return false
        }
        if (etName.text.isNullOrEmpty()) {
            Snackbar.make(
                findViewById(android.R.id.content),
                getString(R.string.err_msg_plz_enter_full_name),
                Snackbar.LENGTH_SHORT
            ).show()
            etName.requestFocus()
            return false
        }

        if (etAbout.text.isNullOrEmpty()) {
            Snackbar.make(
                findViewById(android.R.id.content),
                getString(R.string.err_msg_plz_about_should_not_empty),
                Snackbar.LENGTH_SHORT
            ).show()
            etAbout.requestFocus()
            return false
        }
        if (etCertificate.text.isNullOrEmpty()) {
            Snackbar.make(
                findViewById(android.R.id.content),
                getString(R.string.err_msg_plz_choose_certificate),
                Snackbar.LENGTH_SHORT
            ).show()
            return false
        }

        if (selectedCategory == null) {
            Snackbar.make(
                findViewById(android.R.id.content),
                getString(R.string.err_msg_please_select_category),
                Snackbar.LENGTH_SHORT
            ).show()
            return false
        }

        if (etPassword.text.isNullOrEmpty()) {
            Snackbar.make(
                findViewById(android.R.id.content),
                getString(R.string.err_msg_please_enter_password),
                Snackbar.LENGTH_SHORT
            ).show()
            etPassword.requestFocus()
            return false
        }

        if (etConfirmPassword.text.isNullOrEmpty()) {
            Snackbar.make(
                findViewById(android.R.id.content),
                getString(R.string.err_msg_please_enter_confirm_password),
                Snackbar.LENGTH_SHORT
            ).show()
            etConfirmPassword.requestFocus()
            return false
        }

        if (etConfirmPassword.text.toString().trim() != etPassword.text.toString().trim()) {
            Snackbar.make(
                findViewById(android.R.id.content),
                getString(R.string.err_msg_confirm_pwd_mat),
                Snackbar.LENGTH_SHORT
            ).show()
            etConfirmPassword.requestFocus()
            return false
        }

        return true
    }

    private fun openDialogAvailableTime() {
        val dialog = StartEndTimePickerDialog()
        dialog.newInstance()
        dialog.setRadiusDialog(20) // Set radius of dialog (default is 50)

        dialog.setIs24HourView(true) // Indicates if the format should be 24 hours

        dialog.setColorBackgroundHeader(R.color.colorPrimary) // Set Color of Background header dialog

        dialog.setColorTextButton(R.color.colorPrimaryDark) // Set Text color of button

        val fragmentManager: FragmentManager = supportFragmentManager
        dialog.show(fragmentManager, "Time")
    }

    override fun onSelectedTime(hourStart: Int, minuteStart: Int, hourEnd: Int, minuteEnd: Int) {
        val df = SimpleDateFormat("hh:mma", Locale.US)
        val df2 = SimpleDateFormat("HH:mm:ss", Locale.US)
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

    private fun openDialogHourly() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.enter_hourly_rate))

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        builder.setView(input)

        builder.setPositiveButton(getString(R.string.ok)) { _, _ ->
            selectedHourlyRate = input.text.toString()
            tvHourlyRate.text = "$$selectedHourlyRate\\h"
        }
        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.cancel() }

        builder.show()
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

    private fun getCategoriesList() {
        if (checkingAccess()) {
            progressBar.visibility = View.VISIBLE

            val jsonObject = JSONObject()
            jsonObject.put("action", "categories_list")
            jsonObject.put("user_id", userId ?: "")
            jsonObject.put("customer_type", "3")
            Utility.showLog("jsonObject", "" + jsonObject)

            val request = MultipartBody.Builder()

            request.setType(MultipartBody.FORM)
                .addFormDataPart("action", "categories_list")
                .addFormDataPart("user_id", userId ?: "")
                .addFormDataPart("customer_type", "3")
            val requestBody = request.build()

            viewModel.getCategoriesList(requestBody).observe(this, Observer {
                Utility.hideLoadingDialog()
                progressBar.visibility = View.GONE
                if (it == null)
                    return@Observer
                if (it.status == "200") {
                    it.data?.let { it1 -> serviceCategoryList.addAll(it1) }
                    serviceCategoryList.add(null)
                    if (serviceCategoryList.isEmpty()) {
                        serviceRecyclerView.visibility = View.INVISIBLE
                    } else {
                        serviceRecyclerView.visibility = View.VISIBLE
                        serviceCategoryAdapter.notifyDataSetChanged()
                    }
                } else {
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        it.message ?: getString(R.string.err_msg_oops_something_went_to_wrong),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }


    override fun onItemSelected(category: Category?, adapterPosition: Int) {
        if (category == null) {
            startActivityForResult(Intent(this, AddServiceCategoryActivity::class.java), 200)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        } else {
            selectedCategory = (category)

            for (category in serviceCategoryList) {
                category?.selected = false
            }

            selectedCategory?.selected = true
            serviceCategoryAdapter.notifyDataSetChanged()
        }
    }

    private fun uploadDocument() {
        Dexter.withActivity(this)
            .withPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        val intent = Intent()
                        intent.type = "application/pdf"
                        intent.action = Intent.ACTION_GET_CONTENT
                        startActivityForResult(
                            Intent.createChooser(
                                intent,
                                getString(R.string.select_a_document_type)
                            ),
                            REQUEST_DOCUMENT
                        )
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
                getCategoriesList()
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
                        ivProfile.setImageBitmap(selectedImage)

                        selectedImageUri = Utility.getImageUri(this, selectedImage)!!
                        selectedImagePath =
                            FileUtils2.getRealPath(this, selectedImageUri) ?: ""
                    }
                }
                REQUEST_GALLERY -> if (resultCode == Activity.RESULT_OK && data != null) {
                    val selectedImgUri = data.data
                    Utility.showLog("selectedImageUri", "selectedImageUri : $selectedImgUri")
                    selectedImagePath = FileUtils2.getRealPath(this, selectedImgUri) ?: ""
                    selectedImageUri = selectedImgUri!!
                    imageBitmap =
                        MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)

                    if (imageBitmap != null) {
                        isImageSelected = true
                        ivProfile.setImageBitmap(imageBitmap)
                    }
                }
                REQUEST_DOCUMENT -> if (resultCode == Activity.RESULT_OK && data != null) {

                    val selectedDocUri = data.data

                    // OI FILE Manager
                    val filemanagerstring = selectedDocUri!!.path

                    // MEDIA GALLERY
                    selectedDocumentPath = FileUtils2.getRealPath(this, selectedDocUri) ?: ""
                    selectedDocumentUri = selectedDocUri

                    selectedDocumentExt =
                        selectedDocumentPath!!.substring(selectedDocumentPath!!.lastIndexOf("."))

                    etCertificate.setText(selectedDocumentPath)

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