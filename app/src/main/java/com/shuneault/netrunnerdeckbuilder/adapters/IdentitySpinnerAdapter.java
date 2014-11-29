package com.shuneault.netrunnerdeckbuilder.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.shuneault.netrunnerdeckbuilder.R;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.helper.AppManager;

import java.util.ArrayList;

public class IdentitySpinnerAdapter extends ArrayAdapter<Card> {

    static class ViewHolderItem {
        ImageView imgFaction;
        TextView lblIdentityName;
    }

    public IdentitySpinnerAdapter(Context context, ArrayList<Card> cards) {
        super(context, 0, cards);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolderItem viewHolder;

        if (convertView == null) {
            // Inflate the layout
            LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
            convertView = inflater.inflate(R.layout.list_view_item_identity, parent, false);

            // Set up the ViewHolder
            viewHolder = new ViewHolderItem();
            viewHolder.imgFaction = (ImageView) convertView.findViewById(R.id.imgFaction);
            viewHolder.lblIdentityName = (TextView) convertView.findViewById(R.id.lblIdentityName);

            // Store the ViewHolder
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolderItem) convertView.getTag();
        }

        // Get the object
        Card card = this.getItem(position);

        // Assign the values
        if (card != null) {
            viewHolder.lblIdentityName.setText(card.getTitle());
            viewHolder.imgFaction.setImageResource(card.getFactionImageRes(getContext()));
            Log.i(AppManager.LOGCAT, "Res: " + card.getFactionImageRes(getContext()));
        }

        // Return the view
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        ViewHolderItem viewHolder;

        if (convertView == null) {
            // Inflate the layout
            LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
            convertView = inflater.inflate(R.layout.list_view_item_dropdown_identity, parent, false);

            // Set up the ViewHolder
            viewHolder = new ViewHolderItem();
            viewHolder.imgFaction = (ImageView) convertView.findViewById(R.id.imgFaction);
            viewHolder.lblIdentityName = (TextView) convertView.findViewById(R.id.lblIdentityName);

            // Store the ViewHolder
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolderItem) convertView.getTag();
        }

        // Get the object
        Card card = this.getItem(position);

        // Assign the values
        if (card != null) {
            viewHolder.lblIdentityName.setText(card.getTitle());
            viewHolder.imgFaction.setImageResource(card.getFactionImageRes(getContext()));
            Log.i(AppManager.LOGCAT, "Res: " + card.getFactionImageRes(getContext()));
        }

        // Return the view
        return convertView;
    }
}
