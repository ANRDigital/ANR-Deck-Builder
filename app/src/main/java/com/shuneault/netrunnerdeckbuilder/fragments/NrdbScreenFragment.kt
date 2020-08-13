package com.shuneault.netrunnerdeckbuilder.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.shuneault.netrunnerdeckbuilder.R
import com.shuneault.netrunnerdeckbuilder.fragments.nrdb.NrdbFragment
import com.shuneault.netrunnerdeckbuilder.fragments.nrdb.NrdbFragment.Companion.MODE_PRIVATE_DECKS
import com.shuneault.netrunnerdeckbuilder.fragments.nrdb.NrdbFragment.Companion.MODE_PUBLIC_DECKLISTS

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [NrdbScreenFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NrdbScreenFragment : Fragment() {
    private lateinit var viewPager: ViewPager

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_nrdb_screen, container, false)
        viewPager = view.findViewById(R.id.nrdb_list_pager)
        viewPager.adapter =  (NrdbFragmentPagerAdapter(childFragmentManager))

        val tabs = view.findViewById<TabLayout>(R.id.nrdb_tabs)
        tabs.setupWithViewPager(viewPager)
        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment NrdbScreenFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                NrdbScreenFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }

    private class NrdbFragmentPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> NrdbFragment.newInstance(MODE_PUBLIC_DECKLISTS)
                1 -> NrdbFragment.newInstance(MODE_PRIVATE_DECKS)
                else -> NrdbFragment.newInstance(MODE_PUBLIC_DECKLISTS)
            }
        }

        override fun getCount(): Int {
            return 2
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return when (position){
                1 -> "My Decks"
                else -> "Latest"
            }
        }
    }
}