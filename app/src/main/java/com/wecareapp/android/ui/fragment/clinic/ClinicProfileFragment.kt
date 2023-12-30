package com.wecareapp.android.ui.fragment.clinic

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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.wecareapp.android.adapter.DoctorListAdapter
import com.wecareapp.android.adapter.ServiceCategoryListAdapter
import com.wecareapp.android.managers.LocaleManager
import com.wecareapp.android.model.Category
import com.wecareapp.android.model.Clinic
import com.wecareapp.android.model.ClinicProfile
import com.wecareapp.android.model.Doctor
import com.wecareapp.android.ui.activity.AddServiceCategoryActivity
import com.wecareapp.android.ui.activity.ConsultantReviewsActivity
import com.wecareapp.android.ui.activity.SplashActivity
import com.wecareapp.android.ui.activity.clinic.AddDoctorActivity
import com.wecareapp.android.ui.fragment.BaseFragment
import com.wecareapp.android.ui.fragment.ICallBackToActivity
import com.wecareapp.android.ui.fragment.consultant.ConsultantProfileFragment
import com.wecareapp.android.ui.fragment.patient.PatientHomeConsultantAppointmentSelectionFragment
import com.wecareapp.android.utilities.FileUtils2
import com.wecareapp.android.utilities.Utility
import com.wecareapp.android.webservices.viewmodels.ClinicViewModel
import com.wecareapp.android.webservices.viewmodels.PatientViewModel
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.android.synthetic.main.fragment_clinic_profile.*
import kotlinx.android.synthetic.main.fragment_clinic_profile.ivAddPicture
import kotlinx.android.synthetic.main.fragment_clinic_profile.ivLogout
import kotlinx.android.synthetic.main.fragment_clinic_profile.ivProfile
import kotlinx.android.synthetic.main.fragment_clinic_profile.ivSettings
import kotlinx.android.synthetic.main.fragment_clinic_profile.progressBar
import kotlinx.android.synthetic.main.fragment_clinic_profile.tvClinicLocation
import kotlinx.android.synthetic.main.fragment_clinic_profile.tvLanguage
import kotlinx.android.synthetic.main.fragment_patient_profile.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.File
import java.util.*


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class ClinicProfileFragment : BaseFragment(),
    View.OnClickListener, DoctorListAdapter.Listener, ServiceCategoryListAdapter.Listener {

    private var profile: ClinicProfile? = null
    private lateinit var viewModel: PatientViewModel
    private var list: MutableList<Doctor?> = mutableListOf()
    private var serviceCategoryList: MutableList<Category?> = mutableListOf()
    private var serviceList: MutableList<Doctor?> = mutableListOf()
    private lateinit var adapter: DoctorListAdapter
    private lateinit var serviceCategoryAdapter: ServiceCategoryListAdapter
    private var clinic: Clinic? = null
    private var editMode: Boolean = false

    private var dialog: Dialog? = null
    private lateinit var selectedImageUri: Uri
    private lateinit var selectedImageUri2: Uri
    private lateinit var selectedDocumentUri: Uri
    private var selectedDocumentPath: String? = null
    private var selectedImagePath: String? = null
    private var selectedImagePath2: String? = null
    private var imageBitmap: Bitmap? = null
    private var imageBitmap2: Bitmap? = null
    private var isImageSelected = false
    private var selectedBg = false

    private lateinit var listener: ICallBackToActivity

    companion object {
        fun newInstance(
            bundle: Bundle,
            listener: ICallBackToActivity
        ): ClinicProfileFragment {
            val fragment = ClinicProfileFragment()
            fragment.arguments = bundle
            fragment.setListener(listener)
            return fragment
        }

        fun newInstance(listener: ICallBackToActivity): ClinicProfileFragment {
            val fragment = ClinicProfileFragment()
            fragment.setListener(listener)
            return fragment
        }

        fun newInstance(bundle: Bundle): ClinicProfileFragment {
            val fragment = ClinicProfileFragment()
            fragment.arguments = bundle
            return fragment
        }

        fun newInstance(): ClinicProfileFragment {
            return ClinicProfileFragment()
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
        return inflater.inflate(
            R.layout.fragment_clinic_profile, container, false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            try {
                ivLogout.setOnClickListener(this)
                ivMap.setOnClickListener(this)
                fabAdd.setOnClickListener(this)
                ivSettings.setOnClickListener(this)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            recyclerView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = DoctorListAdapter(list, this, false)
            recyclerView.adapter = adapter

            serviceRecyclerView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            serviceCategoryAdapter = ServiceCategoryListAdapter(serviceCategoryList, this)
            serviceRecyclerView.adapter = serviceCategoryAdapter

            refreshData()

            ratingBar.visibility = View.GONE
            ratingBar.setOnTouchListener(View.OnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    activity?.startActivity(Intent(context, ConsultantReviewsActivity::class.java))
                }
                return@OnTouchListener true
            })

            ivMap.setOnClickListener {
//                val url =
//                    "http://maps.google.com/maps?daddr=" + latLng.latitude.toString() + "," + latLng.
                val url = ""
//                    "http://maps.google.com/maps?daddr=" + "17.9582275" + "," + "79.5691676"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            }

            swipeRefresh.setOnRefreshListener {
                refreshData()
            }

            ivProfile.setOnClickListener {
                Dexter.withActivity(activity)
                    .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    .withListener(object : MultiplePermissionsListener {
                        override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                            if (report.areAllPermissionsGranted()) {
                                selectedBg = false
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

            ivAddBackCover.setOnClickListener {
                Dexter.withActivity(activity)
                    .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    .withListener(object : MultiplePermissionsListener {
                        override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                            if (report.areAllPermissionsGranted()) {
                                selectedBg = true
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

    private fun refreshData() {
        list.clear()
        serviceCategoryList.clear()
        loadData()
        getCategoriesList()
//        getDoctorsList(null)
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
            R.id.fabAdd -> {
                openClinicBalance()
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
        tvClinicName.isClickable = false
        tvClinicName.isEnabled = false
        ivProfile.isClickable = false
        ivProfile.isEnabled = false
        tvClinicLocation.isClickable = false
        tvClinicLocation.isEnabled = false
        ivAddPicture.visibility = View.GONE
        ivAddBackCover.visibility = View.GONE
        ivSettings.setBackgroundResource(R.drawable.ic_settings_clinic)

        updateProfile()
    }

    private fun editProfile() {
        editMode = true
        tvClinicName.isClickable = true
        tvClinicName.isEnabled = true
        ivProfile.isClickable = true
        ivProfile.isEnabled = true
        tvClinicLocation.isClickable = true
        tvClinicLocation.isEnabled = true
        ivAddPicture.visibility = View.VISIBLE
        ivAddBackCover.visibility = View.VISIBLE
        ivSettings.setBackgroundResource(R.drawable.ic_done_clinic_profile)
    }

    private fun openClinicBalance() {
        startActivity(Intent(context, AddDoctorActivity::class.java))
        activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
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
                .addFormDataPart("customer_type", userCustomerType.toString())
                .addFormDataPart("user_id", userId ?: "")
                .addFormDataPart("user_name", tvClinicName.text.toString())
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
            if (selectedImagePath2 != null) {
                val imageFile = File(selectedImagePath2)
                val requestIImageFile: RequestBody =
                    RequestBody.create("multipart/form-data".toMediaTypeOrNull(), imageFile)
                request.addFormDataPart(
                    "clinic_2nd_image",
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
//                    tvAppCount.text = profile?.totalAppointments ?: "0"
//                    tvSpendsCount.text = profile?.totalSpent ?: "0"
//                    tvAppCmpCount.text = profile?.appointmentsCompleted ?: "0%"

                    /*val requestOptions: RequestOptions = RequestOptions()
                        .placeholder(R.drawable.ic_hospital_puple)
                        .error(R.drawable.ic_hospital_puple)
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

            val viewModel = ViewModelProvider(this).get(ClinicViewModel::class.java)
            viewModel.getClinicProfile(requestBody).observe(viewLifecycleOwner, Observer {
                swipeRefresh.isRefreshing = false
                Utility.hideLoadingDialog()
                progressBar.visibility = View.GONE
                if (it == null)
                    return@Observer
                if (it.status == "200") {

                    profile = it.profile

                    tvClinicName.setText(profile?.name)
                    tvClinicLocation.setText(profile?.address)

                    ratingBar.rating = profile?.rating?.toFloat() ?: 0F

                    val requestOptions: RequestOptions = RequestOptions()
                        .placeholder(R.drawable.ic_hospital_puple)
                        .error(R.drawable.ic_hospital_puple)
                        .fitCenter()

                    val img = profile?.image
                    Glide.with(ivProfile.context)
                        .load(img)
                        .fitCenter()
                        .apply(requestOptions)
                        .into(ivProfile)

                    val requestOptions2: RequestOptions = RequestOptions()
                        .placeholder(R.drawable.temp_01)
                        .error(R.drawable.temp_01)
                        .fitCenter()

                    val img2 = profile?.clinic2ndImage
                    Glide.with(ivProfile.context)
                        .load(img2)
                        .fitCenter()
                        .apply(requestOptions2)
                        .into(ivBackCover)
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

            viewModel.getCategoriesList(requestBody).observe(viewLifecycleOwner, Observer {
                Utility.hideLoadingDialog()
                progressBar.visibility = View.GONE
                if (it == null)
                    return@Observer
                if (it.status == "200") {
                    it.data?.let { it1 -> serviceCategoryList.addAll(it1) }
                    serviceCategoryList.add(null)
                    if (serviceCategoryList.isEmpty()) {
                        tvNoData.visibility = View.VISIBLE
                        recyclerView.visibility = View.INVISIBLE
                        serviceRecyclerView.visibility = View.INVISIBLE
                    } else {
                        tvNoData.visibility = View.GONE
                        recyclerView.visibility = View.GONE
                        serviceRecyclerView.visibility = View.VISIBLE
                        serviceCategoryAdapter.notifyDataSetChanged()
                        getDoctorsList(serviceCategoryList[0])
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

    private fun getDoctorsList(category: Category?) {
        category?.selected = true
        serviceCategoryAdapter.notifyDataSetChanged()

        if (checkingAccess()) {
            progressBar.visibility = View.VISIBLE

            val jsonObject = JSONObject()
            jsonObject.put("action", "getClinicDoctorsByCategory1")
            jsonObject.put("user_id", userId ?: "")
            jsonObject.put("customer_type", "3")
            Utility.showLog("jsonObject", "" + jsonObject)

            val request = MultipartBody.Builder()

            request.setType(MultipartBody.FORM)
                .addFormDataPart("action", "getClinicDoctorsByCategory1")
                .addFormDataPart("customer_type", "3")
                .addFormDataPart("start", "0")


            if (!TextUtils.isEmpty(userId))
                request.addFormDataPart("user_id", userId ?: "")

            if (category != null)
                request.addFormDataPart("category_id", category.categoryId ?: "")

            val requestBody = request.build()

            viewModel.getClinicDoctorsByCategory(requestBody).observe(viewLifecycleOwner, Observer {
                Utility.hideLoadingDialog()
                progressBar.visibility = View.GONE
                if (it == null)
                    return@Observer
                if (it.status == "200") {
                    it.doctors?.let { it1 -> list.addAll(it1) }
                    if (list.isEmpty()) {
                        tvNoData.visibility = View.VISIBLE
                        recyclerView.visibility = View.INVISIBLE
                    } else {
                        tvNoData.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        adapter.notifyDataSetChanged()
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

    override fun onItemSelected(doctor: Doctor?, adapterPosition: Int) {
        val fragment = ConsultantProfileFragment(listener)
        val bundle = Bundle().apply {
            putSerializable("doctor", doctor)
        }
        fragment.arguments = bundle
//        openFragment(fragment)
    }

    override fun bookAppointment(doctor: Doctor?, adapterPosition: Int) {
        if (!isLogged) {
            openLoginActivity()
            return
        }
        val fragment = PatientHomeConsultantAppointmentSelectionFragment(listener)
        fragment.arguments = Bundle().apply {
            putSerializable("doctor", doctor)
        }
        openFragment(fragment)
    }

    override fun onItemSelected(category: Category?, adapterPosition: Int) {
        if (category == null) {
            startActivityForResult(Intent(context, AddServiceCategoryActivity::class.java), 200)
            activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        } else {
            list.clear()

//            selectedCategory = (category)

            for (category in serviceCategoryList) {
                category?.selected = false
            }

            getDoctorsList(category)
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
//        if (requestCode == 200) {
//            if (resultCode === Activity.RESULT_OK) {
//                refreshData()
//            }
//        }

        if (resultCode != Activity.RESULT_CANCELED) {
            when (requestCode) {
                REQUEST_CAMERA -> if (resultCode == Activity.RESULT_OK && data != null) {
                    val selectedImage = data.extras!!["data"] as Bitmap?
                    val selectedImgUri = Utility.getImageUri(context, selectedImage)
                    if (selectedImage != null) {
                        if (selectedBg) {
                            isImageSelected = true
                            imageBitmap2 = selectedImage
//                        ivProfile.setImageBitmap(selectedImage)

                            selectedImagePath2 =
                                FileUtils2.getRealPath(requireContext(), selectedImgUri) ?: ""
                            selectedImageUri2 = selectedImgUri!!
                            imageBitmap2 =
                                MediaStore.Images.Media.getBitmap(
                                    activity?.contentResolver,
                                    selectedImageUri2
                                )

                            Glide.with(this).asBitmap()
                                .load(imageBitmap2)
                                .into(ivBackCover)
                        } else {
                            isImageSelected = true
                            imageBitmap = selectedImage
//                        ivProfile.setImageBitmap(selectedImage)

                            selectedImagePath =
                                FileUtils2.getRealPath(requireContext(), selectedImgUri) ?: ""
                            selectedImageUri = selectedImgUri!!
                            imageBitmap =
                                MediaStore.Images.Media.getBitmap(
                                    activity?.contentResolver,
                                    selectedImageUri
                                )

                            Glide.with(this).asBitmap()
                                .load(imageBitmap)
                                .into(ivProfile);
                        }
                    }
                }
                REQUEST_GALLERY -> if (resultCode == Activity.RESULT_OK && data != null) {
                    val selectedImgUri = data.data
                    Utility.showLog("selectedImageUri", "selectedImageUri : $selectedImgUri")
                    if (selectedBg) {
                        selectedImagePath2 =
                            FileUtils2.getRealPath(requireContext(), selectedImgUri) ?: ""
                        selectedImageUri2 = selectedImgUri!!
                        imageBitmap2 =
                            MediaStore.Images.Media.getBitmap(
                                activity?.contentResolver,
                                selectedImageUri2
                            )

                        if (imageBitmap2 != null) {
                            isImageSelected = true
//                        ivProfile.setImageBitmap(imageBitmap)

                            Glide.with(this).asBitmap()
                                .load(imageBitmap2)
                                .into(ivBackCover);
                        }
                    } else {
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