package com.shuneault.netrunnerdeckbuilder.interfaces;

import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.Deck;

public interface OnDeckChangedListener {
    void onDeckCardsChanged();

    void onDeckIdentityChanged(Card newIdentity);
}
