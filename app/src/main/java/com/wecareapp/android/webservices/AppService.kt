package com.wecareapp.android.webservices

import com.wecareapp.android.model.*
import com.wecareapp.android.model.PatientProfileResponse
import com.wecareapp.android.model.DoctorSlotsResponse2
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AppService {

    @POST("/webservices/index.php")
    fun action(@Body params: RequestBody): Call<BaseResponse>

    @POST("/webservices/index.php")
    fun getDoctorList(@Body params: RequestBody): Call<DoctorListResponse>

    @POST("/webservices/index.php")
    fun userLogin(@Body params: RequestBody): Call<LoginResponse>

    @POST("/webservices/index.php")
    fun getClinicList(@Body params: RequestBody): Call<ClinicListResponse>

    @POST("/webservices/index.php")
    fun getDoctorDetails(@Body params: RequestBody): Call<DoctorDetailsResponse>

    @POST("/webservices/index.php")
    fun getNotifications(@Body params: RequestBody): Call<NotificationResponse>

    @POST("/webservices/index.php")
    fun commonRequest(@Body params: RequestBody): Call<BaseResponse>

    @POST("/webservices/index.php")
    fun slotRequest(@Body params: RequestBody): Call<SlotResponse>

    @POST("/webservices/index.php")
    fun slotRequest2(@Body params: RequestBody): Call<ResponseBody>

    @POST("/webservices/index.php")
    fun getDoctorSlots(@Body params: RequestBody): Call<DoctorSlotsResponse>

    @POST("/webservices/index.php")
    fun getDoctorSlots2(@Body params: RequestBody): Call<DoctorSlotsResponse2>

    @POST("/webservices/index.php")
    fun getConsultantProfile(@Body params: RequestBody): Call<ConsultantProfileResponse>

    @POST("/webservices/index.php")
    fun getChatList(@Body params: RequestBody): Call<ContactsResponse>

    @POST("/webservices/index.php")
    fun getChatMessages(@Body params: RequestBody): Call<ConversationResponse>

    @POST("/webservices/index.php")
    fun sendMessage(@Body params: RequestBody): Call<BaseResponse>

    @POST("/webservices/index.php")
    fun getCategoriesList(@Body params: RequestBody): Call<CategoriesListResponse>

    @POST("/webservices/index.php")
    fun getClinicDoctorsByCategory(@Body params: RequestBody): Call<DoctorListResponse>

    @POST("/webservices/index.php")
    fun getRatings(@Body params: RequestBody): Call<RatingResponse>

    @POST("/webservices/index.php")
    fun getPlans(@Body params: RequestBody): Call<PlansResponse>

    @POST("/webservices/index.php")
    fun getDoctorAppointments(@Body params: RequestBody): Call<DoctorAppointmentsResponse>

    @POST("/webservices/index.php")
    fun getPatientDoctorAppointments(@Body params: RequestBody): Call<DoctorAppointmentsResponse>

    @POST("/webservices/index.php")
    fun getDoctorBalanceDashboard(@Body params: RequestBody): Call<ClinicDoctorBalanceDashboardResponse>

    @POST("/webservices/index.php")
    fun getProfile(@Body params: RequestBody): Call<PatientProfileResponse>

    @POST("/webservices/index.php")
    fun getClinicProfile(@Body params: RequestBody): Call<ClientProfileResponse>

    @POST("/webservices/index.php")
    fun sendNotification(@Body params: RequestBody): Call<PatientProfileResponse>

//    @POST("/webservices/index.php")
//    fun getRatings(@Body params: RequestBody): Call<RatingResponse>

/*
    @Multipart
    @POST("/customer_registration")
    fun registerSingleCustomer(
        @Part("device_id") device_id: RequestBody?,
        @Part("token_id") token_id: RequestBody?,
        @Part("customer_type") customer_type: RequestBody?,
        @Part("full_name") full_name: RequestBody?,
        @Part("email") email: RequestBody?,
        @Part("password") password: RequestBody?,
        @Part("gender") gender: RequestBody?,
        @Part("interests") interests: RequestBody?,
        @Part("latitude") latitude: RequestBody?,
        @Part("longitude") longitude: RequestBody?,
        @Part image: MultipartBody.Part?
    ): Call<JsonElement>
*/

}