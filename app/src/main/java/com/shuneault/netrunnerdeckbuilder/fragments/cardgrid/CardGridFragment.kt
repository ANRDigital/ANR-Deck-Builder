package com.shuneault.netrunnerdeckbuilder.fragments.cardgrid

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.shuneault.netrunnerdeckbuilder.R
import kotlinx.android.synthetic.main.card_grid_fragment.*
import org.koin.android.viewmodel.ext.android.sharedViewModel

class CardGridFragment : Fragment() {

    val viewModel: CardGridViewModel by sharedViewModel()

    companion object {
        fun newInstance() = CardGridFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.card_grid_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        message.text = viewModel.title
    }

}
