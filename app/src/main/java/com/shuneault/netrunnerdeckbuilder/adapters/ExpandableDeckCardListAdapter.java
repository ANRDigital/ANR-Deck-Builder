package com.shuneault.netrunnerdeckbuilder.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.shuneault.netrunnerdeckbuilder.R;
import com.shuneault.netrunnerdeckbuilder.SettingsActivity;
import com.shuneault.netrunnerdeckbuilder.db.CardRepository;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.CardMWL;
import com.shuneault.netrunnerdeckbuilder.game.CardPool;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.game.MostWantedList;
import com.shuneault.netrunnerdeckbuilder.helper.AppManager;
import com.shuneault.netrunnerdeckbuilder.helper.ImageDisplayer;
import com.shuneault.netrunnerdeckbuilder.helper.TextFormatter;

import java.util.ArrayList;
import java.util.HashMap;

public class ExpandableDeckCardListAdapter extends BaseExpandableListAdapter {

    private CardRepository mCardRepo;

    public interface OnButtonClickListener {
        void onPlusClick(Card card);

        void onMinusClick(Card card);
    }

    private static class DeckCardListItemViewHolder {
        ImageView imgImage;
        TextView lblIcons;
        TextView lblTitle;
        TextView lblText;
        TextView lblAmount;
        TextView lblInfluence;
        TextView lblMostWanted;
        TextView lblSetName;
        Button btnMinus;
        Button btnPlus;

        DeckCardListItemViewHolder(View view) {
            imgImage = view.findViewById(R.id.imgImage);
            lblIcons = view.findViewById(R.id.lblIcons);
            lblTitle = view.findViewById(R.id.lblTitre);
            lblText = view.findViewById(R.id.lblText);
            lblAmount = view.findViewById(R.id.lblAmount);
            btnMinus = view.findViewById(R.id.btnMinus);
            btnPlus = view.findViewById(R.id.btnPlus);
            lblInfluence = view.findViewById(R.id.lblInfluence);
            lblMostWanted = view.findViewById(R.id.lblMostWanted);
            lblSetName = view.findViewById(R.id.lblSetName);
        }
    }

    private LayoutInflater mInflater;
    private Context mContext;
    private ArrayList<String> mArrDataHeader; // The headers
    private HashMap<String, ArrayList<Card>> mArrDataChild;
    private ArrayList<String> mArrDataHeaderOriginal; // The headers
    private HashMap<String, ArrayList<Card>> mArrDataChildOriginal;
    private Deck mDeck; // The containing deck
    private boolean mMyCards = false;
    private OnButtonClickListener mListener;

