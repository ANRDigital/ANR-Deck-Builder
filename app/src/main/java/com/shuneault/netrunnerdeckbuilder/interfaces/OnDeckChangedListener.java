package com.shuneault.netrunnerdeckbuilder.interfaces;

import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.Deck;

public interface OnDeckChangedListener {
    void onDeckNameChanged(Deck deck, String name);

    void onDeckDeleted(Deck deck);

    void onDeckCloned(Deck deck);

    void onDeckCardsChanged();

    void onDeckIdentityChanged(Card newIdentity);

    void onSettingsChanged();
}
