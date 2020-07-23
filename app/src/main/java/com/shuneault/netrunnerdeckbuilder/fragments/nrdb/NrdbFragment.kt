package com.shuneault.netrunnerdeckbuilder.fragments.nrdb

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.core.content.ContentProviderCompat
import androidx.fragment.app.Fragment
import com.shuneault.netrunnerdeckbuilder.R
import com.shuneault.netrunnerdeckbuilder.appauth.AuthStateManager
import org.koin.android.viewmodel.ext.android.viewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [NrdbFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NrdbFragment : Fragment() {

    private val vm: NrdbFragmentViewModel by viewModel()

    private lateinit var mStateManager: AuthStateManager

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mStateManager = AuthStateManager.getInstance(requireContext())
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_nrdb, container, false)
        val messageText = view.findViewById<TextView>(R.id.messageText)
        val state = mStateManager.current;
        if (state.refreshToken != null){
            messageText.text = "authorized!"
        }
        return view
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
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment NrdbFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                NrdbFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}
