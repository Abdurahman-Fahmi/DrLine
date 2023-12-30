package com.wecareapp.android.webservices.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.wecareapp.android.model.*
import com.wecareapp.android.model.ConversationResponse
import com.wecareapp.android.model.DoctorSlotsResponse2
import com.wecareapp.android.webservices.repositories.PatientRepository
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response

class PatientViewModel : ViewModel() {

    fun slotRequest(paramsModelSignUp: RequestBody): LiveData<SlotResponse> {
        return PatientRepository().slotRequest(paramsModelSignUp)
    }
    fun slotRequest2(paramsModelSignUp: RequestBody): LiveData<Response<ResponseBody>> {
        return PatientRepository().slotRequest2(paramsModelSignUp)
    }
    fun getDoctorSlots(paramsModelSignUp: RequestBody): LiveData<DoctorSlotsResponse> {
        return PatientRepository().getDoctorSlots(paramsModelSignUp)
    }

    fun getDoctorSlots2(paramsModelSignUp: RequestBody): LiveData<DoctorSlotsResponse2> {
        return PatientRepository().getDoctorSlots2(paramsModelSignUp)
    }

    fun getDoctorList(paramsModelSignUp: RequestBody): LiveData<DoctorListResponse> {
        return PatientRepository().getDoctorList(paramsModelSignUp)
    }

    fun getClinicList(paramsModelSignUp: RequestBody): LiveData<ClinicListResponse> {
        return PatientRepository().getClinicList(paramsModelSignUp)
    }

    fun getNotifications(paramsModelSignUp: RequestBody): LiveData<NotificationResponse> {
        return PatientRepository().getNotifications(paramsModelSignUp)
    }

    fun commonRequest(paramsModelSignUp: RequestBody): LiveData<BaseResponse> {
        return PatientRepository().commonRequest(paramsModelSignUp)
    }

    fun getChatList(paramsModelSignUp: RequestBody): LiveData<ContactsResponse> {
        return PatientRepository().getChatList(paramsModelSignUp)
    }

    fun getCategoriesList(paramsModelSignUp: RequestBody): LiveData<CategoriesListResponse> {
        return PatientRepository().getCategoriesList(paramsModelSignUp)
    }

    fun getClinicDoctorsByCategory(paramsModelSignUp: RequestBody): LiveData<DoctorListResponse> {
        return PatientRepository().getClinicDoctorsByCategory(paramsModelSignUp)
    }

    fun getProfile(paramsModelSignUp: RequestBody): LiveData<PatientProfileResponse> {
        return PatientRepository().getProfile(paramsModelSignUp)
    }

    fun sendMessage(paramsModelSignUp: RequestBody): LiveData<BaseResponse> {
        return PatientRepository().sendMessage(paramsModelSignUp)
    }

    fun loadMessages(paramsModelSignUp: RequestBody): LiveData<ConversationResponse> {
        return PatientRepository().loadMessages(paramsModelSignUp)
    }

    fun getPatientDoctorAppointments(paramsModelSignUp: RequestBody): LiveData<DoctorAppointmentsResponse> {
        return PatientRepository().getPatientDoctorAppointments(paramsModelSignUp)
    }
}