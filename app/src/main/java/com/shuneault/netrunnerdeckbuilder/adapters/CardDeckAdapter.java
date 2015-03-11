package com.shuneault.netrunnerdeckbuilder.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.shuneault.netrunnerdeckbuilder.R;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.helper.ImageDisplayer;

import java.util.ArrayList;

/**
 * Created by sebast on 2014-11-22.
 */
public class CardDeckAdapter extends RecyclerView.Adapter<CardDeckAdapter.ViewHolder> {

    private ArrayList<Deck> mDeckList;
    private ViewHolder.IViewHolderClicks mListener;
    private ViewGroup mViewGroup;

    public CardDeckAdapter(ArrayList<Deck> deckList, ViewHolder.IViewHolderClicks listener) {
        super();
        mDeckList = deckList;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup viewGroup, int i) {
        mViewGroup = viewGroup;
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_deck, viewGroup, false);
        return new ViewHolder(itemView, new ViewHolder.IViewHolderClicks() {
            @Override
            public void onClick(int index) {
                mListener.onClick(index);
            }

            @Override
            public void onDeckStarred(int index, boolean isStarred) {
                mListener.onDeckStarred(index, isStarred);
            }
        });
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {
        // Get the deck
        final Deck deck = mDeckList.get(i);

        // Set the values
        viewHolder.txtDeckTitle.setText(deck.getName());
        viewHolder.txtDeckNotes.setText(deck.getNotes());
        ImageDisplayer.fill(viewHolder.imgDeckIdentity, deck.getIdentity(), mViewGroup.getContext());
        viewHolder.chkStarred.setChecked(deck.isStarred());

    }

    @Override
    public int getItemCount() {
        return mDeckList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView txtDeckTitle;
        public TextView txtDeckNotes;
        public ImageView imgDeckIdentity;
        public CheckBox chkStarred;
        public IViewHolderClicks mListener;

        public ViewHolder(View v, IViewHolderClicks listener) {
            super(v);
            mListener = listener;
	        // set click listener to ripple view
            v.findViewById(R.id.ripple).setOnClickListener(this);
            txtDeckTitle = (TextView) v.findViewById(R.id.txtDeckTitle);
            txtDeckNotes = (TextView) v.findViewById(R.id.txtDeckNotes);
            imgDeckIdentity = (ImageView) v.findViewById(R.id.imgDeckIdentity);
            chkStarred = (CheckBox) v.findViewById(R.id.chkStar);
            chkStarred.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onDeckStarred(getPosition(), chkStarred.isChecked());
                }
            });
        }

        @Override
        public void onClick(View v) {
            mListener.onClick(getPosition());
        }

        public static interface IViewHolderClicks {
            public void onClick(int index);

            public void onDeckStarred(int index, boolean isStarred);
        }
    }

}