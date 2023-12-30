package com.wecareapp.android.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.widget.RxTextView
import com.jakewharton.rxbinding2.widget.TextViewTextChangeEvent
import com.wecareapp.android.R
import com.wecareapp.android.adapter.ChatContactAdapter
import com.wecareapp.android.locations.GPSTracker
import com.wecareapp.android.model.InboxItem
import com.wecareapp.android.ui.activity.ChatActivity
import com.wecareapp.android.utilities.Utility
import com.wecareapp.android.webservices.viewmodels.PatientViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_contacts_patient.*
import okhttp3.MultipartBody
import java.util.concurrent.TimeUnit

class PatientContactsFragment : BaseFragment(),
    ChatContactAdapter.Listener {

    private lateinit var viewModel: PatientViewModel
    private var searchKey: String? = null
    private val disposable: CompositeDisposable = CompositeDisposable()
    private var list: MutableList<InboxItem?> = mutableListOf()
    private lateinit var adapter: ChatContactAdapter
    private lateinit var listener: ICallBackToActivity

    companion object {
        fun newInstance(bundle: Bundle, listener: ICallBackToActivity): PatientContactsFragment {
            val fragment = PatientContactsFragment()
            fragment.arguments = bundle
            fragment.setListener(listener)
            return fragment
        }

        fun newInstance(listener: ICallBackToActivity): PatientContactsFragment {
            val fragment = PatientContactsFragment()
            fragment.setListener(listener)
            return fragment
        }

        fun newInstance(bundle: Bundle): PatientContactsFragment {
            val fragment = PatientContactsFragment()
            fragment.arguments = bundle
            return fragment
        }

        fun newInstance(): PatientContactsFragment {
            return PatientContactsFragment()
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
            R.layout.fragment_contacts_patient, container, false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            ivBack.setOnClickListener {
                listener.onBackClicked(true)
            }

            val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            recyclerView.layoutManager = layoutManager

            list.clear()
            adapter = ChatContactAdapter(list, this)
            recyclerView.adapter = adapter

            recyclerView.itemAnimator = DefaultItemAnimator()
            val dividerItemDecoration = DividerItemDecoration(
                recyclerView.context,
                layoutManager.orientation
            )
            recyclerView.addItemDecoration(dividerItemDecoration)
            recyclerView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
                override fun onInterceptTouchEvent(
                    recyclerView: RecyclerView,
                    motionEvent: MotionEvent
                ): Boolean {
                    return false
                }

                override fun onTouchEvent(
                    recyclerView: RecyclerView,
                    motionEvent: MotionEvent
                ) {
                }

                override fun onRequestDisallowInterceptTouchEvent(b: Boolean) {}
            })


            swipeRefresh.setOnRefreshListener {
                callAPIForContacts()
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
        callAPIForContacts()
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
        try {
            gpsTracker = GPSTracker(context)
            list.clear()
            callAPIForContacts()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun callAPIForContacts() {
        try {
            if (!checkingAccess())
                return

            list.clear()

            swipeRefresh.isRefreshing = true

            val request = MultipartBody.Builder()
            request.setType(MultipartBody.FORM)
                .addFormDataPart("action", "loadInbox")
                .addFormDataPart("user_id", userId ?: "")
                .addFormDataPart("customer_type", userCustomerType.toString() ?: "1")
                .addFormDataPart("start", "0")

            if (!TextUtils.isEmpty(searchKey)) {
                searchKey?.let { request.addFormDataPart("search", it) }
            }

            val requestBody = request.build()

            viewModel.getChatList(requestBody).observe(viewLifecycleOwner, Observer {
                Utility.hideLoadingDialog()
                progressBar.visibility = View.GONE
                swipeRefresh.isRefreshing = false
                if (it == null)
                    return@Observer
                if (it.status == "200") {
                    it.inbox?.let { it1 -> list.addAll(it1) }
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun openChat(clinic: InboxItem?) {
        activity?.startActivity(Intent(context, ChatActivity::class.java).apply {
            putExtra("chat", "chat")
            putExtra("contact", clinic)
        })
        activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    override fun onItemSelected(clinic: InboxItem?, adapterPosition: Int) {
        openChat(clinic)
    }
}