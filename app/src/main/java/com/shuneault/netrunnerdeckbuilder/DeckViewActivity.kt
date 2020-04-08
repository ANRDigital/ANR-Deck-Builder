package com.shuneault.netrunnerdeckbuilder

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.shuneault.netrunnerdeckbuilder.ViewModel.DeckActivityViewModel
import org.koin.android.viewmodel.ext.android.viewModel


class DeckViewActivity : AppCompatActivity() {

    private val deckViewModel: DeckActivityViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val deckId = intent.getLongExtra(DeckActivity.ARGUMENT_DECK_ID, -1)
        if (deckId < 0){
            finish()
        }

        deckViewModel.setDeckId(deckId)

        setContentView(R.layout.deck_view_activity)

    }

}
