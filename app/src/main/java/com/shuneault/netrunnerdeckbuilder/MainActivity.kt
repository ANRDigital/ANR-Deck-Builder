package com.shuneault.netrunnerdeckbuilder

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.shuneault.netrunnerdeckbuilder.ViewModel.BrowseCardsViewModel
import com.shuneault.netrunnerdeckbuilder.fragments.BrowseCardsFragment
import com.shuneault.netrunnerdeckbuilder.fragments.ChoosePacksDialogFragment
import com.shuneault.netrunnerdeckbuilder.fragments.ListDecksFragment
import com.shuneault.netrunnerdeckbuilder.fragments.OnBrowseCardsClickListener
import com.shuneault.netrunnerdeckbuilder.game.Card
import org.koin.android.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity(), OnBrowseCardsClickListener,
        ChoosePacksDialogFragment.ChoosePacksDialogListener {
    // BROWSE CARDS
    val vm: BrowseCardsViewModel by viewModel()


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

    private inner class DecksFragmentPager(fm: FragmentManager?) : FragmentPagerAdapter(fm!!) {
        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> return resources.getString(R.string.runner)
                1 -> return resources.getString(R.string.corp)
            }
            return ""
        }

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> ListDecksFragment.newInstance(Card.Side.SIDE_RUNNER)
                1 -> ListDecksFragment.newInstance(Card.Side.SIDE_CORPORATION)
                else -> ListDecksFragment.newInstance(Card.Side.SIDE_RUNNER)
            }
        }

        override fun getCount(): Int {
            return 2
        }
    }

    companion object {
        // Request Codes for activity launch
        const val REQUEST_NEW_IDENTITY = 1
        const val REQUEST_SETTINGS = 3
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