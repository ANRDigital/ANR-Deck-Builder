package com.shuneault.netrunnerdeckbuilder

import android.app.AlertDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shuneault.netrunnerdeckbuilder.ViewModel.FullScreenViewModel
import com.shuneault.netrunnerdeckbuilder.ui.ThemeHelper
import org.koin.android.viewmodel.ext.android.viewModel

class ViewDeckFullscreenActivity : AppCompatActivity() {
    private val vm: FullScreenViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) { // load data
        vm.setCardCode ( intent.getStringExtra(EXTRA_CARD_CODE))
        vm.position = intent.getIntExtra(EXTRA_POSITION, 0)
        val cardList = intent.getSerializableExtra(EXTRA_CARDS)
        if (cardList != null) {
            vm.cardCodes = cardList as ArrayList<String>
        }

        val deckId = intent.getLongExtra(EXTRA_DECK_ID, 0)
        if (deckId > 0) {
            vm.loadDeck(deckId)
        }
        // set theme to identity's faction colors
        val factionCode = vm.factionCode
        if (factionCode != null) {
            setTheme(ThemeHelper.getTheme(factionCode, this))
        }
        // super must be called after setTheme or else notification and navigation bars won't be themed properly
        super.onCreate(savedInstanceState)
        // This too
        val mActionBar = supportActionBar
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true)
            mActionBar.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp)
        }
        setContentView(R.layout.activity_fullscreen_view)
        // Quit if deck is empty
        if (vm.size == 0) {
            exitIfDeckEmpty()
            return
        }
    }

    private fun exitIfDeckEmpty() {
        if (vm.isEmpty()) {
            val builder = MaterialAlertDialogBuilder(this)
            builder.setTitle(R.string.view_deck)
            builder.setMessage(R.string.deck_is_empty)
            builder.setCancelable(false)
            builder.setPositiveButton(R.string.ok) { _, _ -> finish() }
            builder.show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return false
    }

    companion object {
        // Arguments
        const val EXTRA_DECK_ID = "com.example.netrunnerdeckbuilder.EXTRA_DECK_ID"
        const val EXTRA_CARD_CODE = "com.example.netrunnerdeckbuilder.EXTRA_CARD_CODE"
        const val EXTRA_POSITION = "com.example.netrunnerdeckbuilder.EXTRA_POSITION"
        const val EXTRA_CARDS = "com.example.netrunnerdeckbuilder.EXTRA_CARDS"
    }
}