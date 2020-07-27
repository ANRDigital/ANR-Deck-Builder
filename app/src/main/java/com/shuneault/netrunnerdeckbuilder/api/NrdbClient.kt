package com.shuneault.netrunnerdeckbuilder.api

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NrdbClient {
    private lateinit var apiService: NrdbApiService

    fun getApiService(context: Context): NrdbApiService {

        // Initialize ApiService if not initialized yet
        if (!::apiService.isInitialized) {
            val retrofit = Retrofit.Builder()
                    .baseUrl("https://netrunnerdb.com/api/2.0/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okhttpClient(context)) // Add our Okhttp client
                    .build()

            apiService = retrofit.create(NrdbApiService::class.java)
        }

        return apiService
    }

    /**
     * Initialize OkhttpClient with our interceptor
     */
    private fun okhttpClient(context: Context): OkHttpClient {
        return OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(context))
                .build()
    }

}