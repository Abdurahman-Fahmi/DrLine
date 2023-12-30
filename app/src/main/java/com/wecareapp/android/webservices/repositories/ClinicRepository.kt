package com.wecareapp.android.webservices.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wecareapp.android.model.ClientProfileResponse
import com.wecareapp.android.model.ClinicDoctorBalanceDashboardResponse
import com.wecareapp.android.model.DoctorListResponse
import com.wecareapp.android.model.PatientProfileResponse
import com.wecareapp.android.utilities.Utility
import com.wecareapp.android.webservices.RetrofitInstance
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClinicRepository {
    fun getDoctorBalanceDashboard(paramsSignUp: RequestBody): LiveData<ClinicDoctorBalanceDashboardResponse> {
        val mutableLiveData = MutableLiveData<ClinicDoctorBalanceDashboardResponse>()
        RetrofitInstance.create().getDoctorBalanceDashboard(paramsSignUp).enqueue(object :
            Callback<ClinicDoctorBalanceDashboardResponse> {
            override fun onFailure(call: Call<ClinicDoctorBalanceDashboardResponse>, t: Throwable) {
                Utility.showLog("Error", "Server Error : " + t.message)
                mutableLiveData.value = null
            }

            override fun onResponse(
                call: Call<ClinicDoctorBalanceDashboardResponse>,
                response: Response<ClinicDoctorBalanceDashboardResponse>
            ) {
                Utility.showLog("Error", "Repository Response $response")
                if (response.isSuccessful) {
                    mutableLiveData.value = response.body()
                } else {
                    mutableLiveData.value = null
                    Utility.showLog("Error", "Error Else $response")
                }
            }
        })
        return mutableLiveData
    }

    fun getClinicProfile(paramsSignUp: RequestBody): LiveData<ClientProfileResponse> {
        val mutableLiveData = MutableLiveData<ClientProfileResponse>()
        RetrofitInstance.create().getClinicProfile(paramsSignUp).enqueue(object :
            Callback<ClientProfileResponse> {
            override fun onFailure(call: Call<ClientProfileResponse>, t: Throwable) {
                Utility.showLog("Error", "Server Error : " + t.message)
                mutableLiveData.value = null
            }

            override fun onResponse(
                call: Call<ClientProfileResponse>,
                response: Response<ClientProfileResponse>
            ) {
                Utility.showLog("Error", "Repository Response $response")
                if (response.isSuccessful) {
                    mutableLiveData.value = response.body()
                } else {
                    mutableLiveData.value = null
                    Utility.showLog("Error", "Error Else $response")
                }
            }
        })
        return mutableLiveData
    }

}