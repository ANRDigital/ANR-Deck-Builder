package com.shuneault.netrunnerdeckbuilder.fragments

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shuneault.netrunnerdeckbuilder.R
import com.shuneault.netrunnerdeckbuilder.ViewModel.BrowseCardsViewModel
import com.shuneault.netrunnerdeckbuilder.ViewModel.FullScreenViewModel
import com.shuneault.netrunnerdeckbuilder.adapters.BrowseCardRecyclerViewAdapter
import com.shuneault.netrunnerdeckbuilder.db.CardRepository
import com.shuneault.netrunnerdeckbuilder.fragments.ChoosePacksDialogFragment.ChoosePacksDialogListener
import com.shuneault.netrunnerdeckbuilder.game.Card
import com.shuneault.netrunnerdeckbuilder.game.Format
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.java.standalone.KoinJavaComponent
import java.util.*

/**
 * A fragment representing a list of Items.
 *
 *
 * Activities containing this fragment MUST implement the [OnBrowseCardsClickListener] interface.
 */
class BrowseCardsFragment : Fragment(), SearchView.OnQueryTextListener, OnBrowseCardsClickListener {
    private var mAdapter: BrowseCardRecyclerViewAdapter? = null
    val vm: BrowseCardsViewModel by sharedViewModel()
    val fullVM: FullScreenViewModel by sharedViewModel()
    var cardRepo = KoinJavaComponent.inject(CardRepository::class.java)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_browse_cards, container, false)
        setHasOptionsMenu(true)
        // Set the adapter
        if (view is RecyclerView) {
            val context = view.getContext()
            val recyclerView = view
            recyclerView.layoutManager = LinearLayoutManager(context)
            vm.init()
            mAdapter = BrowseCardRecyclerViewAdapter(vm.cardList, this, cardRepo.value)
            recyclerView.adapter = mAdapter
        }
        return view
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String): Boolean {
        vm.searchText = newText
        updateResults()
        return true
    }

    private fun updateResults() {
        vm.doSearch(vm.searchText)
        mAdapter!!.notifyDataSetChanged()
    }

    fun updatePackFilter(packFilter: ArrayList<String?>?) {
        vm.updatePackFilter(packFilter)
        updateResults()
    }

    // on list card clicked
    override fun onCardClicked(card: Card, position: Int) {
        fullVM.cardCodes = vm.cardList.codes
        fullVM.position = position
        val action = BrowseCardsFragmentDirections.actionBrowseCardsFragmentToFullscreenCardsFragment2()
        val navController = NavHostFragment.findNavController(this)
        navController.navigate(action)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.browse, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val sv = MenuItemCompat.getActionView(searchItem) as SearchView
        sv.setOnQueryTextListener(this)

        val filterItem = menu.findItem(R.id.action_filter)
        filterItem.setOnMenuItemClickListener {
            val choosePacksDlg = ChoosePacksDialogFragment()
            choosePacksDlg.setData(vm.packFilter, vm.getFormat(Format.FORMAT_ETERNAL))
            fragmentManager?.let { it1 -> choosePacksDlg.show(it1, "choosePacks") }
            false
        }
    }


}