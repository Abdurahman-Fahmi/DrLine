package com.wecareapp.android.ui.activity

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.provider.Settings
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.wecareapp.android.R
import com.wecareapp.android.model.Category
import com.wecareapp.android.utilities.FileUtils2
import com.wecareapp.android.utilities.Utility
import com.wecareapp.android.webservices.viewmodels.SignUpViewModel
import kotlinx.android.synthetic.main.activity_sign_up.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject


class SignUpActivity : BaseActivity(), View.OnClickListener {
    private var selectedDocumentExt: String = ""
    private var selectedCategory: Category? = null
    private lateinit var selectedDocumentUri: Uri
    private lateinit var selectedDocumentPath: String
    private lateinit var signUpViewModel: SignUpViewModel
    private var userType: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        signUpViewModel = ViewModelProvider(this).get(SignUpViewModel::class.java)

        tvSignUp.setOnClickListener(this)
        tvSignIn.setOnClickListener(this)
        ivPatient.setOnClickListener(this)
        ivConsultant.setOnClickListener(this)
        ivClinic.setOnClickListener(this)
        etCategory.setOnClickListener(this)

        etCertificate.setOnClickListener {
            uploadDocument()
        }
        selectedPatient()

//        tvStart.setOnClickListener {
//            startActivity(Intent(this, SignInActivity::class.java))
//        }
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
                        intent.type = "*/*"
                        intent.action = Intent.ACTION_GET_CONTENT
                        startActivityForResult(
                            Intent.createChooser(intent, getString(R.string.select_certificate)),
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
        ) { dialog: DialogInterface, which: Int ->
            dialog.cancel()
            openSettings()
        }
        builder.setNegativeButton(
            getString(android.R.string.cancel)
        ) { dialog: DialogInterface, which: Int -> dialog.cancel() }
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
        if (resultCode != Activity.RESULT_CANCELED) {
            when (requestCode) {
                2000 -> {
                    selectedCategory = data?.extras?.getSerializable("category") as Category
                    etCategory.setText(selectedCategory?.categoryName)
                }
                REQUEST_DOCUMENT -> if (resultCode == Activity.RESULT_OK && data != null) {

                    val selectedImageUri = data.data

                    // OI FILE Manager
                    var filemanagerstring = selectedImageUri!!.path

                    // MEDIA GALLERY
                    selectedDocumentPath = FileUtils2.getRealPath(this, selectedImageUri) ?: ""
                    selectedDocumentUri = selectedImageUri
                    selectedDocumentExt =
                        selectedDocumentPath.substring(selectedDocumentPath.lastIndexOf("."))

                    etCertificate.setText(selectedDocumentPath)

                    Utility.showLog("selectedImageUri", "selectedImageUri : " + selectedImageUri)
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

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvSignUp -> {
                signUp()
            }
            R.id.tvSignIn -> {
                finish()
            }
            R.id.ivPatient -> {
                selectedPatient()
            }
            R.id.ivConsultant -> {
                selectedConsultant()
            }
            R.id.ivClinic -> {
                selectedClinic()
            }
            R.id.etCategory -> {
                selectService()
            }
        }
    }

    private fun selectService() {
        startActivityForResult(Intent(this, AddServiceCategoryActivity::class.java).apply {
            putExtra("action", "signup")
        }, 2000)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun signUp() {
        if (isValidData()) {
            Utility.showLoadingDialog(this, getString(R.string.msg_please_wait_registering), false)
            val jsonObject = JSONObject()
            jsonObject.put("action", "signup")
            jsonObject.put("user_password", etPassword.text.toString())
            var email = ""
            var name = ""
            if (userType == 1) {
                email = etEmail.text.toString().trim()
                name = etName.text.toString()
            } else {
                name = etName2.text.toString()
                email = etEmail2.text.toString().trim()
            }

            val contactType = if (email.contains("@")) "2" else "1"
            jsonObject.put("user_contact", email)
            jsonObject.put("user_name", name)
            jsonObject.put("customer_type", userType)
            jsonObject.put("contact_type", contactType)
            jsonObject.put("user_token_id", firebaseToken)
            Utility.showLog("jsonObject", "" + jsonObject)

            val body: RequestBody =
                jsonObject.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            val request = MultipartBody.Builder()

            request.setType(MultipartBody.FORM)
                .addFormDataPart("action", "signup")
                .addFormDataPart("user_contact", email)
                .addFormDataPart("user_name", name)
                .addFormDataPart("customer_type", userType.toString())
                .addFormDataPart("contact_type", contactType)
                .addFormDataPart("user_token_id", firebaseToken)
                .addFormDataPart("user_device_id", firebaseToken)
                .addFormDataPart("latitude", gps?.latitude.toString())
                .addFormDataPart("longitude", gps?.longitude.toString())
                .addFormDataPart("user_latitude", gps?.latitude.toString())
                .addFormDataPart("user_longitude", gps?.longitude.toString())
                .addFormDataPart("user_password", etPassword.text.toString())

            if (userType != 1) {
                request.addFormDataPart("about", etAbout.text.toString())
                val file = FileUtils2.getRealPath(applicationContext, selectedDocumentUri)
                val requestFile: RequestBody =
                    RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)
                request.addFormDataPart(
                    "certificate",
                    "${System.currentTimeMillis()}.$selectedDocumentExt",
                    requestFile
                )
            }

            if (userType == 2) {
                request.addFormDataPart("category_id", selectedCategory?.categoryId ?: "1")
            }

            val requestBody = request.build()

            signUpViewModel.action(requestBody).observe(this, Observer {
                Utility.hideLoadingDialog()
                if (it == null)
                    return@Observer
                if (it.status == "200") {
                    finish()
                    startActivity(Intent(this, OtpActivity::class.java).apply {
                        putExtra("email", email)
                        putExtra("customer_type", userType.toString())
                        putExtra("contact_type", contactType)
                    })
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
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

    fun ContentResolver.getFileName(fileUri: Uri): String {
        var name = ""
        val returnCursor = this.query(fileUri, null, null, null, null)
        if (returnCursor != null) {
            val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            returnCursor.moveToFirst()
            name = returnCursor.getString(nameIndex)
            returnCursor.close()
        }

        return name
    }

    private fun isValidData(): Boolean {
        if (userType == 1) {
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
        } else {
            val email = etEmail2.text.toString().trim()
            if (!(isValidMail(email) || isValidMobile(email))) {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    getString(R.string.err_msg_please_enter_email_contact),
                    Snackbar.LENGTH_SHORT
                ).show()
                etEmail2.requestFocus()
                return false
            }
            if (etName2.text.isNullOrEmpty()) {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    getString(R.string.err_msg_plz_enter_full_name),
                    Snackbar.LENGTH_SHORT
                ).show()
                etName2.requestFocus()
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
        }

        if (userType == 2 && selectedCategory == null) {
            Snackbar.make(
                findViewById(android.R.id.content),
                getString(R.string.err_msg_please_select_service),
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

    private fun selectedPatient() {
        userType = 1

        ivPatient.visibility = View.GONE
        tvPatient.visibility = View.GONE

        ivConsultant.visibility = View.VISIBLE
        tvConsultant.visibility = View.VISIBLE

        ivClinic.visibility = View.VISIBLE
        tvClinic.visibility = View.VISIBLE

        etName.visibility = View.VISIBLE
        etEmail.visibility = View.VISIBLE

        etName2.visibility = View.GONE
        etEmail2.visibility = View.GONE

        etCertificate.visibility = View.GONE
        etAbout.visibility = View.GONE
        etCategory.visibility = View.GONE
    }

    private fun selectedConsultant() {
        userType = 2

        ivConsultant.visibility = View.GONE
        tvConsultant.visibility = View.GONE

        ivClinic.visibility = View.VISIBLE
        tvClinic.visibility = View.VISIBLE

        etCategory.visibility = View.VISIBLE

        showClinicUI()
    }

    private fun selectedClinic() {
        userType = 3

        ivConsultant.visibility = View.VISIBLE
        tvConsultant.visibility = View.VISIBLE

        ivClinic.visibility = View.GONE
        tvClinic.visibility = View.GONE

        etCategory.visibility = View.GONE

        showClinicUI()
    }

    private fun showClinicUI() {
        ivPatient.visibility = View.VISIBLE
        tvPatient.visibility = View.VISIBLE

        etName.visibility = View.GONE
        etEmail.visibility = View.GONE

        etName2.visibility = View.VISIBLE
        etEmail2.visibility = View.VISIBLE

        etCertificate.visibility = View.VISIBLE
        etAbout.visibility = View.VISIBLE
    }

}