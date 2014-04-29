package com.shuneault.netrunnerdeckbuilder.adapters;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.shuneault.netrunnerdeckbuilder.R;
import com.shuneault.netrunnerdeckbuilder.game.Deck;

public class ExpandableDeckListAdapter extends BaseExpandableListAdapter {
	
	static class ViewHolderItem {
		ImageView imgFaction;
		TextView lblDeckName;
	}
	
	private LayoutInflater mInflater;
	private Context mContext;
	private ArrayList<String> mArrDataHeader; // The headers
	private HashMap<String, ArrayList<Deck>> mArrDataChild;
	
	public ExpandableDeckListAdapter(Context context, ArrayList<String> listDataHeader, HashMap<String, ArrayList<Deck>> listChildData) {
		this.mContext = context;
		this.mArrDataHeader = listDataHeader;
		this.mArrDataChild = listChildData;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return mArrDataChild.get(mArrDataHeader.get(groupPosition)).get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		
		ViewHolderItem viewHolder;
		
		if (convertView == null) {
			// Inflate the layout
			convertView = mInflater.inflate(R.layout.list_view_deck, parent, false);
			
			// Set up the ViewHolder
			viewHolder = new ViewHolderItem();
			viewHolder.imgFaction = (ImageView) convertView.findViewById(R.id.imgFaction);
			viewHolder.lblDeckName= (TextView) convertView.findViewById(R.id.lblDeckName);
			
			// Store the ViewHolder
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolderItem) convertView.getTag();
		}
		
		// Get the object
		Deck deck = (Deck) getChild(groupPosition, childPosition);
		
		// Assign the values
		if (deck != null) {
			viewHolder.lblDeckName.setText(deck.getName());
			viewHolder.imgFaction.setImageResource(mContext.getResources().getIdentifier(deck.getIdentity().getFactionImageResName(), "drawable", mContext.getPackageName()));
		}
		
		// Return the view
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return mArrDataChild.get(mArrDataHeader.get(groupPosition)).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return mArrDataHeader.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return mArrDataHeader.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		
		View v = convertView;
		String headerTitle = getGroup(groupPosition).toString();
		if (convertView == null) {
			v = mInflater.inflate(R.layout.list_view_header, null, false);
		}
		
		TextView lblHeader = (TextView) v.findViewById(R.id.lblHeader);
		lblHeader.setText(headerTitle);
		//v.setOnClickListener(null);
		
		return v;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

}
