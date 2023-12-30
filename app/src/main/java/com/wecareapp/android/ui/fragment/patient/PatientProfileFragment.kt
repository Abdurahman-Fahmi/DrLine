package com.wecareapp.android.ui.fragment.patient

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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import androidx.fragment.app.Fragment
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
import com.wecareapp.android.Constants
import com.wecareapp.android.R
import com.wecareapp.android.managers.LocaleManager
import com.wecareapp.android.ui.activity.SplashActivity
import com.wecareapp.android.ui.fragment.BaseFragment
import com.wecareapp.android.ui.fragment.ICallBackToActivity
import com.wecareapp.android.utilities.FileUtils2
import com.wecareapp.android.utilities.Utility
import com.wecareapp.android.webservices.viewmodels.PatientViewModel
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.android.synthetic.main.fragment_clinic_profile.*
import kotlinx.android.synthetic.main.fragment_patient_profile.*
import kotlinx.android.synthetic.main.fragment_patient_profile.ivAddPicture
import kotlinx.android.synthetic.main.fragment_patient_profile.ivLogout
import kotlinx.android.synthetic.main.fragment_patient_profile.ivProfile
import kotlinx.android.synthetic.main.fragment_patient_profile.ivSettings
import kotlinx.android.synthetic.main.fragment_patient_profile.progressBar
import kotlinx.android.synthetic.main.fragment_patient_profile.tvClinicLocation
import kotlinx.android.synthetic.main.fragment_patient_profile.tvLanguage
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.File
import java.util.*

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class PatientProfileFragment : BaseFragment(),
    View.OnClickListener {
    private var editMode: Boolean = false
    private lateinit var viewModel: PatientViewModel
    private lateinit var listener: ICallBackToActivity

    private var dialog: Dialog? = null
    private lateinit var selectedImageUri: Uri
    private lateinit var selectedDocumentUri: Uri
    private var selectedDocumentPath: String? = null
    private var selectedImagePath: String? = null
    private var imageBitmap: Bitmap? = null
    private var isImageSelected = false

    companion object {
        fun newInstance(
            bundle: Bundle,
            listener: ICallBackToActivity
        ): PatientProfileFragment {
            val fragment = PatientProfileFragment()
            fragment.arguments = bundle
            fragment.setListener(listener)
            return fragment
        }

        fun newInstance(listener: ICallBackToActivity): PatientProfileFragment {
            val fragment = PatientProfileFragment()
            fragment.setListener(listener)
            return fragment
        }

        fun newInstance(bundle: Bundle): PatientProfileFragment {
            val fragment = PatientProfileFragment()
            fragment.arguments = bundle
            return fragment
        }

        fun newInstance(): PatientProfileFragment {
            return PatientProfileFragment()
        }
    }

    private fun setListener(listener: ICallBackToActivity) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(PatientViewModel::class.java)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_patient_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            ivLogout.setOnClickListener(this)
            ivBack.setOnClickListener(this)
            ivSettings.setOnClickListener(this)
            loadData()
            tvUserName.setText(user?.userName)
            tvClinicLocation.setText(user?.address)

            val requestOptions: RequestOptions = RequestOptions()
                .placeholder(R.drawable.ic_clinic_doctor)
                .error(R.drawable.ic_clinic_doctor)
                .fitCenter()

            val img = user?.profileImage
            Glide.with(this)
                .load(img)
                .fitCenter()
                .apply(requestOptions)
                .into(ivProfile)

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
        }
    }

    private fun closeProfile() {
        editMode = false
        tvUserName.isClickable = false
        tvUserName.isEnabled = false
        ivProfile.isClickable = false
        ivProfile.isEnabled = false
        tvClinicLocation.isClickable = false
        tvClinicLocation.isEnabled = false
        ivAddPicture.visibility = View.GONE
        ivSettings.setBackgroundResource(R.drawable.ic_settings)

        updateProfile()
    }

    private fun editProfile() {
        editMode = true
        tvUserName.isClickable = true
        tvUserName.isEnabled = true
        ivProfile.isClickable = true
        ivProfile.isEnabled = true
        tvClinicLocation.isClickable = true
        tvClinicLocation.isEnabled = true
        ivAddPicture.visibility = View.VISIBLE
        ivSettings.setBackgroundResource(R.drawable.ic_done_patient)
    }

    private fun updateProfile() {
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
                .addFormDataPart("User_name", tvUserName.text.toString())
                .addFormDataPart("user_name", tvUserName.text.toString())
                .addFormDataPart("address", tvClinicLocation.text.toString())

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

            viewModel.getProfile(requestBody).observe(viewLifecycleOwner, Observer {
                Utility.hideLoadingDialog()
                progressBar.visibility = View.GONE
                if (it == null)
                    return@Observer
                if (it.status == "200") {
                    val profile = it.data?.get(0)

                    tvUserName.setText(profile?.userName)
                    tvClinicLocation.setText(profile?.address)
                    tvAppCount.text = profile?.totalAppointments ?: "0"
                    tvSpendsCount.text = profile?.totalSpent ?: "0"
                    tvAppCmpCount.text = profile?.appointmentsCompleted ?: "0%"

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
            jsonObject.put("action", "getProfile")
            jsonObject.put("user_id", userId)
            Utility.showLog("jsonObject", "" + jsonObject)

            val request = MultipartBody.Builder()

            request.setType(MultipartBody.FORM)
                .addFormDataPart("action", "getProfile")
                .addFormDataPart("user_id", userId ?: "")
            val requestBody = request.build()

            viewModel.getProfile(requestBody).observe(viewLifecycleOwner, Observer {
                try {
                    Utility.hideLoadingDialog()
                    progressBar.visibility = View.GONE
                    if (it == null)
                        return@Observer
                    if (it.status == "200") {

                        val profile = it.profile

                        tvUserName.setText(profile?.name)
                        tvClinicLocation.setText(profile?.address)
                        tvAppCount.text = profile?.totalAppointments ?: "0"
                        tvSpendsCount.text = profile?.totalSpent ?: "0"
                        tvAppCmpCount.text = profile?.appointmentsCompleted ?: "0%"

                        val requestOptions: RequestOptions = RequestOptions()
                            .placeholder(R.drawable.ic_clinic_doctor)
                            .error(R.drawable.ic_clinic_doctor)
                            .fitCenter()

                        val img = profile?.image
                        Glide.with(ivProfile.context)
                            .load(img)
                            .fitCenter()
                            .apply(requestOptions)
                            .into(ivProfile)
                    } else {
                        Snackbar.make(
                            requireActivity().findViewById(android.R.id.content),
                            it.message ?: getString(R.string.err_msg_oops_something_went_to_wrong),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
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