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
import com.shuneault.netrunnerdeckbuilder.helper.ImageDisplayer;

import java.util.List;

/**
 * Created by sebast on 21/02/15.
 */
public class CardsImageAdapter extends ArrayAdapter<Card> {
    private static class ViewHolder {
        ImageView imgCard;
        TextView lblAmount;
    }

    public CardsImageAdapter(Context context, List<Card> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.deck_view_image_layout, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.imgCard = (ImageView) convertView.findViewById(R.id.imgCard);
            viewHolder.lblAmount = (TextView) convertView.findViewById(R.id.lblAmount);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Set the image
        Card card = getItem(position);
        ImageDisplayer.fill(viewHolder.imgCard, card, getContext());
        viewHolder.lblAmount.setVisibility(View.GONE);

        // Return the image
        return convertView;
    }
}
