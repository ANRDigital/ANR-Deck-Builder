package com.shuneault.netrunnerdeckbuilder.fragments.nrdb

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shuneault.netrunnerdeckbuilder.DeckViewActivity
import com.shuneault.netrunnerdeckbuilder.R
import com.shuneault.netrunnerdeckbuilder.adapters.ListDecksAdapter
import com.shuneault.netrunnerdeckbuilder.appauth.AuthStateManager
import com.shuneault.netrunnerdeckbuilder.game.Deck
import org.koin.android.viewmodel.ext.android.viewModel

private const val ARG_MODE = "MODE_PARAMETER"

/**
 * Use the [NrdbFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NrdbFragment : Fragment() {
    private lateinit var mRecyclerView: RecyclerView
    private val vm: NrdbFragmentViewModel by viewModel()

    private lateinit var mStateManager: AuthStateManager

    private var fragmentMode: Int = MODE_PUBLIC_DECKLISTS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mStateManager = AuthStateManager.getInstance(requireContext())
        arguments?.let {
            fragmentMode = it.getInt(ARG_MODE)
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_nrdb, container, false)

        mRecyclerView = view.findViewById(R.id.recyclerView)
        mRecyclerView.setHasFixedSize(true)
        val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(activity)
        mRecyclerView.layoutManager = mLayoutManager
        val mDeckAdapter = ListDecksAdapter(object : ListDecksAdapter.DeckViewHolder.IViewHolderClicks {
            override fun onDeckClick(deck: Deck) {
                // Load the deck activity
                if (!deck.hasUnknownCards()) startDeckViewActivityForNrdbDeck(deck)
            }

            override fun onDeckStarred(deck: Deck, isStarred: Boolean) {
//                vm.starDeck(deck, isStarred)
//                // Sort for new starred order
//                Collections.sort(mCurrentDecks, Sorter.DeckSorter())
//                mRecyclerView?.adapter!!.notifyDataSetChanged()
            }

            override fun onDeckView(deck: Deck) {
                // Load the deck view activity
                if (!deck.hasUnknownCards()) startDeckViewActivityForNrdbDeck(deck)
            }
        })

        // Initialize the layout manager and adapter
        when(fragmentMode){
            MODE_PUBLIC_DECKLISTS -> {
                 vm.getNrdbDeckLists(requireContext())
                    .observe(viewLifecycleOwner, Observer{ newData -> mDeckAdapter.setData(newData)})
            }
            MODE_PRIVATE_DECKS -> {
                vm.getNrdbPrivateDecks(requireContext())
                    .observe(viewLifecycleOwner, Observer{ newData -> mDeckAdapter.setData(newData)})
            }
        }

        mRecyclerView.adapter = mDeckAdapter
//        val messageText = view.findViewById<TextView>(R.id.messageText)
//        val state = mStateManager.current;
//        if (state.refreshToken != null){
//            messageText.text = "authorized!"
//        }
        return view
    }

    private fun startDeckViewActivityForNrdbDeck(deck: Deck) {
        val intent = Intent(activity, DeckViewActivity::class.java)
        intent.putExtra(DeckViewActivity.ARGUMENT_DECK, deck)
        requireActivity().startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.nrdb_menu, menu)
        menu.findItem(R.id.nrdb_sign_in_item).setOnMenuItemClickListener {
            ToggleNrdbSignIn()
            false
        }
    }

    private fun ToggleNrdbSignIn() {
        //todo: if signed in then check you want to sign out (confirmdlg), otherwise...
        vm.toggleNrdbSignIn(requireContext())
    }

    companion object {
        const val MODE_PUBLIC_DECKLISTS = 0
        const val MODE_PRIVATE_DECKS = 1

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param mode fragment mode parameter.
         * @return A new instance of fragment NrdbFragment.
         */
        @JvmStatic
        fun newInstance(mode: Int) =
                NrdbFragment().apply {
                    arguments = Bundle().apply {
                        putInt(ARG_MODE, mode)
                    }
                }
    }
}
