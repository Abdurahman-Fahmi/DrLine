package com.wecareapp.android.ui.fragment.patient

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
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
import com.wecareapp.android.adapter.ClinicListAdapter
import com.wecareapp.android.locations.GPSTracker
import com.wecareapp.android.model.Clinic
import com.wecareapp.android.ui.fragment.BaseFragment
import com.wecareapp.android.ui.fragment.ICallBackToActivity
import com.wecareapp.android.utilities.Utility
import com.wecareapp.android.webservices.viewmodels.PatientViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_home_patient_clinic.*
import okhttp3.MultipartBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class PatientHomeClinicsFragment : BaseFragment(),
    ClinicListAdapter.Listener, AdapterView.OnItemSelectedListener {

    private var dataType: String? = null
    private var searchKey: String? = null
    private val disposable: CompositeDisposable = CompositeDisposable()
    private var list: MutableList<Clinic?> = mutableListOf()
    private lateinit var adapter: ClinicListAdapter
    private lateinit var viewModel: PatientViewModel
    private lateinit var listener: ICallBackToActivity

    companion object {
        fun newInstance(bundle: Bundle, listener: ICallBackToActivity): PatientHomeClinicsFragment {
            val fragment = PatientHomeClinicsFragment()
            fragment.arguments = bundle
            fragment.setListener(listener)
            return fragment
        }

        fun newInstance(listener: ICallBackToActivity): PatientHomeClinicsFragment {
            val fragment = PatientHomeClinicsFragment()
            fragment.setListener(listener)
            return fragment
        }

        fun newInstance(bundle: Bundle): PatientHomeClinicsFragment {
            val fragment = PatientHomeClinicsFragment()
            fragment.arguments = bundle
            return fragment
        }

        fun newInstance(): PatientHomeClinicsFragment {
            return PatientHomeClinicsFragment()
        }
    }

    private fun setListener(listener: ICallBackToActivity) {
        this.listener = listener
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
            gpsTracker = GPSTracker(context)

            list.clear()
            setList()
            tvSwitch.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_left, 0)
            tvSwitch.text = getString(R.string.clinics)
            serviceRecyclerView.visibility = View.GONE

            tvSwitch.setOnClickListener {
                openFragment(PatientHomeConsultantsFragment.newInstance(listener))
            }

            loadData()

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

            etSearch.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    listener.hideKeyboard()
                    searchKey = etSearch.text.toString()
                    loadSelectedData()
                    return@setOnEditorActionListener true
                }
                return@setOnEditorActionListener false
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        checkLocationPermission()
        gpsTracker = GPSTracker(context)
    }

    private fun searchData(): DisposableObserver<TextViewTextChangeEvent?>? {
        return object : DisposableObserver<TextViewTextChangeEvent?>() {
            override fun onNext(textViewTextChangeEvent: TextViewTextChangeEvent) {
//                if (checkLocationPermission()) updateLocation()
                searchKey = textViewTextChangeEvent.text().toString().trim { it <= ' ' }
                loadSelectedData()
            }

            override fun onError(e: Throwable) {
//                Log.e(TAG, "onError: " + e.getMessage());
            }

            override fun onComplete() {}
        }
    }

    private fun loadSelectedData() {
        gpsTracker = GPSTracker(context)
        list.clear()
        loadData()
    }

    private fun setList() {
        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        adapter = ClinicListAdapter(list, this)
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
    }

    private fun openFrag() {
        openFragment(PatientHomeClinicDetailsFragment(listener))
    }

    private fun loadData() {
        try {
            if (checkingAccess()) {
                progressBar.visibility = View.VISIBLE

                val jsonObject = JSONObject()
                jsonObject.put("action", "doctors_list")
                jsonObject.put("user_id", userId)
                jsonObject.put("customer_type", userCustomerType)
                Utility.showLog("jsonObject", "" + jsonObject)

                val request = MultipartBody.Builder()
                request.setType(MultipartBody.FORM)
                    .addFormDataPart("action", "clinics_list")
                    .addFormDataPart("user_id", userId + "")
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

                val requestBody = request.build()

                viewModel.getClinicList(requestBody).observe(viewLifecycleOwner, Observer {
                    Utility.hideLoadingDialog()
                    progressBar.visibility = View.GONE
                    if (it == null)
                        return@Observer
                    if (it.status == "200") {
                        //                    list = it.data as MutableList<com.wecareapp.android.model.DataItem?>
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onItemSelected(clinic: Clinic?, adapterPosition: Int) {
        val fragment = PatientHomeClinicDetailsFragment(listener)
        val bundle = Bundle().apply {
            putSerializable("clinic", clinic)
        }
        fragment.arguments = bundle
        openFragment(fragment)
    }

    override fun onStop() {
        super.onStop()
        disposable.clear()
        disposable.dispose()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
        disposable.dispose()
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }
}