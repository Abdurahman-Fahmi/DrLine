package com.wecareapp.android.webservices.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.wecareapp.android.model.BaseResponse
import com.wecareapp.android.model.LoginResponse
import com.wecareapp.android.webservices.repositories.ModelSignUpRepository
import com.wecareapp.android.webservices.repositories.UserSignInRepository
import okhttp3.RequestBody

class SignInViewModel : ViewModel() {

    fun userLogin(paramsModelSignUp: RequestBody): LiveData<LoginResponse> {
        return UserSignInRepository().userLogin(paramsModelSignUp)
    }
    /*fun getLoginResponseViewModel(paramsModelSignUp: RequestBody): LiveData<JsonElement> {
        return ModelSignUpRepository().getModelSignupResponse(paramsModelSignUp)
    }

    fun uploadRentDocument(paramsModelSignUp: RequestBody): LiveData<JsonElement> {
        return ModelSignUpRepository().uploadRentDocument(paramsModelSignUp)
    }*/
}