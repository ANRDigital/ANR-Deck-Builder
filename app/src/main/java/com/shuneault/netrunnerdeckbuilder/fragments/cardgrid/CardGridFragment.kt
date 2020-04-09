package com.shuneault.netrunnerdeckbuilder.fragments.cardgrid

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.GridView
import androidx.fragment.app.Fragment
import com.shuneault.netrunnerdeckbuilder.R
import com.shuneault.netrunnerdeckbuilder.ViewDeckFullscreenActivity
import com.shuneault.netrunnerdeckbuilder.adapters.CardCountImageAdapter
import com.shuneault.netrunnerdeckbuilder.game.CardCount
import com.shuneault.netrunnerdeckbuilder.helper.NrdbHelper
import org.koin.android.viewmodel.ext.android.sharedViewModel

class CardGridFragment : Fragment() {

    val viewModel: CardGridViewModel by sharedViewModel()

    companion object {
        fun newInstance() = CardGridFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.activity_view_deck_grid, container, false)

        val gridView: GridView = view.findViewById<GridView>(R.id.gridView)
        gridView.adapter = CardCountImageAdapter(activity, viewModel.cardCounts)
        gridView.onItemClickListener = OnItemClickListener { parent, view, position, id ->
            val intent = Intent(context, ViewDeckFullscreenActivity::class.java)
            intent.putExtra(ViewDeckFullscreenActivity.EXTRA_DECK_ID, viewModel.deckId)
            intent.putExtra(ViewDeckFullscreenActivity.EXTRA_POSITION, position)
            startActivity(intent)
        }
        gridView.onItemLongClickListener = OnItemLongClickListener { adapterView: AdapterView<*>, view: View?, pos: Int, id: Long ->
            val item = adapterView.getItemAtPosition(pos) as CardCount
            val card = item.card
            NrdbHelper.ShowNrdbWebPage(context, card)
            true
        }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

}
