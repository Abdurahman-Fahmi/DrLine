package com.wecareapp.android.ui.fragment

import com.wecareapp.android.model.Doctor

interface ICallBackToActivity {
    fun onBackClicked(home: Boolean)
    fun hideKeyboard()
    fun openChat()
    fun onLogout()
    fun openChat(doctor: Doctor?)
}