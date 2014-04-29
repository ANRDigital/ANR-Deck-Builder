package com.shuneault.netrunnerdeckbuilder.adapters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.shuneault.netrunnerdeckbuilder.R;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.Deck;

public class ExpandableDeckCardListAdapter extends BaseExpandableListAdapter {
	
	static class ViewHolderItem {
		ImageView imgFaction;
		TextView lblIdentityName;
		TextView lblInfluence;
		TextView lblCardCount;
	}
	
	private LayoutInflater mInflater;
	private Context mContext;
	private ArrayList<String> mArrDataHeader; // The headers
	private HashMap<String, ArrayList<Card>> mArrDataChild;
	private Deck mDeck; // The containing deck
	private boolean mMyCards = false;
	
	public ExpandableDeckCardListAdapter(Context context, ArrayList<String> listDataHeader, HashMap<String, ArrayList<Card>> listChildData, Deck deck) {
		this.mContext = context;
		this.mArrDataHeader = listDataHeader;
		this.mArrDataChild = listChildData;
		this.mDeck = deck;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public ExpandableDeckCardListAdapter(Context context, ArrayList<String> listDataHeader, HashMap<String, ArrayList<Card>> listChildData, Deck deck, boolean isMyCards) {
		this(context, listDataHeader, listChildData, deck);
		mMyCards = true;
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
			convertView = mInflater.inflate(R.layout.list_view_item_deck_card, parent, false);
			
			// Set up the ViewHolder
			viewHolder = new ViewHolderItem();
			viewHolder.imgFaction = (ImageView) convertView.findViewById(R.id.imgFaction);
			viewHolder.lblIdentityName = (TextView) convertView.findViewById(R.id.lblIdentityName);
			viewHolder.lblCardCount = (TextView) convertView.findViewById(R.id.lblCardCount);
			viewHolder.lblInfluence = (TextView) convertView.findViewById(R.id.lblInfluence);
			
			// Store the ViewHolder
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolderItem) convertView.getTag();
		}
		
		// Get the object
		Card card = (Card) this.getChild(groupPosition, childPosition);
		
		// Assign the values
		if (card != null) {
			viewHolder.lblIdentityName.setText(card.getTitle());
			//viewHolder.imgFaction.setImageResource(mContext.getResources().getIdentifier(card.getFactionImageResName(), "drawable", mContext.getPackageName()));
			viewHolder.imgFaction.setImageResource(card.getFactionImageRes(mContext));
			viewHolder.lblCardCount.setText(mDeck.getCardCount(card) + "/" + Deck.MAX_INDIVIDUAL_CARD);
			
			// Influence count
			if (!mDeck.getIdentity().getFaction().equals(card.getFaction())) {
				char[] chars = new char[card.getFactionCost()];
				Arrays.fill(chars, mContext.getResources().getString(R.string.influence_char).toCharArray()[0]);
				String result = new String(chars);
				viewHolder.lblInfluence.setText(result);
			} else {
				viewHolder.lblInfluence.setText("");
			}
				
		}
		
		// Return the view
		return convertView;
		
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		try {
		return mArrDataChild.get(mArrDataHeader.get(groupPosition)).size();
		} catch (Exception e) {
			return 0;
		}
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
		/**
		 * The count is:
		 * 		mMyCards=true: How many cards in TOTAL
		 * 		mMyCards=false: How many different cards
		 */
		if (mMyCards) {
			int iCount = 0;
			for (Card card : mDeck.getCards()) {
				if (card.getType().equalsIgnoreCase(getGroup(groupPosition).toString()))
					iCount = iCount + mDeck.getCardCount(card);
			}
			lblHeader.setText(headerTitle + " (" + iCount + ")");
		} else {
			lblHeader.setText(headerTitle + " (" + getChildrenCount(groupPosition) + ")");
		}
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
