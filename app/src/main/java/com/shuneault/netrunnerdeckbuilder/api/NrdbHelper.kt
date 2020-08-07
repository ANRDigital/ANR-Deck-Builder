package com.shuneault.netrunnerdeckbuilder.api

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import com.shuneault.netrunnerdeckbuilder.R
import com.shuneault.netrunnerdeckbuilder.appauth.AuthStateManager
import com.shuneault.netrunnerdeckbuilder.appauth.TokenActivity
import com.shuneault.netrunnerdeckbuilder.game.Card
import com.shuneault.netrunnerdeckbuilder.helper.AppManager
import net.openid.appauth.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

object NrdbHelper {
    const val NRDB_SECRET = "lvlwz4israoo44kso088c4g40kwwco40008w04sgswc084kcw"

    /* Shows a card's page on netrunnerdb.com */
    fun ShowNrdbWebPage(context: Context, card: Card) {
        val url = String.format(context.getString(R.string.nrdb_card_url), card.code)
        val builder = CustomTabsIntent.Builder()
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(context, Uri.parse(url))
    }

    /* OAuth SignIn for Nrdb */
    fun doNrdbSignIn(context: Context) {
        val serviceConfiguration = AuthorizationServiceConfiguration(
                Uri.parse("https://netrunnerdb.com/oauth/v2/auth") /* auth endpoint */,
                Uri.parse("https://netrunnerdb.com/oauth/v2/token") /* token endpoint */
        )

        val clientId = "30_43lvcug21iqsgg44oss8go40ok04s80wcc8gocog8o0k88ck0c"
        val redirectUri: Uri = Uri.parse("org.anrdigital.anrdeckbuilder://oauth2callback")
        val builder = AuthorizationRequest.Builder(
                serviceConfiguration,
                clientId,
                "code",
                redirectUri
        )

        val request = builder.build()
        val authorizationService = AuthorizationService(context)
        val postAuthorizationIntent = Intent(context, TokenActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(context, request.hashCode(), postAuthorizationIntent, 0)
        authorizationService.performAuthorizationRequest(request, pendingIntent)
    }

    fun getDateDeckLists(date: Date, context: Context,
                                 onSuccess: (Response<NrdbDeckLists>)->Unit) {
        //todo: cache response?
        val dateString = SimpleDateFormat("yyyy-MM-dd").format(date)
//        val dateString = "2020-07-28"

        val apiService = NrdbClient().getApiService(context)
        val call = apiService.getDateDeckLists(dateString)
        Toast.makeText(context, R.string.accessing_nrdb, Toast.LENGTH_SHORT).show()

        call.enqueue(object : Callback<NrdbDeckLists> {
            override fun onResponse(call: Call<NrdbDeckLists>, response: Response<NrdbDeckLists>) {
                Log.i(AppManager.LOGCAT, "Success NRDB Response: decklists by_date")
                onSuccess(response)
            }

            override fun onFailure(call: Call<NrdbDeckLists>, t: Throwable) {
                Log.e(AppManager.LOGCAT, "Error NRDB Response: decklists by_date")
            }
        })
    }

    fun getMyPrivateDeckLists(context: Context,
                                      onSuccess: (Response<NrdbDeckLists>)->Unit) {
        //todo: split out token refresh?
        AuthStateManager.getInstance(context).current
                .performActionWithFreshTokens(AuthorizationService(context)
                ) {
                    _, _, ex ->
                    if (ex != null) {
                        this.signOut(context)
                    }
                    else {
                        privateDeckListsAction(context, onSuccess)
                    }
                }
    }

    private fun privateDeckListsAction(context: Context, onSuccess: (Response<NrdbDeckLists>) -> Unit) {
        //todo: cache response?
        val apiService = NrdbClient().getApiService(context)
        val call = apiService.getMyDeckLists()
        Toast.makeText(context, R.string.accessing_nrdb, Toast.LENGTH_SHORT).show()

        call.enqueue(object : Callback<NrdbDeckLists> {
            override fun onResponse(call: Call<NrdbDeckLists>, response: Response<NrdbDeckLists>) {
                Log.i(AppManager.LOGCAT, "Success NRDB Response: decklists by_date")
                onSuccess(response)
            }

            override fun onFailure(call: Call<NrdbDeckLists>, t: Throwable) {
                Log.e(AppManager.LOGCAT, "Error NRDB Response: decklists by_date")
            }
        })
    }

    private fun signOut(context: Context) {
        // discard the authorization and token state, but retain the configuration and
        // dynamic client registration (if applicable), to save from retrieving them again.
        val mStateManager = AuthStateManager.getInstance(context)
        val currentState: AuthState = mStateManager.current
        val clearedState = AuthState(currentState.authorizationServiceConfiguration!!)
        if (currentState.lastRegistrationResponse != null) {
            clearedState.update(currentState.lastRegistrationResponse)
        }
        mStateManager.replace(clearedState)
    }

}