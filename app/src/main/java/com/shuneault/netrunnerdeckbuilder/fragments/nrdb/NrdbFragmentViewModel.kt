package com.shuneault.netrunnerdeckbuilder.fragments.nrdb

import android.content.Context
import androidx.lifecycle.ViewModel
import com.shuneault.netrunnerdeckbuilder.helper.NrdbHelper

class NrdbFragmentViewModel: ViewModel() {
    fun toggleNrdbSignIn(context: Context) {
        NrdbHelper.doNrdbSignIn(context)
    }

}
