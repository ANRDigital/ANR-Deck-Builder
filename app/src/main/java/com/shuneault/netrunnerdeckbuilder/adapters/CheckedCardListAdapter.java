package com.shuneault.netrunnerdeckbuilder.adapters;

import android.content.Context;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.shuneault.netrunnerdeckbuilder.R;
import com.shuneault.netrunnerdeckbuilder.game.CardCount;

import java.util.ArrayList;

public class CheckedCardListAdapter extends ArrayAdapter<CardCount> {

    private static class ViewHolderItem {
        CheckBox chkChecked;
        ImageView imgImage;
        TextView lblCardName;
        TextView lblCardCount;
        TextView lblSetAndNumber;
    }

    private Context mContext;
    private LayoutInflater mInflater;

    public CheckedCardListAdapter(Context context, ArrayList<CardCount> objects) {
        super(context, 0, objects);
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final ViewHolderItem viewHolder;

        // Get the card
        final CardCount cardCount = this.getItem(position);

        if (convertView == null) {
            // Inflater the layout
            convertView = mInflater.inflate(R.layout.list_view_item_card_checked, parent, false);

            // Setup the holder
            viewHolder = new ViewHolderItem();
            viewHolder.chkChecked = (CheckBox) convertView.findViewById(R.id.chkChecked);
            viewHolder.imgImage = (ImageView) convertView.findViewById(R.id.imgImage);
            viewHolder.lblCardName = (TextView) convertView.findViewById(R.id.lblCardName);
            viewHolder.lblCardCount = (TextView) convertView.findViewById(R.id.lblCardCount);
            viewHolder.lblSetAndNumber = (TextView) convertView.findViewById(R.id.lblSetAndNumber);

            // Store the view holder in the view
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolderItem) convertView.getTag();
        }

        // Assign the values
        if (cardCount != null) {
            viewHolder.chkChecked.setChecked(cardCount.isDone());
            //viewHolder.imgFaction.setImageResource(mContext.getResources().getIdentifier(cardCount.getCard().getFactionImageResName(), "drawable", mContext.getPackageName()));
            viewHolder.imgImage.setImageBitmap(cardCount.getCard().getImage(getContext()));
            viewHolder.lblCardName.setText(cardCount.getCard().getTitle());
            if (cardCount.getCount() > 0) {
                viewHolder.lblCardCount.setTextColor(ContextCompat.getColor(getContext(), R.color.value_positive));
                viewHolder.lblCardCount.setText(String.valueOf("+" + cardCount.getCount()));
            } else {
                viewHolder.lblCardCount.setTextColor(ContextCompat.getColor(getContext(), R.color.value_negative));
                viewHolder.lblCardCount.setText(String.valueOf(cardCount.getCount()));
            }
            viewHolder.lblSetAndNumber.setText(cardCount.getCard().getCode() + " - " + cardCount.getCard().getSetCode());
        } else {
            return null;
        }

        return convertView;
    }

}
