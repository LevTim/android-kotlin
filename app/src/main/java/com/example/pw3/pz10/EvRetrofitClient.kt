package com.example.pw3.pz10

import com.example.pw3.mock.MockEvBackendInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object EvRetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:5156/"

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(MockEvBackendInterceptor())
            .build()
    }

    val api: EvApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EvApiService::class.java)
    }
}