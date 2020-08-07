package com.shuneault.netrunnerdeckbuilder

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.github.mikephil.charting.charts.Chart.LOG_TAG
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.shuneault.netrunnerdeckbuilder.ViewModel.BrowseCardsViewModel
import com.shuneault.netrunnerdeckbuilder.fragments.BrowseCardsFragment
import com.shuneault.netrunnerdeckbuilder.fragments.ChoosePacksDialogFragment
import com.shuneault.netrunnerdeckbuilder.fragments.OnBrowseCardsClickListener
import com.shuneault.netrunnerdeckbuilder.game.Card
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import org.koin.android.viewmodel.ext.android.viewModel


class MainActivity : AppCompatActivity(), OnBrowseCardsClickListener,
        ChoosePacksDialogFragment.ChoosePacksDialogListener {

    private val USED_INTENT = "USED_INTENT"

    // BROWSE CARDS
    val vm: BrowseCardsViewModel by viewModel()

    /// copy from app-auth
    override fun onNewIntent(intent: Intent?) {
        checkIntent(intent)
        super.onNewIntent(intent)
    }

    private fun checkIntent(intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            when (action) {
                "org.anrdigital.anrdeckbuilder.HANDLE_AUTHORIZATION_RESPONSE" -> {
                    if (!intent.hasExtra(USED_INTENT)) {
                        handleAuthorizationResponse(intent);
                        intent.putExtra(USED_INTENT, true);
                    }
                }
                else -> {
                }
            }
        }
    }

    private fun handleAuthorizationResponse(intent: Intent) {
        val response = AuthorizationResponse.fromIntent(intent)
        val error = AuthorizationException.fromIntent(intent)
        val authState = AuthState(response, error)

        if (response != null) {
            Log.i(LOG_TAG, java.lang.String.format("Handled Authorization Response %s ", authState.jsonSerializeString()))

            val service = AuthorizationService(this)
            service.performTokenRequest(response.createTokenExchangeRequest()) { tokenResponse, exception ->
                if (exception != null) {
                    Log.w(LOG_TAG, "Token Exchange failed", exception)
                } else {
                    if (tokenResponse != null) {
                        authState.update(tokenResponse, exception)
                        writeAuthState(authState)
                        Log.i(LOG_TAG, String.format("Token Response [ Access Token: %s, ID Token: %s ]", tokenResponse.accessToken, tokenResponse.idToken))
                    }
                }
            }
        }
    }

    fun writeAuthState(state: AuthState) {
        val authPrefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        authPrefs.edit()
                .putString("stateJson", state.jsonSerializeString())
                .apply()
    }

    override fun onStart() {
        super.onStart()
        checkIntent(intent)
    }

    /// END copy from app-auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.app_toolbar)
        setSupportActionBar(toolbar)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav)
        val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment?
        NavigationUI.setupWithNavController(bottomNavigationView, navHostFragment!!.navController)
    }

    override fun onCardClicked(card: Card, position: Int) { // do nothing for now
        val intent = Intent(this, ViewDeckFullscreenActivity::class.java)
        intent.putExtra(ViewDeckFullscreenActivity.EXTRA_CARD_CODE, card.code)
        startActivity(intent)
    }

    companion object {
        // Request Codes for activity launch
        const val REQUEST_NEW_IDENTITY = 1
        const val REQUEST_SETTINGS = 3 //todo: delete this
    }

    override fun onChoosePacksDialogPositiveClick(dialog: DialogFragment?) {
        val dlg = dialog as ChoosePacksDialogFragment
        vm.updatePackFilter(dlg.getSelectedValues(), dlg.format)

        // update the screen / list
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment?
        val childFragmentManager = navHostFragment!!.childFragmentManager
        val frag = childFragmentManager.primaryNavigationFragment as BrowseCardsFragment?
        frag!!.notifyDataUpdated()
    }

    override fun onMyCollectionChosen(dialog: DialogFragment?) {
        vm.useMyCollectionAsFilter()

        // update list
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment?
        val frag = navHostFragment!!.childFragmentManager.primaryNavigationFragment as BrowseCardsFragment?
        frag!!.notifyDataUpdated()
    }
}