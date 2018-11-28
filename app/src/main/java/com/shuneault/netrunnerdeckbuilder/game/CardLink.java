package com.shuneault.netrunnerdeckbuilder.game;

public class CardLink {
    private String cardCode;
    private int quantity;

    public CardLink(String cardCode, int quantity) {
        this.cardCode = cardCode;
        this.quantity = quantity;
    }

    public String getCardCode() {
        return cardCode;
    }

    public int getQuantity() {
        return quantity;
    }
}
