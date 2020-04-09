package com.shuneault.netrunnerdeckbuilder

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.shuneault.netrunnerdeckbuilder.fragments.cardgrid.CardGridViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class ViewCardsAsGridActivity : AppCompatActivity() {
    private val vm: CardGridViewModel by viewModel()

    companion object {
        // Arguments
        const val EXTRA_DECK_ID = "com.example.netrunnerdeckbuilder.EXTRA_DECK_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mActionBar = supportActionBar
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true)
            mActionBar.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp)
        }
        // Get the deck
        val mDeckId = intent.getLongExtra(EXTRA_DECK_ID, 0)
        vm.loadDeck(mDeckId)
        title = vm.title
        // Quit if deck is empty
        if (vm.cardCounts.isEmpty()) {
            alertEmptyDeck()
        }

        setContentView(R.layout.card_grid_activity) // this loads the fragment
    }

    private fun alertEmptyDeck() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.view_deck)
        builder.setMessage(R.string.deck_is_empty)
        builder.setCancelable(false)
        builder.setPositiveButton(R.string.ok) { dialog: DialogInterface?, which: Int -> finish() }
        builder.show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return false
    }

}