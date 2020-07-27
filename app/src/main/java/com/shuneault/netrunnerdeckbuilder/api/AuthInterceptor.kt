package com.shuneault.netrunnerdeckbuilder.api

import android.content.Context
import com.shuneault.netrunnerdeckbuilder.appauth.AuthStateManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor to add auth token to requests
 */
class AuthInterceptor(context: Context) : Interceptor {
    private val mAuthStateManager = AuthStateManager.getInstance(context)

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()

        // If token has been saved, add it to the request
        mAuthStateManager.current.accessToken?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }

        return chain.proceed(requestBuilder.build())
    }
}