package com.wecareapp.android.webservices.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.wecareapp.android.model.BaseResponse
import com.wecareapp.android.webservices.repositories.ModelSignUpRepository
import okhttp3.RequestBody

class SignUpViewModel : ViewModel() {

    fun action(paramsModelSignUp: RequestBody): LiveData<BaseResponse> {
        return ModelSignUpRepository().action(paramsModelSignUp)
    }
    /*fun getLoginResponseViewModel(paramsModelSignUp: RequestBody): LiveData<JsonElement> {
        return ModelSignUpRepository().getModelSignupResponse(paramsModelSignUp)
    }

    fun uploadRentDocument(paramsModelSignUp: RequestBody): LiveData<JsonElement> {
        return ModelSignUpRepository().uploadRentDocument(paramsModelSignUp)
    }*/
}