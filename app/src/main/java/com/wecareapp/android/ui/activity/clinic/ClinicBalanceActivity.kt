package com.wecareapp.android.ui.activity.clinic

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.Legend.LegendForm
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.components.YAxis.YAxisLabelPosition
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.snackbar.Snackbar
import com.wecareapp.android.R
import com.wecareapp.android.adapter.ClinicAppointmentDoctorListAdapter
import com.wecareapp.android.adapter.PaymentAdapter
import com.wecareapp.android.component.DayAxisValueFormatter
import com.wecareapp.android.component.MyAxisValueFormatter
import com.wecareapp.android.component.XYMarkerView
import com.wecareapp.android.model.Appointment
import com.wecareapp.android.model.Doctor
import com.wecareapp.android.ui.activity.BaseActivity
import com.wecareapp.android.ui.activity.CashoutActivity
import com.wecareapp.android.ui.activity.ProfileBoostActivity
import com.wecareapp.android.utilities.Utility
import com.wecareapp.android.webservices.viewmodels.ClinicViewModel
import com.wecareapp.android.webservices.viewmodels.PatientViewModel
import kotlinx.android.synthetic.main.activity_clinic_balance.*
import kotlinx.android.synthetic.main.activity_clinic_balance.ivBack
import kotlinx.android.synthetic.main.activity_clinic_balance.ivDocList
import kotlinx.android.synthetic.main.activity_clinic_balance.ivProfile
import kotlinx.android.synthetic.main.activity_clinic_balance.progressBar
import kotlinx.android.synthetic.main.activity_clinic_balance.recyclerView
import kotlinx.android.synthetic.main.activity_clinic_balance.rvDoctors
import kotlinx.android.synthetic.main.activity_clinic_balance.swipeRefresh
import kotlinx.android.synthetic.main.activity_clinic_balance.tvNoData
import kotlinx.android.synthetic.main.fragment_consultant_my_appointments.*
import kotlinx.android.synthetic.main.fragment_consultant_profile.*
import okhttp3.MultipartBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class ClinicBalanceActivity : BaseActivity(), ClinicAppointmentDoctorListAdapter.Listener,
    PaymentAdapter.Listener, OnChartValueSelectedListener {
    private lateinit var adapter: PaymentAdapter
    private lateinit var adapterDoctors: ClinicAppointmentDoctorListAdapter
    private var list: MutableList<Appointment?> = mutableListOf()
    private var listDoctors: MutableList<Doctor?> = mutableListOf()
    private var showDocs = false
    private var doctor: Doctor? = null
    private lateinit var viewModel: PatientViewModel

    protected var tfRegular: Typeface? = null
    protected var tfLight: Typeface? = null

    protected val months = arrayOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dec"
    )

    protected val parties = arrayOf(
        "Party A", "Party B", "Party C", "Party D", "Party E", "Party F", "Party G", "Party H",
        "Party I", "Party J", "Party K", "Party L", "Party M", "Party N", "Party O", "Party P",
        "Party Q", "Party R", "Party S", "Party T", "Party U", "Party V", "Party W", "Party X",
        "Party Y", "Party Z"
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clinic_balance)

        viewModel = ViewModelProvider(this).get(PatientViewModel::class.java)


        tfRegular = Typeface.createFromAsset(assets, "OpenSans-Regular.ttf");
        tfLight = Typeface.createFromAsset(assets, "OpenSans-Light.ttf");

        ivBack.setOnClickListener {
            onBackPressed()
        }

        ivCashOut.setOnClickListener {
            startActivity(Intent(this, CashoutActivity::class.java).apply {
                putExtra(
                    "doctor",
                    doctor
                )
            })
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = PaymentAdapter(list, this)
        recyclerView.adapter = adapter

        rvDoctors.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapterDoctors = ClinicAppointmentDoctorListAdapter(listDoctors, this)
        rvDoctors.adapter = adapterDoctors

        ivProfile.setOnClickListener {
            showDocs = !showDocs
            if (showDocs) {
                ivDocList.setImageResource(R.drawable.ic_arrow_up_clinic_doc)
                rvDoctors.visibility = View.VISIBLE
            } else {
                ivDocList.setImageResource(R.drawable.ic_arrow_down_clinic_doc)
                rvDoctors.visibility = View.GONE
            }
        }

        loadData()
        swipeRefresh.setOnRefreshListener {
            loadData()
        }

        chart.setOnChartValueSelectedListener(this)

        chart.setDrawBarShadow(false)
        chart.setDrawValueAboveBar(true)

        chart.description.isEnabled = false

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        chart.setMaxVisibleValueCount(60)

        // scaling can now only be done on x- and y-axis separately

        // scaling can now only be done on x- and y-axis separately
        chart.setPinchZoom(false)

        chart.setDrawGridBackground(false)
        // chart.setDrawYLabels(false);

        // chart.setDrawYLabels(false);
        val xAxisFormatter = DayAxisValueFormatter(chart)

        val xAxis = chart.xAxis
        xAxis.position = XAxisPosition.BOTTOM
        xAxis.typeface = tfLight
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 0f // only intervals of 1 day

        xAxis.labelCount = 7
        xAxis.valueFormatter = xAxisFormatter

        val custom = MyAxisValueFormatter()

        val leftAxis = chart.axisLeft
        leftAxis.typeface = tfLight
        leftAxis.setLabelCount(8, false)
        leftAxis.valueFormatter = custom
        leftAxis.setPosition(YAxisLabelPosition.OUTSIDE_CHART)
        leftAxis.spaceTop = 15f
        leftAxis.axisMinimum = 0f // this replaces setStartAtZero(true)


        val rightAxis = chart.axisRight
        rightAxis.setDrawGridLines(false)
        rightAxis.typeface = tfLight
        rightAxis.setLabelCount(8, false)
        rightAxis.valueFormatter = custom
        rightAxis.spaceTop = 15f
        rightAxis.axisMinimum = 0f // this replaces setStartAtZero(true)


        val l = chart.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.setDrawInside(false)
        l.form = LegendForm.SQUARE
        l.formSize = 9f
        l.textSize = 10f
        l.xEntrySpace = 4f

//        val mv = XYMarkerView(this, xAxisFormatter)
//        mv.chartView = chart // For bounds control
//
//        chart.marker = mv // Set the marker to the chart


        // setting data

        // setting data
//        seekBarY.setProgress(50)
//        seekBarX.setProgress(12)

        // chart.setDrawLegend(false);

        setData(12, 30f)
    }

    private fun setData(count: Int, range: Float) {
        val start = 1f
        val values: ArrayList<BarEntry> = ArrayList()
        var i = start.toFloat()
        while (i < start + count) {
            val `val` = (Math.random() * (range + 1)).toFloat()
            if (Math.random() * 100 < 25) {
                values.add(BarEntry(i, `val`, resources.getDrawable(R.drawable.star)))
            } else {
                values.add(BarEntry(i, `val`))
            }
            i++
        }
        val set1: BarDataSet
        if (chart.data != null &&
            chart.data.dataSetCount > 0
        ) {
            set1 = chart.data.getDataSetByIndex(0) as BarDataSet
            set1.values = values
            chart.data.notifyDataChanged()
            chart.notifyDataSetChanged()
        } else {
            set1 = BarDataSet(values, "")
//            set1.setDrawIcons(false)
            val startColor1 = ContextCompat.getColor(this, android.R.color.holo_orange_light)
            val startColor2 = ContextCompat.getColor(this, android.R.color.holo_blue_light)
            val startColor3 = ContextCompat.getColor(this, android.R.color.holo_orange_light)
            val startColor4 = ContextCompat.getColor(this, android.R.color.holo_green_light)
            val startColor5 = ContextCompat.getColor(this, android.R.color.holo_red_light)
            val endColor1 = ContextCompat.getColor(this, android.R.color.holo_blue_dark)
            val endColor2 = ContextCompat.getColor(this, android.R.color.holo_purple)
            val endColor3 = ContextCompat.getColor(this, android.R.color.holo_green_dark)
            val endColor4 = ContextCompat.getColor(this, android.R.color.holo_red_dark)
            val endColor5 = ContextCompat.getColor(this, android.R.color.holo_orange_dark)
            /*val gradientFills: MutableList<Fill> = ArrayList()
            gradientFills.add(Fill(startColor1, endColor1))
            gradientFills.add(Fill(startColor2, endColor2))
            gradientFills.add(Fill(startColor3, endColor3))
            gradientFills.add(Fill(startColor4, endColor4))
            gradientFills.add(Fill(startColor5, endColor5))
            set1.setFills(gradientFills)*/
            val dataSets: ArrayList<IBarDataSet> = ArrayList()
            dataSets.add(set1)
            val data = BarData(dataSets)
            data.setValueTextSize(10f)
            data.setValueTypeface(tfLight)
            data.barWidth = 0.9f
            chart.data = data
        }
    }

    private fun loadData() {
        list.clear()
        listDoctors.clear()
        getDoctorsList()
    }

    private fun getDoctorsList() {
        if (checkingAccess()) {
            progressBar.visibility = View.VISIBLE
            val jsonObject = JSONObject()
            jsonObject.put("action", "doctors_list")
            jsonObject.put("user_id", userId)
            jsonObject.put("customer_type", "1")
            Utility.showLog("jsonObject", "" + jsonObject)

            val request = MultipartBody.Builder()

            request.setType(MultipartBody.FORM)
                .addFormDataPart("action", "doctors_list")
                .addFormDataPart("user_id", userId.toString())
                .addFormDataPart("customer_type", userCustomerType.toString())
            val requestBody = request.build()

            viewModel.getDoctorList(requestBody).observe(this, Observer {
                Utility.hideLoadingDialog()
                progressBar.visibility = View.GONE
                if (it == null)
                    return@Observer
                if (it.status == "200") {
                    it.data?.let { it1 -> listDoctors.addAll(it1) }
                    adapterDoctors.notifyDataSetChanged()

                    doctor = if (it.data?.size!! > 0) it.data[0] else null
                    setDoctorProfile(doctor)
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

    private fun setDoctorProfile(doctor: Doctor?) {
        getDoctorBalanceDashboard(doctor)

        val requestOptions: RequestOptions = RequestOptions()
            .placeholder(R.drawable.ic_clinic_doctor)
            .error(R.drawable.ic_clinic_doctor)
            .fitCenter()

        val img = doctor?.image
        Glide.with(ivProfile.context)
            .load(img)
            .fitCenter()
            .apply(requestOptions)
            .into(ivProfile)
    }

    private fun getDoctorBalanceDashboard(doctor: Doctor?) {
        if (!checkingAccess())
            return
        swipeRefresh.isRefreshing = true

        val request = MultipartBody.Builder()
        request.setType(MultipartBody.FORM)
            .addFormDataPart("action", "getDoctorBalanceDashboard")
            .addFormDataPart("user_id", userId.toString())
            .addFormDataPart("clinic_id", userId.toString())
            .addFormDataPart("doctor_id", doctor?.doctorId.toString())
            .addFormDataPart("start", "0")

        val requestBody = request.build()

        val viewModel = ViewModelProvider(this).get(ClinicViewModel::class.java)
        viewModel.getDoctorBalanceDashboard(requestBody).observe(this, Observer {
            Utility.hideLoadingDialog()
            progressBar.visibility = View.GONE
            swipeRefresh.isRefreshing = false
            if (it == null)
                return@Observer
            if (it.stauts == "200") {
                tvWalletBalance.text = it.currency + " " + (it.wallet ?: "0")
                adapter.currency = it.currency ?: "SAR"
                it.recentAppointments?.let { it1 -> list.addAll(it1) }
                if (list.isEmpty()) {
                    tvNoData.visibility = View.VISIBLE
                    recyclerView.visibility = View.INVISIBLE
                } else {
                    tvNoData.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    adapter.notifyDataSetChanged()
                }

                val ms = it?.membershipExpiry

                tvProfileBoost.setOnClickListener {
                    try {
                        var expired = true
                        val sdf = SimpleDateFormat("yyyy-MM-dd")
                        val strDate = sdf.parse(ms)
                        if (Date().after(strDate)) {
                            expired = false
                        }
                        if (expired) {
                            startActivity(Intent(this, ProfileBoostActivity::class.java))
                            overridePendingTransition(
                                R.anim.slide_in_right,
                                R.anim.slide_out_left
                            )
                        } else {
                            Snackbar.make(
                                this.findViewById(android.R.id.content),
                                String.format(
                                    getString(R.string.msg_membership_expiry),
                                    ms
                                ),
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                ivVideoCall.setOnClickListener {
                    try {
                        var expired = true
                        val sdf = SimpleDateFormat("yyyy-MM-dd")
                        val strDate = sdf.parse(ms)
                        if (Date().after(strDate)) {
                            expired = false
                        }
                        if (expired) {
                            startActivity(Intent(this, ProfileBoostActivity::class.java))
                            overridePendingTransition(
                                R.anim.slide_in_right,
                                R.anim.slide_out_left
                            )
                        } else {
                            Snackbar.make(
                                this.findViewById(android.R.id.content),
                                String.format(
                                    getString(R.string.msg_membership_expiry),
                                    ms
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
                    findViewById(android.R.id.content),
                    it.message ?: getString(R.string.err_msg_oops_something_went_to_wrong),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        })
    }

    override fun onItemSelected(category: Doctor?, adapterPosition: Int) {
        doctor = category
        list.clear()
        ivDocList.setImageResource(R.drawable.ic_arrow_down_clinic_doc)
        rvDoctors.visibility = View.GONE
        setDoctorProfile(doctor)
    }

    override fun onItemSelected(clinic: Appointment?, adapterPosition: Int) {

    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {

    }

    override fun onNothingSelected() {

    }
}