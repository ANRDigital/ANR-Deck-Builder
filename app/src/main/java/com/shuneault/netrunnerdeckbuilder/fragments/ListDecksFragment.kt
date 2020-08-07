package com.shuneault.netrunnerdeckbuilder.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.shuneault.netrunnerdeckbuilder.*
import com.shuneault.netrunnerdeckbuilder.ViewModel.MainActivityViewModel
import com.shuneault.netrunnerdeckbuilder.adapters.ListDecksAdapter
import com.shuneault.netrunnerdeckbuilder.adapters.ListDecksAdapter.DeckViewHolder.IViewHolderClicks
import com.shuneault.netrunnerdeckbuilder.game.Card
import com.shuneault.netrunnerdeckbuilder.game.Deck
import com.shuneault.netrunnerdeckbuilder.helper.Sorter.DeckSorter
import org.koin.android.viewmodel.ext.android.viewModel

import java.util.*

/**
 * Created by sebast on 11/02/16.
 */
class ListDecksFragment : Fragment() {

    // Database and decks
    private val vm: MainActivityViewModel by viewModel()
    private var mScrollDirection = 0
    private lateinit var fabButton: FloatingActionButton
    private lateinit var mRecyclerView: RecyclerView
    // Animations
    private var slideDown: Animation? = null
    private var slideUp: Animation? = null

    // Intent information
    private lateinit var mSide: String
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Side
        mSide = requireArguments().getString(EXTRA_SIDE)

        val mainView = inflater.inflate(R.layout.fragment_list_decks, container, false)

        // GUI
        fabButton = mainView.findViewById(R.id.fabButton)
        // Set up the FloatingActionButton
        fabButton.setOnClickListener {
            startChooseIdentityActivity(mSide)
        }

        // load show/hide animations for fab
        slideDown = AnimationUtils.loadAnimation(context, R.anim.slide_down)
        slideUp = AnimationUtils.loadAnimation(context, R.anim.slide_up)

        // Initialize the RecyclerView
        mRecyclerView = mainView.findViewById(R.id.recyclerView)
        mRecyclerView.setHasFixedSize(true)
        val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(activity)
        mRecyclerView.layoutManager = mLayoutManager

        // Initialize the layout manager and adapter
        val mCurrentDecks = currentDecks
        val mDeckAdapter = ListDecksAdapter(object : IViewHolderClicks {
            override fun onDeckClick(deck: Deck) {
                // Load the deck activity
                if (!deck.hasUnknownCards()) startDeckActivity(deck.rowId)
            }

            override fun onDeckStarred(deck: Deck, isStarred: Boolean) {
                vm.starDeck(deck, isStarred)
                // Sort for new starred order
                Collections.sort(mCurrentDecks, DeckSorter())
                mRecyclerView?.adapter!!.notifyDataSetChanged()
            }

            override fun onDeckView(deck: Deck) {
                // Load the deck view activity
                if (!deck.hasUnknownCards()) startDeckViewActivity(deck.rowId)
            }
        },
        true)

        mDeckAdapter.setData(mCurrentDecks)

        mRecyclerView.setAdapter(mDeckAdapter)
        mRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 && mScrollDirection <= 0) { // Scroll Down
                    // hide fab
                    fabButton!!.startAnimation(slideDown)
                    fabButton.hide()
                    mScrollDirection = dy
                } else if (dy < 0 && mScrollDirection >= 0) { // Scroll Up
                    // show fab
                    fabButton!!.show()
                    fabButton.startAnimation(slideUp)
                    mScrollDirection = dy
                } else {
                    // Same direction
                }
            }
        })
        return mainView
    }

    private fun startChooseIdentityActivity(side: String) {
        if (side != Card.Side.SIDE_CORPORATION && side != Card.Side.SIDE_RUNNER) return
        val intent = Intent(context, ChooseIdentityActivity::class.java)
        intent.putExtra(ChooseIdentityActivity.EXTRA_SIDE_CODE, side)
        startActivityForResult(intent, MainActivity.REQUEST_NEW_IDENTITY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            MainActivity.REQUEST_NEW_IDENTITY -> {
                // Get the chosen identity
                val identityCardCode = data?.getStringExtra(ChooseIdentityActivity.EXTRA_IDENTITY_CODE)
                // Create a new deck
                val mDeck: Deck = vm.createDeck(identityCardCode)
                // Start the new deck activity
                startDeckActivity(mDeck.rowId)
            }
        }
    }

    // Only the selected tab decks
    private val currentDecks: ArrayList<Deck>
        private get() {
            // Only the selected tab decks
            val mCurrentDecks = ArrayList<Deck>()
            val mDecks = vm.decks
            for (deck in mDecks) {
                if (deck != null && deck.side == mSide) {
                    mCurrentDecks.add(deck)
                }
            }
            // Sort the list
            Collections.sort(mCurrentDecks, DeckSorter())
            return mCurrentDecks
        }

    private fun startDeckActivity(rowId: Long) {
        val intent = Intent(activity, DeckActivity::class.java)
        intent.putExtra(DeckActivity.ARGUMENT_DECK_ID, rowId)
        requireActivity().startActivity(intent)
    }

    private fun startDeckViewActivity(rowId: Long) {
        val intent = Intent(activity, DeckViewActivity::class.java)
        intent.putExtra(DeckActivity.ARGUMENT_DECK_ID, rowId)
        requireActivity().startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        (mRecyclerView!!.adapter as ListDecksAdapter?)!!.setData(currentDecks)
    }

    companion object {
        private const val EXTRA_SIDE = "com.shuneault.netrunnerdeckbuilder.EXTRA_SIDE"
        fun newInstance(side: String?): ListDecksFragment {
            val fragment = ListDecksFragment()
            val bundle = Bundle()
            bundle.putString(EXTRA_SIDE, side)
            fragment.arguments = bundle
            return fragment
        }
    }
}