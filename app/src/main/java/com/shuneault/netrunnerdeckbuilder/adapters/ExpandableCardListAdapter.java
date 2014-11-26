package com.shuneault.netrunnerdeckbuilder.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.shuneault.netrunnerdeckbuilder.R;
import com.shuneault.netrunnerdeckbuilder.game.Card;

import java.util.ArrayList;
import java.util.HashMap;

public class ExpandableCardListAdapter extends BaseExpandableListAdapter {

    static class ViewHolderItem {
        ImageView imgFaction;
        TextView lblIdentityName;
    }

    private LayoutInflater mInflater;
    private Context mContext;
    private ArrayList<String> mArrDataHeader; // The headers
    private HashMap<String, ArrayList<Card>> mArrDataChild;

    public ExpandableCardListAdapter(Context context, ArrayList<String> listDataHeader, HashMap<String, ArrayList<Card>> listChildData) {
        this.mContext = context;
        this.mArrDataHeader = listDataHeader;
        this.mArrDataChild = listChildData;
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

        ViewHolderItem viewHolder;

        if (convertView == null) {
            // Inflate the layout
            convertView = mInflater.inflate(R.layout.list_view_item_card, parent, false);

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
        Card card = (Card) this.getChild(groupPosition, childPosition);

        // Assign the values
        if (card != null) {
            viewHolder.lblIdentityName.setText(card.getTitle());
            viewHolder.imgFaction.setImageResource(card.getFactionImageRes(mContext));
        }

        // Return the view
        return convertView;

    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mArrDataChild.get(mArrDataHeader.get(groupPosition)).size();
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
        lblHeader.setText(headerTitle);
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
