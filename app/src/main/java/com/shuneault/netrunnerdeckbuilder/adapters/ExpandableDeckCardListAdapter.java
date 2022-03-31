package com.shuneault.netrunnerdeckbuilder.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.shuneault.netrunnerdeckbuilder.R;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.CardMWL;
import com.shuneault.netrunnerdeckbuilder.game.CardPool;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.game.Format;
import com.shuneault.netrunnerdeckbuilder.game.MostWantedList;
import com.shuneault.netrunnerdeckbuilder.helper.ImageDisplayer;
import com.shuneault.netrunnerdeckbuilder.helper.TextFormatter;

import java.util.ArrayList;
import java.util.HashMap;

public class ExpandableDeckCardListAdapter extends BaseExpandableListAdapter {

    private boolean showPackNames;
    private CardPool pool;

    private LayoutInflater mInflater;
    private Context mContext;
    private ArrayList<String> mArrDataHeader; // The headers
    private HashMap<String, ArrayList<Card>> mArrDataChild;
    private ArrayList<String> mArrDataHeaderOriginal; // The headers
    private HashMap<String, ArrayList<Card>> mArrDataChildOriginal;
    private Deck mDeck; // The containing deck
    private boolean mMyCards;
    private OnButtonClickListener mListener;
    private final MostWantedList mMostWantedList;

    public interface OnButtonClickListener {
        void onPlusClick(Card card);

        void onMinusClick(Card card);
    }


    public ExpandableDeckCardListAdapter(Context context, ArrayList<String> listDataHeader,
                                         HashMap<String, ArrayList<Card>> listChildData, Deck deck,
                                         OnButtonClickListener listener, boolean myCardsMode, boolean showPackNames, MostWantedList mostWantedList, CardPool cardPool) {
        this.mContext = context;
        this.mArrDataHeader = listDataHeader;
        this.mArrDataChild = listChildData;
        this.mArrDataHeaderOriginal = (ArrayList<String>) listDataHeader.clone();
        this.mArrDataChildOriginal = (HashMap<String, ArrayList<Card>>) listChildData.clone();
        this.mDeck = deck;
        this.pool = cardPool;
        this.mMostWantedList = mostWantedList;
        this.mListener = listener;
        this.mMyCards = myCardsMode;
        this.showPackNames = showPackNames;

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
            CardMWL cardMWL = mMostWantedList == null ? null : mMostWantedList.GetCardMWL(card);
            viewHolder.setItem(card, cardMWL, pool.getMaxCardCount(card), showPackNames);
        }

        // Return the view
        return convertView;
    }

    private void setBackgroundColor(View view, Card card) {
        // Do nothing when in MyCards view
        if (mMyCards) return;

        // Colored background for the cards in the deck
        if (mDeck.getCardCount(card) > 0) {
            int theColor = mContext.getResources().getIdentifier("light_" + mDeck.getFactionCode().replace("-", ""), "color", mContext.getPackageName());
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

        TextView lblHeader = v.findViewById(R.id.lblHeader);
        /*
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

    public void filterData(String newQuery) {
        String query = newQuery.toLowerCase();
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

    private class DeckCardListItemViewHolder {
        private final View mView;
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
            mView = view;
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

        public void setItem(Card card, CardMWL cardMWL, int maxCardCount, boolean showPackNames) {
            Context context = lblText.getContext();
            ImageDisplayer.fillSmall(imgImage, card, context);
            // Headline
            lblIcons.setText(TextFormatter.FormatCardIcons(context, card));
            lblTitle.setText(TextFormatter.FormatCardTitle(card));
            lblText.setText(TextFormatter.getFormattedString(context, card.getText()));

            updateAmount(mDeck.getCardCount(card), maxCardCount);

            // Set names
            if (showPackNames) {
                lblSetName.setText(card.getPack().getName());
                lblSetName.setVisibility(View.VISIBLE);
            } else {
                lblSetName.setVisibility(View.GONE);
            }

            // Influence cost
            int numInfluence = 0;
            // out of faction influence cost
            if (!mDeck.isFaction(card.getFactionCode())) {
                numInfluence += card.getFactionCost();
            }
            // universal influence cost
            if (cardMWL != null) {
                numInfluence += cardMWL.getUniversalFactionCost();
            }
            lblInfluence.setText(TextFormatter.GetInfluenceString(context, numInfluence));

            // MWL 2.0 indicators
            lblMostWanted.setText("");
            if (cardMWL != null) {
                lblMostWanted.setText(TextFormatter.GetMWLIcon(cardMWL));
            }

            // Plus and minus buttons
            btnMinus.setOnClickListener(v -> {
                // tell the fragment - should trigger game state change
                mListener.onMinusClick(card);
                // update to reflect any change
                setBackgroundColor(mView, card);
                updateAmount(mDeck.getCardCount(card) , maxCardCount);
            });
            btnPlus.setOnClickListener(v -> {
                mListener.onPlusClick(card);
                updateAmount(mDeck.getCardCount(card) , maxCardCount);
                setBackgroundColor(mView, card);
            });

        }

        private void updateAmount(Integer count, Integer maxCardCount) {
            String text = count + "/" + maxCardCount;
            if (count > maxCardCount){
                btnPlus.setText("⚠");
                btnPlus.setEnabled(false);
            } else {
                btnPlus.setText("+");
                btnPlus.setEnabled(true);
            }
            lblAmount.setText(text);
        }
    }

}
