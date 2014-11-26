package com.shuneault.netrunnerdeckbuilder.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.shuneault.netrunnerdeckbuilder.R;
import com.shuneault.netrunnerdeckbuilder.game.Deck;

import java.util.ArrayList;

public class HeaderListAdapter extends ArrayAdapter<HeaderListItemInterface> {

    private LayoutInflater mInflater;

    public HeaderListAdapter(Context context, ArrayList<HeaderListItemInterface> objects) {
        super(context, 0, objects);
        mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Return a header or an item, depending on the side
        View v;
        HeaderListItemInterface myItem = this.getItem(position);
        if (myItem.getItemType() == HeaderListItem.TYPE_HEADER) {
            HeaderListItem header = (HeaderListItem) myItem;
            v = mInflater.inflate(R.layout.list_view_header, null, false);
            TextView lblHeader = (TextView) v.findViewById(R.id.lblHeader);
            lblHeader.setText(header.getItemName());
            v.setOnClickListener(null);
        } else {
            Deck deck = (Deck) myItem;
            //v = mInflater.inflate(R.layout.list_view_header, null, false);
            v = mInflater.inflate(android.R.layout.simple_list_item_1, null, false);
            TextView lbl = (TextView) v.findViewById(android.R.id.text1);
            lbl.setText(deck.getName());
        }
        return v;
    }

}
