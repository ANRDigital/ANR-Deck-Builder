package com.shuneault.netrunnerdeckbuilder.api
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.*

interface NrdbApiService {
    @GET("public/decklists/by_date/{date}")
    fun getDateDeckLists(@Path("date") date: String): Call<NrdbDeckLists>

    @GET("private/decklists/")
    fun getMyDeckLists(): Call<NrdbDeckLists>
}