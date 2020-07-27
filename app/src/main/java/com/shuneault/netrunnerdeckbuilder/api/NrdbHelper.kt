package com.shuneault.netrunnerdeckbuilder.api

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.shuneault.netrunnerdeckbuilder.R
import com.shuneault.netrunnerdeckbuilder.appauth.TokenActivity
import com.shuneault.netrunnerdeckbuilder.game.Card
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import org.json.JSONArray

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

    fun getMyNrdbDecks(): JSONArray {
        //todo: call api for data

        //todo: get decks data out of response
        val json = ""
        return JSONArray(json)
    }
}