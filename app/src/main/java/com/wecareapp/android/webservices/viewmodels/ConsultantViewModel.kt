package com.wecareapp.android.webservices.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.wecareapp.android.model.*
import com.wecareapp.android.webservices.repositories.ConsultantRepository
import okhttp3.RequestBody

class ConsultantViewModel : ViewModel() {

    fun getConsultantProfile(paramsModelSignUp: RequestBody): LiveData<ConsultantProfileResponse> {
        return ConsultantRepository().getConsultantProfile(paramsModelSignUp)
    }

    fun getDoctorAppointments(paramsModelSignUp: RequestBody): LiveData<DoctorAppointmentsResponse> {
        return ConsultantRepository().getDoctorAppointments(paramsModelSignUp)
    }

    fun getRatings(paramsModelSignUp: RequestBody): LiveData<RatingResponse> {
        return ConsultantRepository().getRatings(paramsModelSignUp)
    }

    fun getPlans(paramsModelSignUp: RequestBody): LiveData<PlansResponse> {
        return ConsultantRepository().getPlans(paramsModelSignUp)
    }
}