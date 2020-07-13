package com.shuneault.netrunnerdeckbuilder.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.shuneault.netrunnerdeckbuilder.ChooseIdentityActivity
import com.shuneault.netrunnerdeckbuilder.DeckActivity
import com.shuneault.netrunnerdeckbuilder.MainActivity
import com.shuneault.netrunnerdeckbuilder.R
import com.shuneault.netrunnerdeckbuilder.ViewModel.MainActivityViewModel
import com.shuneault.netrunnerdeckbuilder.game.Card
import com.shuneault.netrunnerdeckbuilder.game.Deck
import org.koin.android.viewmodel.ext.android.viewModel

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {

    val vm: MainActivityViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // GUI
        val mViewPager = view.findViewById<View>(R.id.viewPager) as ViewPager
        val mTabLayout = view.findViewById<View>(R.id.tabLayout) as TabLayout

        // Setup the ViewPager
        mViewPager.adapter = DecksFragmentPager(childFragmentManager, getString(R.string.runner), getString(R.string.corp))
        mTabLayout.setupWithViewPager(mViewPager)

        activity?.setTitle(R.string.action_decks)

        return view
    }





    private fun startDeckActivity(rowId: Long) {
        val intent = Intent(context, DeckActivity::class.java)
        intent.putExtra(DeckActivity.ARGUMENT_DECK_ID, rowId)
        startActivity(intent)
    }

    private class DecksFragmentPager(fm: FragmentManager?, val runnerName: String, val corpName: CharSequence?) : FragmentPagerAdapter(fm!!) {
        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> return runnerName
                1 -> return corpName
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

}

