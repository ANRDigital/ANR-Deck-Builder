package com.shuneault.netrunnerdeckbuilder

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.NavHostFragment
import com.shuneault.netrunnerdeckbuilder.ViewModel.BrowseCardsViewModel
import com.shuneault.netrunnerdeckbuilder.fragments.BrowseCardsFragment
import com.shuneault.netrunnerdeckbuilder.fragments.ChoosePacksDialogFragment
import com.shuneault.netrunnerdeckbuilder.fragments.ChoosePacksDialogFragment.ChoosePacksDialogListener
import com.shuneault.netrunnerdeckbuilder.fragments.OnBrowseCardsClickListener
import com.shuneault.netrunnerdeckbuilder.game.Card
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.*

class BrowseActivity : AppCompatActivity(), OnBrowseCardsClickListener, ChoosePacksDialogListener {
    private var mPackFilter = ArrayList<String?>()
    val vm: BrowseCardsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.browse_cards)
        setTitle(R.string.title_browse_cards)

        val mActionBar = supportActionBar
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true)
            mActionBar.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp)
        }
    }

    override fun onCardClicked(card: Card, position: Int) { // do nothing for now
        val intent = Intent(this, ViewDeckFullscreenActivity::class.java)
        intent.putExtra(ViewDeckFullscreenActivity.EXTRA_CARD_CODE, card.code)
        startActivity(intent)
    }

    override fun onChoosePacksDialogPositiveClick(dialog: DialogFragment) { // save the new setting
        val dlg = dialog as ChoosePacksDialogFragment
        vm.setFilter(dlg.selectedValues)
        mPackFilter = dlg.selectedValues
        // update list
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment?
        val childFragmentManager = navHostFragment!!.childFragmentManager
        val frag = childFragmentManager.primaryNavigationFragment as BrowseCardsFragment?
        frag!!.updatePackFilter(vm.packFilter)
    }

    override fun onMyCollectionChosen(dialog: DialogFragment) {
        vm.useMyCollectionAsFilter()
        // update list
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment?
        val frag = navHostFragment!!.childFragmentManager.primaryNavigationFragment as BrowseCardsFragment?
        frag!!.updatePackFilter(vm.packFilter)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return false
    }
}