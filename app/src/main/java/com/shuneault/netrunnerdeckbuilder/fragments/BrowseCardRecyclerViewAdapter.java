package com.shuneault.netrunnerdeckbuilder.fragments;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.shuneault.netrunnerdeckbuilder.R;
import com.shuneault.netrunnerdeckbuilder.db.CardRepository;
import com.shuneault.netrunnerdeckbuilder.fragments.BrowseCardsFragment.OnBrowseCardsClickListener;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.helper.ImageDisplayer;
import com.shuneault.netrunnerdeckbuilder.helper.TextFormatter;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Card} and makes a call to the
 * specified {@link OnBrowseCardsClickListener}.
 */
public class BrowseCardRecyclerViewAdapter extends RecyclerView.Adapter<BrowseCardRecyclerViewAdapter.ViewHolder> {

    private final List<Card> mCards;
    private final OnBrowseCardsClickListener mListener;
    private CardRepository repo;

    public BrowseCardRecyclerViewAdapter(List<Card> items, OnBrowseCardsClickListener listener, CardRepository repo) {
        mCards = items;
        mListener = listener;
        this.repo = repo;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.setItem(mCards.get(position));
    }

    @Override
    public int getItemCount() {
        return mCards.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final View mView;
        private final TextView lblTitle;
        private final ImageView imgImage;
        private TextView lblText;
        private TextView lblIcons;
        private TextView lblInfluence;
        private TextView lblMostWanted;
        private TextView lblSetName;

        private Card mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            lblTitle = view.findViewById(R.id.lblTitre);
            lblText = view.findViewById(R.id.lblText);
            imgImage = view.findViewById(R.id.imgImage);
            lblIcons = view.findViewById(R.id.lblIcons);
            lblInfluence = view.findViewById(R.id.lblInfluence);
            lblMostWanted = view.findViewById(R.id.lblMostWanted);
            lblSetName = view.findViewById(R.id.lblSetName);
        }

        @Override
        public String toString() {
            return super.toString();
        }

        public void setItem(Card card) {
            this.mItem = card;
            Context context = lblText.getContext();
            lblIcons.setText(TextFormatter.FormatCardIcons(context, card));
            lblTitle.setText(TextFormatter.FormatCardTitle(card));
            lblInfluence.setText(TextFormatter.GetInfluenceString(context, card.getFactionCost()));

            lblText.setText(TextFormatter.getFormattedString(context, card.getText()));
            ImageDisplayer.fillSmall(imgImage, card, context);
            lblSetName.setText(repo.getPack(card.getSetCode()).getName());

            mView.setOnClickListener(v -> {
                if (null != mListener) {
                    mListener.onCardClicked(mItem);
                }
            });
        }
    }



}
