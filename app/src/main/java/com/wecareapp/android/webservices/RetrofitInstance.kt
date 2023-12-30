package com.wecareapp.android.webservices

import com.google.gson.GsonBuilder
import com.readystatesoftware.chuck.ChuckInterceptor
import com.wecareapp.android.BaseApplication
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitInstance {

    companion object {

        private var retrofit: Retrofit? = null
        private var client: OkHttpClient? = null

        var BASE_URL: String = "https://drline.app/"

        fun create(): AppService {
            if (retrofit == null) {
                retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
                    .client(getOkHttpClient())
                    .build()
            }
            return retrofit!!.create(AppService::class.java)
        }

        private fun getOkHttpClient(): OkHttpClient {
            if (client == null) {

                val logging = HttpLoggingInterceptor()
                logging.setLevel(HttpLoggingInterceptor.Level.BODY)

                val headers = HttpLoggingInterceptor()
                headers.setLevel(HttpLoggingInterceptor.Level.HEADERS)


                val builder = OkHttpClient.Builder()
                builder.readTimeout(60, TimeUnit.SECONDS)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .addInterceptor(logging)
                    .addInterceptor(headers)

                builder.addInterceptor(ChuckInterceptor(BaseApplication.getInstance()))

                client = builder.build()
            }
            return client!!
        }
    }

}