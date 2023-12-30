package com.wecareapp.android.webservices.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wecareapp.android.model.*
import com.wecareapp.android.model.ContactsResponse
import com.wecareapp.android.model.ConversationResponse
import com.wecareapp.android.model.DoctorSlotsResponse2
import com.wecareapp.android.utilities.Utility
import com.wecareapp.android.webservices.RetrofitInstance
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PatientRepository {
    fun getDoctorList(paramsSignUp: RequestBody): LiveData<DoctorListResponse> {
        val mutableLiveData = MutableLiveData<DoctorListResponse>()
        RetrofitInstance.create().getDoctorList(paramsSignUp).enqueue(object :
            Callback<DoctorListResponse> {
            override fun onFailure(call: Call<DoctorListResponse>, t: Throwable) {
                Utility.showLog("Error", "Server Error : " + t.message)
                mutableLiveData.value = null
            }

            override fun onResponse(
                call: Call<DoctorListResponse>,
                response: Response<DoctorListResponse>
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

    fun slotRequest2(paramsSignUp: RequestBody): LiveData<Response<ResponseBody>> {
        val mutableLiveData = MutableLiveData<Response<ResponseBody>>()
        RetrofitInstance.create().slotRequest2(paramsSignUp).enqueue(object :
            Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Utility.showLog("Error", "Server Error : " + t.message)
                mutableLiveData.value = null
            }

            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                Utility.showLog("Error", "Repository Response $response")
                if (response.isSuccessful) {
                    mutableLiveData.value = response
                } else {
                    mutableLiveData.value = null
                    Utility.showLog("Error", "Error Else $response")
                }
            }
        })
        return mutableLiveData
    }


    fun getDoctorSlots(paramsSignUp: RequestBody): LiveData<DoctorSlotsResponse> {
        val mutableLiveData = MutableLiveData<DoctorSlotsResponse>()
        RetrofitInstance.create().getDoctorSlots(paramsSignUp).enqueue(object :
            Callback<DoctorSlotsResponse> {
            override fun onFailure(call: Call<DoctorSlotsResponse>, t: Throwable) {
                Utility.showLog("Error", "Server Error : " + t.message)
                mutableLiveData.value = null
            }

            override fun onResponse(
                call: Call<DoctorSlotsResponse>,
                response: Response<DoctorSlotsResponse>
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


    fun getDoctorSlots2(paramsSignUp: RequestBody): LiveData<DoctorSlotsResponse2> {
        val mutableLiveData = MutableLiveData<DoctorSlotsResponse2>()
        RetrofitInstance.create().getDoctorSlots2(paramsSignUp).enqueue(object :
            Callback<DoctorSlotsResponse2> {
            override fun onFailure(call: Call<DoctorSlotsResponse2>, t: Throwable) {
                Utility.showLog("Error", "Server Error : " + t.message)
                mutableLiveData.value = null
            }

            override fun onResponse(
                call: Call<DoctorSlotsResponse2>,
                response: Response<DoctorSlotsResponse2>
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

    fun getChatList(paramsSignUp: RequestBody): LiveData<ContactsResponse> {
        val mutableLiveData = MutableLiveData<ContactsResponse>()
        RetrofitInstance.create().getChatList(paramsSignUp).enqueue(object :
            Callback<ContactsResponse> {
            override fun onFailure(call: Call<ContactsResponse>, t: Throwable) {
                Utility.showLog("Error", "Server Error : " + t.message)
                mutableLiveData.value = null
            }

            override fun onResponse(
                call: Call<ContactsResponse>,
                response: Response<ContactsResponse>
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

    fun getCategoriesList(paramsSignUp: RequestBody): LiveData<CategoriesListResponse> {
        val mutableLiveData = MutableLiveData<CategoriesListResponse>()
        RetrofitInstance.create().getCategoriesList(paramsSignUp).enqueue(object :
            Callback<CategoriesListResponse> {
            override fun onFailure(call: Call<CategoriesListResponse>, t: Throwable) {
                Utility.showLog("Error", "Server Error : " + t.message)
                mutableLiveData.value = null
            }

            override fun onResponse(
                call: Call<CategoriesListResponse>,
                response: Response<CategoriesListResponse>
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

    fun getClinicDoctorsByCategory(paramsSignUp: RequestBody): LiveData<DoctorListResponse> {
        val mutableLiveData = MutableLiveData<DoctorListResponse>()
        RetrofitInstance.create().getClinicDoctorsByCategory(paramsSignUp).enqueue(object :
            Callback<DoctorListResponse> {
            override fun onFailure(call: Call<DoctorListResponse>, t: Throwable) {
                Utility.showLog("Error", "Server Error : " + t.message)
                mutableLiveData.value = null
            }

            override fun onResponse(
                call: Call<DoctorListResponse>,
                response: Response<DoctorListResponse>
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

    fun sendMessage(paramsSignUp: RequestBody): LiveData<BaseResponse> {
        val mutableLiveData = MutableLiveData<BaseResponse>()
        RetrofitInstance.create().sendMessage(paramsSignUp).enqueue(object :
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

    fun loadMessages(paramsSignUp: RequestBody): LiveData<ConversationResponse> {
        val mutableLiveData = MutableLiveData<ConversationResponse>()
        RetrofitInstance.create().getChatMessages(paramsSignUp).enqueue(object :
            Callback<ConversationResponse> {
            override fun onFailure(call: Call<ConversationResponse>, t: Throwable) {
                Utility.showLog("Error", "Server Error : " + t.message)
                mutableLiveData.value = null
            }

            override fun onResponse(
                call: Call<ConversationResponse>,
                response: Response<ConversationResponse>
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

    fun getPatientDoctorAppointments(paramsSignUp: RequestBody): LiveData<DoctorAppointmentsResponse> {
        val mutableLiveData = MutableLiveData<DoctorAppointmentsResponse>()
        RetrofitInstance.create().getPatientDoctorAppointments(paramsSignUp).enqueue(object :
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

    fun getProfile(paramsSignUp: RequestBody): LiveData<PatientProfileResponse> {
        val mutableLiveData = MutableLiveData<PatientProfileResponse>()
        RetrofitInstance.create().getProfile(paramsSignUp).enqueue(object :
            Callback<PatientProfileResponse> {
            override fun onFailure(call: Call<PatientProfileResponse>, t: Throwable) {
                Utility.showLog("Error", "Server Error : " + t.message)
                mutableLiveData.value = null
            }

            override fun onResponse(
                call: Call<PatientProfileResponse>,
                response: Response<PatientProfileResponse>
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