package com.shuneault.netrunnerdeckbuilder.adapters;

public class HeaderListItem implements HeaderListItemInterface {

    private String name;

    public HeaderListItem(String name) {
        this.name = name;
    }

    @Override
    public int getItemType() {
        //
        return HeaderListItemInterface.TYPE_HEADER;
    }

    @Override
    public String getItemName() {
        //
        return name;
    }


}
