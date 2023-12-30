package com.wecareapp.android.webservices.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.wecareapp.android.model.ClientProfileResponse
import com.wecareapp.android.model.ClinicDoctorBalanceDashboardResponse
import com.wecareapp.android.webservices.repositories.ClinicRepository
import okhttp3.RequestBody

class ClinicViewModel : ViewModel() {

    fun getDoctorBalanceDashboard(paramsModelSignUp: RequestBody): LiveData<ClinicDoctorBalanceDashboardResponse> {
        return ClinicRepository().getDoctorBalanceDashboard(paramsModelSignUp)
    }

    fun getClinicProfile(paramsImage: RequestBody): LiveData<ClientProfileResponse> {
        return ClinicRepository().getClinicProfile(paramsImage)
    }

/*
    fun loadInbox(paramsAudio: RequestBody): LiveData<ContactsResponse> {
        return ActorChatRepository().loadChat(paramsAudio)
    }*/
}