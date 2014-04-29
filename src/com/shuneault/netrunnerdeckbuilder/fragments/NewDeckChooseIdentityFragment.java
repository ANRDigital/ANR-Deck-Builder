
package com.shuneault.netrunnerdeckbuilder.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.helper.AppManager;
import com.shuneault.netrunnerdeckbuilder.helper.ImageDisplayer;

public class NewDeckChooseIdentityFragment extends Fragment {
	
	public static final String ARGUMENT_IDENTITY_CODE = "com.example.netrunnerdeckbuilder.ARGUMENT_IDENTITY_CODE";
		
	ImageView imgIdentity;
	
	Card mIdentity;
		
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		// Main View
		imgIdentity = new ImageView(getActivity());
		
		// Get the identity
		Bundle bundle = getArguments();
		mIdentity = AppManager.getInstance().getCard(bundle.getString(ARGUMENT_IDENTITY_CODE));
		
		// Display the identity
		//imgIdentity.setImageBitmap(mIdentity.getImage(getActivity()));
		ImageDisplayer.fill(imgIdentity, mIdentity, getActivity());
				
		return imgIdentity;
	}

}
