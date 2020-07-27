package com.shuneault.netrunnerdeckbuilder.fragments.cardgrid

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.GridView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.shuneault.netrunnerdeckbuilder.R
import com.shuneault.netrunnerdeckbuilder.ViewModel.FullScreenViewModel
import com.shuneault.netrunnerdeckbuilder.adapters.CardCountImageAdapter
import com.shuneault.netrunnerdeckbuilder.game.CardCount
import com.shuneault.netrunnerdeckbuilder.api.NrdbHelper
import org.koin.android.viewmodel.ext.android.sharedViewModel

class CardGridFragment : Fragment() {

    val viewModel: FullScreenViewModel by sharedViewModel()

    companion object {
        fun newInstance() = CardGridFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.activity_view_deck_grid, container, false)

        activity?.title = viewModel.getDeckTitle()

        val gridView: GridView = view.findViewById<GridView>(R.id.gridView)
        gridView.adapter = CardCountImageAdapter(activity, viewModel.cardCounts)
        gridView.onItemClickListener = OnItemClickListener { parent, view, position, id ->
            viewModel.position = position
            val action = CardGridFragmentDirections.actionCardGridFragmentToFullscreenCardsFragment()
            findNavController().navigate(action)
        }
        gridView.onItemLongClickListener = OnItemLongClickListener { adapterView: AdapterView<*>, view: View?, pos: Int, id: Long ->
            val item = adapterView.getItemAtPosition(pos) as CardCount
            val card = item.card
            NrdbHelper.ShowNrdbWebPage(requireContext(), card)
            true
        }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

}
