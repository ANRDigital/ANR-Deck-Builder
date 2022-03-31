package com.shuneault.netrunnerdeckbuilder.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.shuneault.netrunnerdeckbuilder.db.CardRepository
import com.shuneault.netrunnerdeckbuilder.game.Card
import com.shuneault.netrunnerdeckbuilder.helper.ImageDisplayer
import org.koin.android.ext.android.inject

class NewDeckChooseIdentityFragment : Fragment() {
    var imgIdentity: ImageView? = null
    var mIdentity: Card? = null
    private val repo: CardRepository by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Main View
        imgIdentity = ImageView(activity)

        // Get the identity
        val bundle = arguments
        mIdentity = repo.getCard(bundle!!.getString(ARGUMENT_IDENTITY_CODE))

        // Display the identity
        //imgIdentity.setImageBitmap(mIdentity.getImage(getActivity()));
        ImageDisplayer.fill(imgIdentity, mIdentity, activity)
        return imgIdentity
    }

    companion object {
        const val ARGUMENT_IDENTITY_CODE = "com.example.netrunnerdeckbuilder.ARGUMENT_IDENTITY_CODE"
    }
}