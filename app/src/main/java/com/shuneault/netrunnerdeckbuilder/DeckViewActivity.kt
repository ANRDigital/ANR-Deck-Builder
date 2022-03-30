package com.shuneault.netrunnerdeckbuilder

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.shuneault.netrunnerdeckbuilder.ViewModel.FullScreenViewModel
import com.shuneault.netrunnerdeckbuilder.game.Deck
import com.shuneault.netrunnerdeckbuilder.ui.ThemeHelper
import org.koin.androidx.viewmodel.ext.android.viewModel

class DeckViewActivity : AppCompatActivity() {
    companion object  {
        const val ARGUMENT_DECK: String  = "DECK_ARGUMENT"
    }

    private val vm: FullScreenViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val incomingDeck : Deck? = intent.getSerializableExtra(ARGUMENT_DECK) as Deck?
        if (incomingDeck != null){
            vm.setDeck(incomingDeck)
        }
        else {
            val deckId = intent.getLongExtra(DeckActivity.ARGUMENT_DECK_ID, -1)
            if (deckId < 0) {
                finish()
            }

            vm.loadDeck(deckId)
        }

        val factionCode = vm.factionCode
        if (factionCode != null) {
            val theme = ThemeHelper.getTheme(factionCode, this)
            setTheme(theme)
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setContentView(R.layout.deck_view_activity)
    }

    override fun onSupportNavigateUp(): Boolean {
        val nav = findNavController(R.id.nav_host_fragment)
        if (!nav.popBackStack()) {
            finish()
        }
        return false;
    }

}
