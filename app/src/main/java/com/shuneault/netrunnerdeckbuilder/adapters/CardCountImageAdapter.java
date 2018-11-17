package com.shuneault.netrunnerdeckbuilder.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.shuneault.netrunnerdeckbuilder.R;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.CardCount;
import com.shuneault.netrunnerdeckbuilder.helper.ImageDisplayer;

import java.util.List;

/**
 * Created by sebast on 21/02/15.
 */
public class CardCountImageAdapter extends ArrayAdapter<CardCount> {
    private static class CardViewHolder {
        ImageView imgCard;
        TextView lblAmount;
    }

    public CardCountImageAdapter(Context context, List<CardCount> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CardViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.deck_view_image_layout, parent, false);
            viewHolder = new CardViewHolder();
            viewHolder.imgCard = (ImageView) convertView.findViewById(R.id.imgCard);
            viewHolder.lblAmount = (TextView) convertView.findViewById(R.id.lblAmount);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (CardViewHolder) convertView.getTag();
        }

        // Set the image]
        CardCount cc = getItem(position);
        Card card = cc.getCard();
        ImageDisplayer.fill(viewHolder.imgCard, card, getContext());
        if (cc.getCount() > 0)
            viewHolder.lblAmount.setText(String.valueOf(cc.getCount()));
        else
            viewHolder.lblAmount.setVisibility(View.GONE);

        // Return the image
        return convertView;
    }
}