    public ExpandableDeckCardListAdapter(CardRepository mCardRepo, Context context, ArrayList<String> listDataHeader, HashMap<String, ArrayList<Card>> listChildData, Deck deck, OnButtonClickListener listener) {
        this.mCardRepo = mCardRepo;
        this.mContext = context;
        this.mArrDataHeader = listDataHeader;
        this.mArrDataChild = listChildData;
        this.mArrDataHeaderOriginal = (ArrayList<String>) listDataHeader.clone();
        this.mArrDataChildOriginal = (HashMap<String, ArrayList<Card>>) listChildData.clone();
        this.mDeck = deck;
        this.mListener = listener;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public ExpandableDeckCardListAdapter(Context context, ArrayList<String> listDataHeader, HashMap<String, ArrayList<Card>> listChildData, Deck deck, boolean isMyCards, OnButtonClickListener listener, CardRepository mCardRepo) {
        this(mCardRepo, context, listDataHeader, listChildData, deck, listener);
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

        final DeckCardListItemViewHolder viewHolder;
        if (convertView == null) {
            // Inflate the layout
            convertView = mInflater.inflate(R.layout.list_view_item_cards_build, parent, false);

            // Set up the ViewHolder
            viewHolder = new DeckCardListItemViewHolder(convertView);

            // Store the ViewHolder
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (DeckCardListItemViewHolder) convertView.getTag();
        }
        final View view = convertView;

        // Get the object
        final Card card = (Card) this.getChild(groupPosition, childPosition);

        // Set the background color
        setBackgroundColor(view, card);

        // Assign the values
        if (card != null) {
            ImageDisplayer.fillSmall(viewHolder.imgImage, card, mContext);
            // Headline
            viewHolder.lblIcons.setText(TextFormatter.FormatCardIcons(mContext, card));
            viewHolder.lblTitle.setText(TextFormatter.FormatCardTitle(card));

            viewHolder.lblText.setText(TextFormatter.getFormattedString(mContext, card.getText()));

            final Integer maxCardCount = mDeck.getCardPool().getMaxCardCount(card);
            viewHolder.lblAmount.setText(mDeck.getCardCount(card) + "/" + maxCardCount);

            // Set names
            viewHolder.lblSetName.setText(card.getSetName());
            if (AppManager.getInstance().getSharedPrefs().getBoolean(SettingsActivity.KEY_PREF_DISPLAY_SET_NAMES_WITH_CARDS, false)) {
                viewHolder.lblSetName.setVisibility(View.VISIBLE);
            } else {
                viewHolder.lblSetName.setVisibility(View.GONE);
            }

            // Influence cost
            int numInfluence = 0;
            // out of faction influence cost
            if (!mDeck.isFaction(card.getFactionCode())) {
                numInfluence += card.getFactionCost();
            }
            // universal influence cost
            if (card.isMostWanted() && AppManager.getInstance().getSharedPrefs().getBoolean(SettingsActivity.KEY_PREF_USE_MOST_WANTED_LIST, false)) {
                //todo: get mwlinfluence from the current mwl entity
                numInfluence += card.getMWLInfluence();
            }
            viewHolder.lblInfluence.setText(TextFormatter.GetInfluenceString(mContext, numInfluence));

            // MWL 2.0 indicators
            viewHolder.lblMostWanted.setText("");
            MostWantedList mwl = AppManager.getInstance().getMWL();
            CardMWL cardMWL = mwl.GetCardMWL(card);
            if (cardMWL != null) {
                viewHolder.lblMostWanted.setText(TextFormatter.GetMWLIcon(cardMWL));
            }

            // Plus and minus buttons
            viewHolder.btnMinus.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    mDeck.ReduceCard(card);
                    viewHolder.lblAmount.setText(mDeck.getCardCount(card) + "/" + maxCardCount);
                    setBackgroundColor(view, card);
                    mListener.onMinusClick(card);
                }
            });
            viewHolder.btnPlus.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    mDeck.AddCard(card);
                    viewHolder.lblAmount.setText(mDeck.getCardCount(card) + "/" + maxCardCount);
                    setBackgroundColor(view, card);
                    mListener.onPlusClick(card);
                }
            });

        }

        // Return the view
        return convertView;
    }

    private void setBackgroundColor(View view, Card card) {
        // Do nothing when in MyCards view
        if (mMyCards) return;

        // Colored background for the cards in the deck
        if (mDeck.getCardCount(card) > 0) {
            int theColor = mContext.getResources().getIdentifier("light_" + mDeck.getIdentity().getFactionCode().replace("-", ""), "color", mContext.getPackageName());
            if (theColor != 0) {
                view.setBackgroundColor(mContext.getResources().getColor(theColor));
            } else {
                view.setBackgroundColor(mContext.getResources().getColor(R.color.netrunner_blue_light));
            }
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
         * 		mMyCards=true: How many cards in deck (total count)
         * 		mMyCards=false: How many different cards available within group
         */
        if (mMyCards) {
            int iCount = 0;
            for (Card card : mDeck.getCards()) {
                if (card.getTypeCode().equalsIgnoreCase(getGroup(groupPosition).toString()))
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

    public void filterData(String query) {
        query = query.toLowerCase();
        mArrDataChild.clear();
        mArrDataHeader.clear();

        // empty query? show all
        if (query.isEmpty()) {
            mArrDataHeader.addAll(mArrDataHeaderOriginal);
            for (String type : mArrDataHeader) {
                if (mArrDataChildOriginal.get(type) != null) {
                    mArrDataChild.put(type, new ArrayList<Card>());
                    mArrDataChild.get(type).addAll(mArrDataChildOriginal.get(type));
                }
            }
        } else {
            // Do filter
            for (String type : mArrDataHeaderOriginal) {
                mArrDataChild.put(type, new ArrayList<Card>());
                if (mArrDataChildOriginal.get(type) != null) {
                    for (Card card : mArrDataChildOriginal.get(type)) {
                        if (card.getTitle().toLowerCase().contains(query) ||
                                card.getText().toLowerCase().contains(query) ||
                                card.getSubtype().toLowerCase().contains(query) ||
                                card.getSetCode().toLowerCase().contains(query)) {
                            // Add the header
                            if (!mArrDataHeader.contains(type)) {
                                mArrDataHeader.add(type);
                            }
                            mArrDataChild.get(type).add(card);
                        }
                    }
                }
            }
        }
        // Show the new list
        notifyDataSetChanged();
    }

}
