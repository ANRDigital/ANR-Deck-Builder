package com.shuneault.netrunnerdeckbuilder.adapters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Color;
import android.opengl.Visibility;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.shuneault.netrunnerdeckbuilder.R;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.helper.ImageDisplayer;

public class ExpandableDeckCardListAdapter extends BaseExpandableListAdapter {
	
	public interface OnButtonClickListener {
		void onPlusClick(Card card);
		void onMinusClick(Card card);
	}
	
	private static class ViewHolderItem {
		ImageView imgImage;
		TextView lblTitle;
		TextView lblText;
		TextView lblAmount;
		TextView lblInfluence;
		Button btnMinus;
		Button btnPlus;
	}
	
	private LayoutInflater mInflater;
	private Context mContext;
	private ArrayList<String> mArrDataHeader; // The headers
	private HashMap<String, ArrayList<Card>> mArrDataChild;
	private Deck mDeck; // The containing deck
	private boolean mMyCards = false;
	private OnButtonClickListener mListener;
	
	public ExpandableDeckCardListAdapter(Context context, ArrayList<String> listDataHeader, HashMap<String, ArrayList<Card>> listChildData, Deck deck, OnButtonClickListener listener) {
		this.mContext = context;
		this.mArrDataHeader = listDataHeader;
		this.mArrDataChild = listChildData;
		this.mDeck = deck;
		this.mListener = listener;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public ExpandableDeckCardListAdapter(Context context, ArrayList<String> listDataHeader, HashMap<String, ArrayList<Card>> listChildData, Deck deck, boolean isMyCards, OnButtonClickListener listener) {
		this(context, listDataHeader, listChildData, deck, listener);
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
		
		final ViewHolderItem viewHolder;		
		if (convertView == null) {
			// Inflate the layout
			convertView = mInflater.inflate(R.layout.list_view_item_cards_build, parent, false);
			
			// Set up the ViewHolder
			viewHolder = new ViewHolderItem();
			viewHolder.imgImage = (ImageView) convertView.findViewById(R.id.imgImage);
			viewHolder.lblTitle = (TextView) convertView.findViewById(R.id.lblTitre);
			viewHolder.lblText = (TextView) convertView.findViewById(R.id.lblText);
			viewHolder.lblAmount = (TextView) convertView.findViewById(R.id.lblAmount);
			viewHolder.btnMinus = (Button) convertView.findViewById(R.id.btnMinus);
			viewHolder.btnPlus = (Button) convertView.findViewById(R.id.btnPlus);
			viewHolder.lblInfluence = (TextView) convertView.findViewById(R.id.lblInfluence);
//			
			// Store the ViewHolder
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolderItem) convertView.getTag();
		}
		final View view = convertView;
		
		// Get the object
		final Card card = (Card) this.getChild(groupPosition, childPosition);
		
		// Set the background color
		setBackgroundColor(view, card);
		
		// Assign the values
		if (card != null) {
			viewHolder.lblTitle.setText(card.getTitle());
			viewHolder.lblText.setText(card.getFormattedText(mContext));
			ImageDisplayer.fillSmall(viewHolder.imgImage, card, mContext);
			viewHolder.lblAmount.setText(mDeck.getCardCount(card) + "/" + Deck.MAX_INDIVIDUAL_CARD);
			
			// Influence count
			if (!mDeck.getIdentity().getFaction().equals(card.getFaction())) {
				char[] chars = new char[card.getFactionCost()];
				Arrays.fill(chars, mContext.getResources().getString(R.string.influence_char).toCharArray()[0]);
				String result = new String(chars);
				viewHolder.lblInfluence.setText(result);
			} else {
				viewHolder.lblInfluence.setText("");
			}
			
			// Plus and minus buttons
			viewHolder.btnMinus.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					mDeck.setCardCount(card, mDeck.getCardCount(card) - 1);
					viewHolder.lblAmount.setText(mDeck.getCardCount(card) + "/" + Deck.MAX_INDIVIDUAL_CARD);
					setBackgroundColor(view, card);
					mListener.onMinusClick(card);
				}
			});
			viewHolder.btnPlus.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					mDeck.setCardCount(card, mDeck.getCardCount(card) + 1);
					viewHolder.lblAmount.setText(mDeck.getCardCount(card) + "/" + Deck.MAX_INDIVIDUAL_CARD);
					setBackgroundColor(view, card);
					mListener.onPlusClick(card);
				}
			});
				
		}
		
		// Return the view
		return convertView;
		
	}
	
	private void setBackgroundColor(View view, Card card) {
		// Do nothing for my cards
		if (mMyCards) return;
		// Green background for the cards I own
		if (mDeck.getCardCount(card) > 0) {
			view.setBackgroundColor(Color.parseColor("#DEFFF0"));
		} else {
			view.setBackgroundColor(Color.TRANSPARENT);
		}
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
