package com.shuneault.netrunnerdeckbuilder.fragments

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.shuneault.netrunnerdeckbuilder.R
import com.shuneault.netrunnerdeckbuilder.ViewModel.FullScreenViewModel
import com.shuneault.netrunnerdeckbuilder.game.CardCount
import com.shuneault.netrunnerdeckbuilder.helper.ImageDisplayer
import com.shuneault.netrunnerdeckbuilder.helper.NrdbHelper
import org.koin.android.viewmodel.ext.android.sharedViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FullscreenCardsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FullscreenCardsFragment : Fragment() {
    private lateinit var viewAdapter: ImageViewPagerAdapter

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    val vm: FullScreenViewModel by sharedViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_fullscreen_cards, container, false)
        val viewPager = view.findViewById<ViewPager2>(R.id.pager)
        this.viewAdapter = ImageViewPagerAdapter(vm.cardCounts, View.OnClickListener {
            findNavController().popBackStack()
//            activity?.finish()
        })
        viewPager.registerOnPageChangeCallback(object:
            ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                vm.position = position;
                activity?.title = vm.getCurrentCardTitle()
            }
        }
        )
        viewPager.adapter = this.viewAdapter;
        viewPager.setCurrentItem(vm.position, false)

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fullscreen_menu, menu)
        //super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.mnuOnline -> {
                // show nrdb page!
                NrdbHelper.ShowNrdbWebPage(context, vm.getCurrentCard())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FullscreenCardsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                FullscreenCardsFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }

    private class ImageViewPagerAdapter(private val mCardCounts: ArrayList<CardCount>, val itemClickListener: View.OnClickListener) : RecyclerView.Adapter<ImageViewPagerAdapter.ImageViewHolder>() {
        // ViewHolder class for ImageView
        class ImageViewHolder(itemView: View):RecyclerView.ViewHolder(itemView)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
            val imageCardView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.image_card_view, parent, false)
            imageCardView.findViewById<ImageView>(R.id.cardImage).setOnClickListener(itemClickListener)
            return ImageViewHolder(imageCardView)
        }

        override fun getItemCount(): Int {
            return mCardCounts.size
        }

        override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
            val cardCount: CardCount = mCardCounts[position]
            val imageView: ImageView = holder.itemView.findViewById(R.id.cardImage) as ImageView
            imageView.contentDescription = cardCount.card.text
            ImageDisplayer.fill(imageView, cardCount.card, imageView.context )
        }
    }

}
