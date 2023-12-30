package com.wecareapp.android.webservices.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wecareapp.android.model.BaseResponse
import com.wecareapp.android.model.LoginResponse
import com.wecareapp.android.utilities.Utility
import com.wecareapp.android.webservices.RetrofitInstance
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserSignInRepository {
    fun userLogin(paramsSignUp: RequestBody): LiveData<LoginResponse> {
        val signUpResponse = MutableLiveData<LoginResponse>()
        RetrofitInstance.create().userLogin(paramsSignUp).enqueue(object :
            Callback<LoginResponse> {
            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Utility.showLog("Error", "Server Error : " + t.message)
                signUpResponse.value = null
            }

            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                Utility.showLog("Error", "Repository Response " + response)
                if (response.isSuccessful) {
                    signUpResponse.value = response.body()
                } else {
                    signUpResponse.value = null
                    Utility.showLog("Error", "Error Else " + response)
                }
            }
        })
        return signUpResponse
    }
}