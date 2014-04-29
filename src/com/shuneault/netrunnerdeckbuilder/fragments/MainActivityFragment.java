package com.shuneault.netrunnerdeckbuilder.fragments;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.shuneault.netrunnerdeckbuilder.R;
import com.shuneault.netrunnerdeckbuilder.ViewDeckFullscreenActivity;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.helper.AppManager;
import com.shuneault.netrunnerdeckbuilder.helper.CardImagesDownloader;
import com.shuneault.netrunnerdeckbuilder.helper.CardImagesDownloader.CardImagesDownloaderListener;

public class MainActivityFragment extends Fragment {
	
	// GUI
	private View theView;
	private TextView lblNumberOfDecks;
	private TextView lblNumberImagesCached;
	private Button btnViewCardsBySet;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		// View
		theView = inflater.inflate(R.layout.fragment_main_activity, container, false);
		
		// GUI
		lblNumberOfDecks = (TextView) theView.findViewById(R.id.lblNumberOfDecks);
		lblNumberImagesCached = (TextView) theView.findViewById(R.id.lblNumberImagesCached);
		btnViewCardsBySet = (Button) theView.findViewById(R.id.btnViewCardsBySet);
		
		// Option menu
		setHasOptionsMenu(true);
		
		// Set the text
		updateInfo();
		
		// Event listeners
		btnViewCardsBySet.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Get the set names
				final ArrayList<String> setNames = new ArrayList<String>();
				for ( String setName : AppManager.getInstance().getSetNames() ) {
					setNames.add(setName + " (" + AppManager.getInstance().getCardsBySetName(setName).size() + ")");
				}
				CharSequence[] cs = setNames.toArray(new CharSequence[setNames.size()]);
				// Display the dialog
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle(R.string.view_cards);
				builder.setItems(cs, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Launch the full screen image viewer activity
						Intent intent = new Intent(getActivity(), ViewDeckFullscreenActivity.class);
						intent.putExtra(ViewDeckFullscreenActivity.EXTRA_SET_NAME, AppManager.getInstance().getSetNames().get(which));
						startActivity(intent);
					}
				});
				builder.show();
			}
		});

		// Return the view
		return theView;
	}
	
	public void updateInfo() {
		lblNumberOfDecks.setText(String.valueOf(AppManager.getInstance().getAllDecks().size()));
		lblNumberImagesCached.setText(String.valueOf(getNumberImagesCached()) + "/" + AppManager.getInstance().getAllCards().size());
	}
	
	@Override
	public void onResume() {
		super.onResume();
		updateInfo();
	}
	
	private int getNumberImagesCached() {
		return AppManager.getInstance().getNumberImagesCached(getActivity());
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// 
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.main_fragment, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.mnuDownloadAllImages:
			// Ask for confirmation
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage(R.string.download_all_images_question);
			builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// Download all images
					CardImagesDownloader dnl = new CardImagesDownloader(getActivity(), new CardImagesDownloaderListener() { 
						
						@Override
						public void onTaskCompleted() {

						}
						
						@Override
						public void onImageDownloaded(Card card, int count, int max) {
							updateInfo();
						}
						
						@Override
						public void onBeforeStartTask(Context context, int max) {

						}
					});
					dnl.execute(getActivity());
				}
			});
			builder.setNegativeButton(android.R.string.no, null);
			builder.create().show();
			return true;
			
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
}
