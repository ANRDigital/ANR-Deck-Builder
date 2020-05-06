package com.shuneault.netrunnerdeckbuilder

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.shuneault.netrunnerdeckbuilder.ViewModel.FullScreenViewModel
import org.koin.android.viewmodel.ext.android.viewModel


class DeckViewActivity : AppCompatActivity() {

    private val vm: FullScreenViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val deckId = intent.getLongExtra(DeckActivity.ARGUMENT_DECK_ID, -1)
        if (deckId < 0){
            finish()
        }

        vm.loadDeck(deckId)
        setContentView(R.layout.deck_view_activity)

    }

}
