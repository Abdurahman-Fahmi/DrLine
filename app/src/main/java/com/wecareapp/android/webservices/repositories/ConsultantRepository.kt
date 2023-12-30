package com.wecareapp.android.webservices.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wecareapp.android.model.*
import com.wecareapp.android.utilities.Utility
import com.wecareapp.android.webservices.RetrofitInstance
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ConsultantRepository {
    fun getConsultantProfile(paramsSignUp: RequestBody): LiveData<ConsultantProfileResponse> {
        val mutableLiveData = MutableLiveData<ConsultantProfileResponse>()
        RetrofitInstance.create().getConsultantProfile(paramsSignUp).enqueue(object :
            Callback<ConsultantProfileResponse> {
            override fun onFailure(call: Call<ConsultantProfileResponse>, t: Throwable) {
                Utility.showLog("Error", "Server Error : " + t.message)
                mutableLiveData.value = null
            }

            override fun onResponse(
                call: Call<ConsultantProfileResponse>,
                response: Response<ConsultantProfileResponse>
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

    fun getRatings(paramsSignUp: RequestBody): LiveData<RatingResponse> {
        val mutableLiveData = MutableLiveData<RatingResponse>()
        RetrofitInstance.create().getRatings(paramsSignUp).enqueue(object :
            Callback<RatingResponse> {
            override fun onFailure(call: Call<RatingResponse>, t: Throwable) {
                Utility.showLog("Error", "Server Error : " + t.message)
                mutableLiveData.value = null
            }

            override fun onResponse(
                call: Call<RatingResponse>,
                response: Response<RatingResponse>
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

    fun getPlans(paramsSignUp: RequestBody): LiveData<PlansResponse> {
        val mutableLiveData = MutableLiveData<PlansResponse>()
        RetrofitInstance.create().getPlans(paramsSignUp).enqueue(object :
            Callback<PlansResponse> {
            override fun onFailure(call: Call<PlansResponse>, t: Throwable) {
                Utility.showLog("Error", "Server Error : " + t.message)
                mutableLiveData.value = null
            }

            override fun onResponse(
                call: Call<PlansResponse>,
                response: Response<PlansResponse>
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

    fun getClinicList(paramsSignUp: RequestBody): LiveData<ClinicListResponse> {
        val mutableLiveData = MutableLiveData<ClinicListResponse>()
        RetrofitInstance.create().getClinicList(paramsSignUp).enqueue(object :
            Callback<ClinicListResponse> {
            override fun onFailure(call: Call<ClinicListResponse>, t: Throwable) {
                Utility.showLog("Error", "Server Error : " + t.message)
                mutableLiveData.value = null
            }

            override fun onResponse(
                call: Call<ClinicListResponse>,
                response: Response<ClinicListResponse>
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

    fun getDoctorDetails(paramsSignUp: RequestBody): LiveData<DoctorDetailsResponse> {
        val mutableLiveData = MutableLiveData<DoctorDetailsResponse>()
        RetrofitInstance.create().getDoctorDetails(paramsSignUp).enqueue(object :
            Callback<DoctorDetailsResponse> {
            override fun onFailure(call: Call<DoctorDetailsResponse>, t: Throwable) {
                Utility.showLog("Error", "Server Error : " + t.message)
                mutableLiveData.value = null
            }

            override fun onResponse(
                call: Call<DoctorDetailsResponse>,
                response: Response<DoctorDetailsResponse>
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

    fun getNotifications(paramsSignUp: RequestBody): LiveData<NotificationResponse> {
        val mutableLiveData = MutableLiveData<NotificationResponse>()
        RetrofitInstance.create().getNotifications(paramsSignUp).enqueue(object :
            Callback<NotificationResponse> {
            override fun onFailure(call: Call<NotificationResponse>, t: Throwable) {
                Utility.showLog("Error", "Server Error : " + t.message)
                mutableLiveData.value = null
            }

            override fun onResponse(
                call: Call<NotificationResponse>,
                response: Response<NotificationResponse>
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

    fun commonRequest(paramsSignUp: RequestBody): LiveData<BaseResponse> {
        val mutableLiveData = MutableLiveData<BaseResponse>()
        RetrofitInstance.create().commonRequest(paramsSignUp).enqueue(object :
            Callback<BaseResponse> {
            override fun onFailure(call: Call<BaseResponse>, t: Throwable) {
                Utility.showLog("Error", "Server Error : " + t.message)
                mutableLiveData.value = null
            }

            override fun onResponse(
                call: Call<BaseResponse>,
                response: Response<BaseResponse>
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

    fun slotRequest(paramsSignUp: RequestBody): LiveData<SlotResponse> {
        val mutableLiveData = MutableLiveData<SlotResponse>()
        RetrofitInstance.create().slotRequest(paramsSignUp).enqueue(object :
            Callback<SlotResponse> {
            override fun onFailure(call: Call<SlotResponse>, t: Throwable) {
                Utility.showLog("Error", "Server Error : " + t.message)
                mutableLiveData.value = null
            }

            override fun onResponse(
                call: Call<SlotResponse>,
                response: Response<SlotResponse>
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

    fun getDoctorAppointments(paramsSignUp: RequestBody): LiveData<DoctorAppointmentsResponse> {
        val mutableLiveData = MutableLiveData<DoctorAppointmentsResponse>()
        RetrofitInstance.create().getDoctorAppointments(paramsSignUp).enqueue(object :
            Callback<DoctorAppointmentsResponse> {
            override fun onFailure(call: Call<DoctorAppointmentsResponse>, t: Throwable) {
                Utility.showLog("Error", "Server Error : " + t.message)
                mutableLiveData.value = null
            }

            override fun onResponse(
                call: Call<DoctorAppointmentsResponse>,
                response: Response<DoctorAppointmentsResponse>
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