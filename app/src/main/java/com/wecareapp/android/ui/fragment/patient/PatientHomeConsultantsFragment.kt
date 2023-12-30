package com.wecareapp.android.ui.fragment.patient

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.widget.RxTextView
import com.jakewharton.rxbinding2.widget.TextViewTextChangeEvent
import com.wecareapp.android.R
import com.wecareapp.android.adapter.DoctorListAdapter
import com.wecareapp.android.adapter.ServiceCategoryListAdapter
import com.wecareapp.android.locations.GPSTracker
import com.wecareapp.android.model.Category
import com.wecareapp.android.model.Doctor
import com.wecareapp.android.ui.fragment.BaseFragment
import com.wecareapp.android.ui.fragment.ICallBackToActivity
import com.wecareapp.android.utilities.Utility
import com.wecareapp.android.webservices.viewmodels.PatientViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_home_patient_clinic.*
import kotlinx.android.synthetic.main.fragment_home_patient_clinic.progressBar
import kotlinx.android.synthetic.main.fragment_home_patient_clinic.recyclerView
import kotlinx.android.synthetic.main.fragment_home_patient_clinic.serviceRecyclerView
import kotlinx.android.synthetic.main.fragment_home_patient_clinic.tvNoData
import kotlinx.android.synthetic.main.fragment_home_patient_clinic_details.*
import okhttp3.MultipartBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class PatientHomeConsultantsFragment : BaseFragment(),
    DoctorListAdapter.Listener, ServiceCategoryListAdapter.Listener {

    private var list: MutableList<Doctor?> = mutableListOf()
    private lateinit var adapter: DoctorListAdapter
    private lateinit var viewModel: PatientViewModel

    private var serviceCategoryList: MutableList<Category?> = mutableListOf()
    private lateinit var serviceCategoryAdapter: ServiceCategoryListAdapter

    private var dataType: String? = null
    private var searchKey: String? = null
    private var categoryId: String? = null
    private val disposable: CompositeDisposable = CompositeDisposable()

    private lateinit var listener: ICallBackToActivity

    private fun setListener(listener: ICallBackToActivity) {
        this.listener = listener
    }

    companion object {
        fun newInstance(
            bundle: Bundle,
            listener: ICallBackToActivity
        ): PatientHomeConsultantsFragment {
            val fragment = PatientHomeConsultantsFragment()
            fragment.arguments = bundle
            fragment.setListener(listener)
            return fragment
        }

        fun newInstance(listener: ICallBackToActivity): PatientHomeConsultantsFragment {
            val fragment = PatientHomeConsultantsFragment()
            fragment.setListener(listener)
            return fragment
        }

        fun newInstance(bundle: Bundle): PatientHomeConsultantsFragment {
            val fragment = PatientHomeConsultantsFragment()
            fragment.arguments = bundle
            return fragment
        }

        fun newInstance(): PatientHomeConsultantsFragment {
            return PatientHomeConsultantsFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(PatientViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_patient_clinic, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            checkLocationPermission()
            gpsTracker = GPSTracker(context)

            list.clear()
            setList()
            tvSwitch.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_arrow_right,
                0,
                0,
                0
            )
            tvSwitch.text = getString(R.string.consultant)
            serviceRecyclerView.visibility = View.VISIBLE

            tvSwitch.setOnClickListener {
                openFragment(PatientHomeClinicsFragment.newInstance(listener))
            }

            ibNearMe.setOnClickListener {
                dataType = "near_me"
                loadSelectedData()
            }

            ibOnline.setOnClickListener {
                dataType = "online"
                loadSelectedData()
            }

            ibTopRated.setOnClickListener {
                dataType = "top_rated"
                loadSelectedData()
            }

            RxTextView.textChangeEvents(etSearch)
                .skipInitialValue()
                .debounce(1000, TimeUnit.MILLISECONDS)
                .filter { textViewTextChangeEvent ->
                    (TextUtils.isEmpty(textViewTextChangeEvent.text().toString())
                            || textViewTextChangeEvent.text().toString().length > 2)
                }
                .distinctUntilChanged()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(searchData())?.let { disposable.add(it) }

            etSearch.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    listener.hideKeyboard()
                    searchKey = etSearch.text.toString()
                    loadSelectedData()
                    return@setOnEditorActionListener true
                }
                return@setOnEditorActionListener false
            }
            getCategoriesList()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        gpsTracker = GPSTracker(context)
        checkLocationPermission()
    }

    private fun searchData(): DisposableObserver<TextViewTextChangeEvent?>? {
        return object : DisposableObserver<TextViewTextChangeEvent?>() {
            override fun onNext(textViewTextChangeEvent: TextViewTextChangeEvent) {
                try {
                    //                if (checkLocationPermission()) updateLocation()
                    searchKey = textViewTextChangeEvent.text().toString().trim { it <= ' ' }
                    loadSelectedData()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onError(e: Throwable) {
//                Log.e(TAG, "onError: " + e.getMessage());
            }

            override fun onComplete() {}
        }
    }

    private fun loadSelectedData() {
        try {
            gpsTracker = GPSTracker(context)
            list.clear()
            loadData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setList() {
        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        adapter = DoctorListAdapter(list, this, true)
        recyclerView.adapter = adapter
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.addOnItemTouchListener(object : OnItemTouchListener {
            override fun onInterceptTouchEvent(
                recyclerView: RecyclerView,
                motionEvent: MotionEvent
            ): Boolean {
//                openFrag()
                return false
            }

            override fun onTouchEvent(
                recyclerView: RecyclerView,
                motionEvent: MotionEvent
            ) {
            }

            override fun onRequestDisallowInterceptTouchEvent(b: Boolean) {}
        })

        serviceRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        serviceCategoryAdapter = ServiceCategoryListAdapter(serviceCategoryList, this)
        serviceRecyclerView.adapter = serviceCategoryAdapter
    }

    private fun openFrag() {
        openFragment(PatientHomeConsultantProfileFragment.newInstance(listener))
    }

    private fun loadData() {
        if (checkingAccess()) {
            progressBar.visibility = View.VISIBLE
            val jsonObject = JSONObject()
            jsonObject.put("action", "doctors_list")
            jsonObject.put("user_id", userId)
            jsonObject.put("customer_type", userCustomerType)
            Utility.showLog("jsonObject", "" + jsonObject)

            val request = MultipartBody.Builder()

            request.setType(MultipartBody.FORM)
                .addFormDataPart("action", "doctors_list")
                .addFormDataPart("user_id", userId.toString())
                .addFormDataPart("customer_type", userCustomerType.toString())

            when (dataType) {
                "near_me" -> {
                    request.addFormDataPart("user_latitude", gpsTracker.latitude.toString())
                    request.addFormDataPart("user_longitude", gpsTracker.longitude.toString())
                    request.addFormDataPart("latitude", gpsTracker.latitude.toString())
                    request.addFormDataPart("longitude", gpsTracker.longitude.toString())
                }
                "online" -> {
                    request.addFormDataPart("available_status", "1")
                }
                "top_rated" -> {
                    request.addFormDataPart("top_rated", "1")
                    request.addFormDataPart("rating", "1")
                }
            }

            if (!TextUtils.isEmpty(searchKey)) {
                searchKey?.let { request.addFormDataPart("search", it) }
            }

            if (!TextUtils.isEmpty(categoryId)) {
                categoryId?.let { request.addFormDataPart("category_id", it) }
            }

            val requestBody = request.build()

            viewModel.getDoctorList(requestBody).observe(viewLifecycleOwner, Observer {
                Utility.hideLoadingDialog()
                progressBar.visibility = View.GONE
                if (it == null)
                    return@Observer
                if (it.status == "200") {
                    it.data?.let { it1 -> list.addAll(it1) }
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

    private fun getCategoriesList() {
        if (checkingAccess()) {
            serviceCategoryList.clear()
            progressBar.visibility = View.VISIBLE

            val jsonObject = JSONObject()
            jsonObject.put("action", "getCategoriesList")
            jsonObject.put("user_id", userId)
            jsonObject.put("customer_type", userCustomerType)
            Utility.showLog("jsonObject", "" + jsonObject)

            val request = MultipartBody.Builder()

            request.setType(MultipartBody.FORM)
                .addFormDataPart("action", "getCategoriesList")
                .addFormDataPart("customer_type", "1")

            if (!TextUtils.isEmpty(userId))
                request.addFormDataPart("user_id", userId ?: "")

            val requestBody = request.build()

            viewModel.getCategoriesList(requestBody).observe(viewLifecycleOwner, Observer {
                Utility.hideLoadingDialog()
                progressBar.visibility = View.GONE
                if (it == null)
                    return@Observer
                if (it.status == "200") {
                    serviceCategoryList.clear()
                    it.data?.let { it1 -> serviceCategoryList.addAll(it1) }
                    if (serviceCategoryList.isEmpty()) {
                        tvNoData.visibility = View.VISIBLE
                        recyclerView.visibility = View.INVISIBLE
                        serviceRecyclerView.visibility = View.INVISIBLE
                    } else {
                        tvNoData.visibility = View.GONE
                        serviceRecyclerView.visibility = View.VISIBLE
                        serviceCategoryAdapter.notifyDataSetChanged()
                        loadData()
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
        val fragment = PatientHomeConsultantProfileFragment.newInstance(listener)
        val bundle = Bundle().apply {
            putSerializable("doctor", doctor)
        }
        fragment.arguments = bundle
        openFragment(fragment)
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

    override fun onStop() {
        super.onStop()
        disposable.clear()
        disposable.dispose()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }

    override fun onItemSelected(category: Category?, adapterPosition: Int) {
        for (category in serviceCategoryList) {
            category?.selected = false
        }
        category?.selected = true
        serviceCategoryAdapter.notifyDataSetChanged()
        categoryId = category?.categoryId
        list.clear()
        loadData()
    }
}