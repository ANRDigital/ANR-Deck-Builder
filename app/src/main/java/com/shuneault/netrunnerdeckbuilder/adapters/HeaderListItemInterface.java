package com.shuneault.netrunnerdeckbuilder.adapters;

public interface HeaderListItemInterface {
    public static final int TYPE_HEADER = 1;
    public static final int TYPE_ITEM = 2;

    public int getItemType();

    public String getItemName();
}